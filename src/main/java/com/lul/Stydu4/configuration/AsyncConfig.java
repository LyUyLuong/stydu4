package com.lul.Stydu4.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for background task processing
 * 
 * Configures separate thread pools for different types of async operations:
 * - Email sending (non-blocking, separate from main request threads)
 * - File processing
 * - Other background tasks
 * 
 * @author Stydu4 Team
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Thread pool executor for sending emails asynchronously
     * 
     * Configuration:
     * - Core Pool Size: 2 threads (minimum threads always alive)
     * - Max Pool Size: 5 threads (maximum threads under load)
     * - Queue Capacity: 100 (max queued tasks before rejection)
     * - Thread Name Prefix: "email-" for easy identification in logs
     * 
     * Why async email?
     * - Email sending is slow (network I/O)
     * - Don't block main transaction
     * - Don't rollback payment if email fails
     * - Better user experience (faster response)
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core threads always alive
        executor.setCorePoolSize(2);
        
        // Maximum threads under high load
        executor.setMaxPoolSize(5);
        
        // Queue capacity before rejecting tasks
        executor.setQueueCapacity(100);
        
        // Thread name prefix for identification
        executor.setThreadNamePrefix("email-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Timeout for awaiting termination
        executor.setAwaitTerminationSeconds(60);
        
        // Initialize the executor
        executor.initialize();
        
        log.info("Email Task Executor initialized - Core: 2, Max: 5, Queue: 100");
        
        return executor;
    }

    /**
     * Thread pool executor for file processing tasks
     * 
     * Configuration:
     * - Core Pool Size: 1 thread (file operations are less frequent)
     * - Max Pool Size: 3 threads
     * - Queue Capacity: 50
     */
    @Bean(name = "fileTaskExecutor")
    public Executor fileTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("file-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("File Task Executor initialized - Core: 1, Max: 3, Queue: 50");
        
        return executor;
    }

    /**
     * Default task executor for @Async without specific executor name
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Default Async Task Executor initialized - Core: 3, Max: 10, Queue: 200");
        
        return executor;
    }

    /**
     * Handle uncaught exceptions in async methods
     * 
     * This prevents silent failures - all async errors will be logged
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async method execution failed: {}.{}", 
                method.getDeclaringClass().getSimpleName(), 
                method.getName());
            log.error("Exception: {}", throwable.getMessage(), throwable);
            log.error("Parameters: {}", params);
        };
    }
}
