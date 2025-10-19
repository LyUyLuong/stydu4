package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.AuthenticationRequest;
import com.lul.Stydu4.dto.request.IntrospectRequest;
import com.lul.Stydu4.dto.request.LogoutRequest;
import com.lul.Stydu4.dto.request.RefreshTokenRequest;
import com.lul.Stydu4.dto.response.AuthenticationResponse;
import com.lul.Stydu4.dto.response.IntrospectResponse;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.UserMapper;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IAuthenticationService;
import com.lul.Stydu4.service.IJwtBlacklistService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements IAuthenticationService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    IUserRepository userRepository;
    UserMapper userMapper;
    IJwtBlacklistService jwtBlacklistService;


    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();

        boolean isValid = true;

        try {
            verify(token,false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }


    @Override
    public String generateToken(UserEntity user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("Stydu4")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
        log.error("Cannot create token", e);
        throw new RuntimeException(e);
        }
    }

    @Override
    public String buildScope(UserEntity user){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(roleEntity -> {
                stringJoiner.add("ROLE_"+roleEntity.getName());
                if(!CollectionUtils.isEmpty(roleEntity.getPermissions())){
                    roleEntity.getPermissions().forEach(permissionEntity -> {
                        stringJoiner.add(permissionEntity.getName());
                    });
                }
            });
        }

        log.warn(stringJoiner.toString());
        return stringJoiner.toString();
    }

    @Override
    public void logout(LogoutRequest request) {
        try {
            SignedJWT token = verify(request.getToken(), false);

            String jit = token.getJWTClaimsSet().getJWTID();
            Date expirationTime = token.getJWTClaimsSet().getExpirationTime();

            long ttl = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                jwtBlacklistService.blacklistToken(jit, ttl);
            }
        } catch (Exception e) {
            log.info("Logout called with invalid or expired token, ignoring: {}", e.getMessage());
        }
    }

    private SignedJWT verify(String token,Boolean isRefresh) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION,ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isAuthenticated =  signedJWT.verify(verifier);

        if(!isAuthenticated || expiryTime.before(new Date())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

//        if (jwtBlacklistService.isTokenBlacklisted(signedJWT.getJWTClaimsSet().getJWTID()))
//            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;

    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {

        SignedJWT signedJWT = verify(request.getToken(),true);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        String username = signedJWT.getJWTClaimsSet().getSubject();
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        long ttl = (expiryTime.getTime() - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            jwtBlacklistService.blacklistToken(jit, ttl);
        }

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }
}
