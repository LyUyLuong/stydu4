package com.lul.Stydu4.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * 
 * Configures caching for frequently accessed data:
 * - Tests: 1 hour TTL
 * - Courses: 2 hours TTL
 * - Users: 30 minutes TTL
 * - Parts: 1 hour TTL
 * - Questions: 1 hour TTL
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Create ObjectMapper for JSON serialization with Java 8 time support
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    /**
     * Configure Redis Cache Manager with different TTLs for different cache names
     */
    @Bean
    @Override
    public CacheManager cacheManager() {
        ObjectMapper objectMapper = createObjectMapper();
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)))
                .disableCachingNullValues();

        // Specific configurations for different caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Tests cache - 1 hour TTL
        cacheConfigurations.put("tests", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Test details cache - 1 hour TTL
        cacheConfigurations.put("test-details", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Courses cache - 2 hours TTL
        cacheConfigurations.put("courses", 
            defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Course list cache - 30 minutes TTL
        cacheConfigurations.put("course-list", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Users cache - 30 minutes TTL
        cacheConfigurations.put("users", 
            defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Parts cache - 1 hour TTL
        cacheConfigurations.put("parts", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Question groups cache - 1 hour TTL
        cacheConfigurations.put("question-groups", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Enrollments cache - 15 minutes TTL (more dynamic)
        cacheConfigurations.put("enrollments", 
            defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Custom key generator for cache keys
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(target.getClass().getSimpleName()).append(".");
            key.append(method.getName()).append("(");
            for (int i = 0; i < params.length; i++) {
                key.append(params[i]);
                if (i < params.length - 1) {
                    key.append(",");
                }
            }
            key.append(")");
            return key.toString();
        };
    }

    /**
     * Custom cache error handler to prevent cache failures from breaking the app
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("Cache GET error - cache: {}, key: {}, error: {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                log.error("Cache PUT error - cache: {}, key: {}, error: {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                log.error("Cache EVICT error - cache: {}, key: {}, error: {}", 
                    cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                log.error("Cache CLEAR error - cache: {}, error: {}", 
                    cache.getName(), exception.getMessage());
            }
        };
    }
}
