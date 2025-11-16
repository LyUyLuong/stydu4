package com.lul.Stydu4.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Structured Logging với Context Fields
 * - Tự động log các method trong Controller và Service
 * - Thêm context: userId, requestId, method, duration
 * - Log có điều kiện: chỉ log slow operations
 * - Sử dụng SLF4J MDC (Mapped Diagnostic Context) để thêm context vào mọi log message
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_OPERATION_THRESHOLD_MS = 1000; // 1 second

    /**
     * Log các method trong Controller với context đầy đủ
     */
    @Around("execution(* com.lul.Stydu4.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String requestId = generateRequestId();
        
        // Add context to MDC (will be included in all logs)
        MDC.put("requestId", requestId);
        MDC.put("layer", "controller");
        
        try {
            // Add user context if authenticated
            addUserContext();
            
            // Add HTTP request details if available
            addHttpRequestContext();
            
            log.info("⟹ Request started: {}", methodName);
            long start = System.currentTimeMillis();
            
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - start;
            MDC.put("duration_ms", String.valueOf(duration));
            
            // Log based on performance
            if (duration > SLOW_OPERATION_THRESHOLD_MS) {
                log.warn("⚠️ Slow request: {} completed in {}ms (threshold: {}ms)", 
                        methodName, duration, SLOW_OPERATION_THRESHOLD_MS);
            } else {
                log.info("⟸ Request completed: {} in {}ms", methodName, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            MDC.put("error", e.getClass().getSimpleName());
            MDC.put("error_message", e.getMessage());
            log.error("❌ Request failed: {} - {}", methodName, e.getMessage(), e);
            throw e;
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /**
     * Log các method trong Service với điều kiện
     */
    @Around("execution(* com.lul.Stydu4.service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        MDC.put("layer", "service");
        MDC.put("method", methodName);
        
        long start = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            
            // Chỉ log slow operations hoặc errors
            if (duration > SLOW_OPERATION_THRESHOLD_MS) {
                MDC.put("duration_ms", String.valueOf(duration));
                log.warn("🐌 Slow service operation: {} took {}ms", methodName, duration);
            } else {
                // DEBUG level for normal operations to reduce noise
                log.debug("Service: {} completed in {}ms", methodName, duration);
            }
            
            return result;
            
        } catch (Exception e) {
            MDC.put("error", e.getClass().getSimpleName());
            log.error("Service error in {}: {}", methodName, e.getMessage());
            throw e;
        } finally {
            MDC.remove("layer");
            MDC.remove("method");
            MDC.remove("duration_ms");
            MDC.remove("error");
        }
    }

    /**
     * Thêm user context vào MDC
     */
    private void addUserContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                MDC.put("userId", username);
                MDC.put("authenticated", "true");
                
                // Add roles if available
                if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                    String roles = authentication.getAuthorities().stream()
                            .map(auth -> auth.getAuthority())
                            .reduce((a, b) -> a + "," + b)
                            .orElse("NONE");
                    MDC.put("roles", roles);
                }
            } else {
                MDC.put("userId", "anonymous");
                MDC.put("authenticated", "false");
            }
        } catch (Exception e) {
            // Ignore - authentication context might not be available
            MDC.put("userId", "unknown");
        }
    }

    /**
     * Thêm HTTP request context vào MDC
     */
    private void addHttpRequestContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                MDC.put("httpMethod", request.getMethod());
                MDC.put("requestUri", request.getRequestURI());
                
                // Add client IP
                String clientIp = getClientIpAddress(request);
                MDC.put("clientIp", clientIp);
                
                // Add User-Agent for analytics
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    MDC.put("userAgent", userAgent.length() > 100 ? userAgent.substring(0, 100) : userAgent);
                }
            }
        } catch (Exception e) {
            // Ignore - request context might not be available
        }
    }

    /**
     * Get real client IP address (handles proxy/load balancer)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs (take first one)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Generate unique request ID for tracing
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
