package com.lul.Stydu4.scheduler;

import com.lul.Stydu4.repository.IFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * File Cleanup Scheduler
 * 
 * Automatically cleans up orphaned files that exist in storage but not in database.
 * Runs daily at 2:00 AM to minimize impact on users.
 * 
 * Orphaned files can occur when:
 * - File upload succeeds but DB save fails
 * - File entity deleted from DB but file deletion fails
 * - Application crashes during file operations
 * 
 * @author Stydu4 Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileCleanupScheduler {

    private final IFileRepository fileRepository;
    
    @Value("${file.storage.location}")
    private String storageLocation;

    /**
     * Clean up orphaned files daily at 2:00 AM
     * 
     * Cron expression: "0 0 2 * * ?" means:
     * - Seconds: 0
     * - Minutes: 0
     * - Hour: 2 (2 AM)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: ? (any day)
     * 
     * Zone: Server timezone (adjust for production)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(readOnly = true)
    public void cleanupOrphanedFiles() {
        log.info("===========================================");
        log.info("Starting scheduled orphaned file cleanup...");
        log.info("===========================================");
        
        LocalDateTime startTime = LocalDateTime.now();
        AtomicInteger totalFiles = new AtomicInteger(0);
        AtomicInteger orphanedFiles = new AtomicInteger(0);
        AtomicInteger deletedFiles = new AtomicInteger(0);
        AtomicInteger failedDeletions = new AtomicInteger(0);
        
        try {
            Path storageRoot = Paths.get(storageLocation);
            
            // Check if storage directory exists
            if (!Files.exists(storageRoot)) {
                log.warn("Storage directory does not exist: {}", storageLocation);
                return;
            }
            
            log.info("Scanning storage directory: {}", storageLocation);
            
            // Walk through all files in storage directory
            try (Stream<Path> paths = Files.walk(storageRoot)) {
                paths.filter(Files::isRegularFile)
                     .forEach(filePath -> {
                         totalFiles.incrementAndGet();
                         
                         String filename = filePath.getFileName().toString();
                         String relativePath = storageRoot.relativize(filePath).toString();
                         
                         // Check if file exists in database by stored name
                         boolean existsInDb = fileRepository.existsByStoredFilename(filename);
                         
                         if (!existsInDb) {
                             orphanedFiles.incrementAndGet();
                             log.warn("Found orphaned file: {} (Path: {})", filename, relativePath);
                             
                             // Attempt to delete orphaned file
                             try {
                                 Files.delete(filePath);
                                 deletedFiles.incrementAndGet();
                                 log.info("✓ Deleted orphaned file: {}", filename);
                             } catch (IOException e) {
                                 failedDeletions.incrementAndGet();
                                 log.error("✗ Failed to delete orphaned file {}: {}", 
                                     filename, e.getMessage());
                             }
                         } else {
                             log.debug("File exists in DB: {}", filename);
                         }
                     });
                     
            } catch (IOException e) {
                log.error("Error walking through storage directory: {}", e.getMessage(), e);
            }
            
            // Calculate duration
            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            
            // Print summary
            log.info("===========================================");
            log.info("File Cleanup Summary:");
            log.info("- Started at: {}", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("- Completed at: {}", endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("- Duration: {} seconds", durationSeconds);
            log.info("- Total files scanned: {}", totalFiles.get());
            log.info("- Orphaned files found: {}", orphanedFiles.get());
            log.info("- Files successfully deleted: {}", deletedFiles.get());
            log.info("- Failed deletions: {}", failedDeletions.get());
            log.info("- Storage saved: ~{} MB", calculateStorageSaved(deletedFiles.get()));
            log.info("===========================================");
            
            // Log warning if there were many orphaned files
            if (orphanedFiles.get() > 10) {
                log.warn("⚠️ High number of orphaned files detected ({})! Investigate file management logic.", 
                    orphanedFiles.get());
            }
            
            // Log error if many deletions failed
            if (failedDeletions.get() > 0) {
                log.error("⚠️ {} file deletions failed! Check file permissions and disk space.", 
                    failedDeletions.get());
            }
            
        } catch (Exception e) {
            log.error("Critical error during file cleanup: {}", e.getMessage(), e);
        }
        
        log.info("Scheduled file cleanup completed.");
    }
    
    /**
     * Manual cleanup trigger (for testing or emergency cleanup)
     * Can be called via admin endpoint or scheduled externally
     */
    public void triggerManualCleanup() {
        log.info("Manual cleanup triggered by admin");
        cleanupOrphanedFiles();
    }
    
    /**
     * Estimate storage saved based on average file size
     * Assumes average file size of 1MB (adjust based on your app)
     */
    private long calculateStorageSaved(int filesDeleted) {
        long averageFileSizeMB = 1; // Adjust based on your average file size
        return filesDeleted * averageFileSizeMB;
    }
}
