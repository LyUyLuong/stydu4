package com.lul.Stydu4.service;

public interface IJwtBlacklistService {

    void blacklistToken(String jwtId, long expirationTimeInSeconds);
    boolean isTokenBlacklisted(String jwtId);

}
