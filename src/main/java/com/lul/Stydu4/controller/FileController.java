package com.lul.Stydu4.controller;

import com.lul.Stydu4.dto.response.ApiResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.service.IFileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final IFileStorageService fileStorageService;

    /**
     * Upload image file
     */
    @PostMapping("/upload/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FileEntity>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subFolder", defaultValue = "general") String subFolder,
            @RequestParam(value = "description", required = false) String description
    ) {
        log.info("Uploading image: {}, subFolder: {}", file.getOriginalFilename(), subFolder);

        FileEntity savedFile = fileStorageService.storeFile(file, FileType.IMAGE, subFolder);

        if (description != null) {
            savedFile.setDescription(description);
        }

        return ResponseEntity.ok(ApiResponse.<FileEntity>builder()
                .code(1000)
                .message("Image uploaded successfully")
                .result(savedFile)
                .build());
    }

    /**
     * Upload audio file
     */
    @PostMapping("/upload/audio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FileEntity>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subFolder", defaultValue = "general") String subFolder,
            @RequestParam(value = "description", required = false) String description
    ) {
        log.info("Uploading audio: {}, subFolder: {}", file.getOriginalFilename(), subFolder);

        FileEntity savedFile = fileStorageService.storeFile(file, FileType.AUDIO, subFolder);

        if (description != null) {
            savedFile.setDescription(description);
        }

        return ResponseEntity.ok(ApiResponse.<FileEntity>builder()
                .code(1000)
                .message("Audio uploaded successfully")
                .result(savedFile)
                .build());
    }

    /**
     * Download/Stream file by ID
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileId,
            HttpServletRequest request
    ) {
        log.info("Downloading file: {}", fileId);

        Resource resource = fileStorageService.loadFileAsResource(fileId);

        // Determine content type
        String contentType = null;
        try {
            contentType = request.getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn("Could not determine file type for: {}", fileId);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Delete file
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileId) {
        log.info("Deleting file: {}", fileId);

        fileStorageService.deleteFile(fileId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("File deleted successfully")
                .build());
    }

    /**
     * Get all files by type
     */
    @GetMapping("/type/{fileType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FileEntity>>> getFilesByType(
            @PathVariable FileType fileType
    ) {
        log.info("Getting files by type: {}", fileType);

        List<FileEntity> files = fileStorageService.getFilesByType(fileType);

        return ResponseEntity.ok(ApiResponse.<List<FileEntity>>builder()
                .code(1000)
                .message("Files retrieved successfully")
                .result(files)
                .build());
    }

    /**
     * Check if file exists
     */
    @GetMapping("/{fileId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> fileExists(@PathVariable String fileId) {
        boolean exists = fileStorageService.fileExists(fileId);

        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(1000)
                .message(exists ? "File exists" : "File not found")
                .result(exists)
                .build());
    }
}
