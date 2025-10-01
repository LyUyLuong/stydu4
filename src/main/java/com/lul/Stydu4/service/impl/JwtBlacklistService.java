package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.service.IJwtBlacklistService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtBlacklistService implements IJwtBlacklistService {
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    RedisTemplate<String, String> redisTemplate;

    // Thêm JWT vào danh sách đen với thời gian hết hạn (TTL)
    public void blacklistToken(String jwtId, long expirationTimeInSeconds) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jwtId, "blacklisted", expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    // Kiểm tra xem JWT có trong danh sách đen hay không
    public boolean isTokenBlacklisted(String jwtId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jwtId));
    }
}
