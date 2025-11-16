package com.lul.Stydu4.aspect;

import com.lul.Stydu4.annotation.RateLimited;
import com.lul.Stydu4.exception.RateLimitExceededException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RateLimitAspect {

    RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(com.lul.Stydu4.annotation.RateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimited rateLimited = signature.getMethod().getAnnotation(RateLimited.class);

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication != null ? authentication.getName() : "anonymous";

        // Build Redis key
        String key = String.format("%s:%s:%s",
                rateLimited.keyPrefix(),
                signature.getMethod().getName(),
                userName);

        log.debug("Checking rate limit for key: {}", key);

        // Get current count from Redis
        String countStr = redisTemplate.opsForValue().get(key);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (currentCount >= rateLimited.maxRequests()) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.warn("Rate limit exceeded for user: {}, key: {}, count: {}, TTL: {}s",
                    userName, key, currentCount, ttl);
            
            throw new RateLimitExceededException(
                    String.format("Bạn đã nộp bài quá nhiều lần. Vui lòng chờ %d giây trước khi thử lại.", ttl != null ? ttl : rateLimited.timeWindowSeconds())
            );
        }

        // Increment counter
        Long newCount = redisTemplate.opsForValue().increment(key);
        
        // Set expiration on first request
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimited.timeWindowSeconds()));
            log.debug("Set rate limit window for key: {}, duration: {}s", key, rateLimited.timeWindowSeconds());
        }

        log.debug("Rate limit check passed for user: {}, count: {}/{}", 
                userName, newCount, rateLimited.maxRequests());

        // Proceed with the method execution
        return joinPoint.proceed();
    }
}
