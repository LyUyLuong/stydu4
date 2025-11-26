package com.lul.Stydu4.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to controller methods
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Maximum number of requests allowed within the time window
     */
    int maxRequests() default 5;
    
    /**
     * Time window in seconds
     */
    int timeWindowSeconds() default 60;
    
    /**
     * Rate limit key prefix (will be combined with user identifier)
     */
    String keyPrefix() default "rate_limit";
}
