package com.lul.Stydu4.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Tự động log các method trong Controller và Service
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Log các method trong Controller
     */
    @Around("execution(* com.lul.Stydu4.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        log.info("==> Controller: {}", methodName);
        long start = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("<== Controller: {} completed in {}ms", methodName, duration);
            return result;
        } catch (Exception e) {
            log.error("!!! Controller: {} failed - {}", methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Log các method trong Service
     */
    @Around("execution(* com.lul.Stydu4.service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            
            if (duration > 1000) {
                log.warn("Service: {} took {}ms (slow!)", methodName, duration);
            } else {
                log.debug("Service: {} - {}ms", methodName, duration);
            }
            return result;
        } catch (Exception e) {
            log.error("Service: {} error - {}", methodName, e.getMessage());
            throw e;
        }
    }
}
