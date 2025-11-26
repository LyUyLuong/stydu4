package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.configuration.FileStorageProperties;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IFileRepository;
import com.lul.Stydu4.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    private final FileStorageProperties fileStorageProperties;
    private final IFileRepository fileRepository;

    @Override
    @Transactional
    public FileEntity storeFile(MultipartFile file, FileType fileType, String subFolder) {
        log.info("Storing file: {}, type: {}, subFolder: {}",
                file.getOriginalFilename(), fileType, subFolder);

        // 1. Validate file
        validateFile(file, fileType);

        // 2. Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;

        // 3. Create directory structure: /images|audio/subfolder/yyyy/MM/dd/
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String typeFolder = fileType == FileType.IMAGE ? "images" : "audio";
        String relativePath = String.format("%s/%s/%s/%s", typeFolder, subFolder, dateFolder, storedFilename);

        // 4. Create full path
        Path targetLocation = Paths.get(fileStorageProperties.getStorage().getLocation())
                .resolve(relativePath)
                .normalize()
                .toAbsolutePath();

        try {
            // 5. Create directories if not exists
            Files.createDirectories(targetLocation.getParent());

            // 6. Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully at: {}", relativePath);

            // ✅ 7. Generate file ID and URL BEFORE saving
            String fileId = UUID.randomUUID().toString();
            String fileUrl = buildFileUrl(fileId);

            // ✅ 8. Save metadata to database WITH all fields set
            FileEntity fileEntity = FileEntity.builder()
                    .id(fileId)
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .filePath(relativePath)
                    .fileUrl(fileUrl)
                    .fileType(fileType)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            FileEntity savedFile = fileRepository.save(fileEntity);
            log.info("File metadata saved with ID: {} and URL: {}",
                    savedFile.getId(), savedFile.getFileUrl());

            return savedFile;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFilename, ex);
            throw new AppException(ErrorCode.FILE_STORE_FAILED);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileId) {
        log.info("Loading file as resource: {}", fileId);
        try {
            FileEntity fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

            Path filePath = Paths.get(fileStorageProperties.getStorage().getLocation())
                    .resolve(fileEntity.getFilePath())
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("File loaded successfully: {}", fileEntity.getFilePath());
                return resource;
            } else {
                log.error("File not found or not readable: {}", fileEntity.getFilePath());
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Could not load file: {}", fileId, ex);
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteFile(String fileId) {
        log.info("Deleting file: {}", fileId);
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        try {
            // 1. Delete from filesystem
            Path filePath = Paths.get(fileStorageProperties.getStorage().getLocation())
                    .resolve(fileEntity.getFilePath())
                    .normalize();

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted from filesystem: {}", fileEntity.getFilePath());
            } else {
                log.warn("File not found in filesystem: {}", fileEntity.getFilePath());
            }

            // 2. Delete from database
            fileRepository.delete(fileEntity);
            log.info("File metadata deleted from database: {}", fileId);

        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileEntity.getFilePath(), ex);
            throw new AppException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    @Override
    public boolean fileExists(String fileId) {
        return fileRepository.existsById(fileId);
    }

    @Override
    public String getFileUrl(String fileId) {
        return buildFileUrl(fileId);
    }

    @Override
    public List<FileEntity> getFilesByType(FileType fileType) {
        log.info("Getting all files by type: {}", fileType);
        return fileRepository.findByFileType(fileType);
    }
    
    @Override
    public PageResponse<FileEntity> getFilesByTypeWithPagination(FileType fileType, Pageable pageable) {
        log.info("Getting files by type: {} with pagination - page: {}, size: {}", 
                fileType, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<FileEntity> page = fileRepository.findByFileType(fileType, pageable);
        
        return PageResponse.<FileEntity>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent())
                .build();
    }

    // =============== PRIVATE HELPER METHODS ===============

    private String buildFileUrl(String fileId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileId)
                .toUriString();
    }

    private void validateFile(MultipartFile file, FileType fileType) {
        // 1. Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        // 2. Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String extension = getFileExtension(filename);
        FileStorageProperties.FileConfig config;

        switch (fileType) {
            case IMAGE:
                config = fileStorageProperties.getUpload().getImages();
                break;
            case AUDIO:
                config = fileStorageProperties.getUpload().getAudio();
                break;
            case VIDEO:
                config = fileStorageProperties.getUpload().getVideos();
                break;
            case DOCUMENT:
                config = fileStorageProperties.getUpload().getDocuments();
                break;
            default:
                config = fileStorageProperties.getUpload().getDocuments();
        }

        if (config != null && config.getAllowedExtensions() != null &&
                !config.getAllowedExtensions().contains(extension.toLowerCase())) {
            log.error("Invalid file extension: {}. Allowed: {}",
                    extension, config.getAllowedExtensions());
            throw new AppException(ErrorCode.FILE_INVALID_EXTENSION);
        }

        // 3. Check file size
        if (config.getMaxSize() != null) {
            long maxSize = parseSize(config.getMaxSize());
            if (file.getSize() > maxSize) {
                log.error("File size {} exceeds maximum allowed size {}",
                        file.getSize(), maxSize);
                throw new AppException(ErrorCode.FILE_TOO_LARGE);
            }
        }

        log.debug("File validation passed: {} ({} bytes)", filename, file.getSize());
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private long parseSize(String size) {
        size = size.toUpperCase().trim();
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        } else if (size.endsWith("GB")) {
            return Long.parseLong(size.replace("GB", "")) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(size);
    }
}
