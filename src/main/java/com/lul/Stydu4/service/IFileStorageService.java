package com.lul.Stydu4.service;

import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileStorageService {

    /**
     * Store file and return file metadata
     */
    FileEntity storeFile(MultipartFile file, FileType fileType, String subFolder);

    /**
     * Load file as Resource for download/streaming
     */
    Resource loadFileAsResource(String fileId);

    /**
     * Delete file from filesystem and database
     */
    void deleteFile(String fileId);

    /**
     * Check if file exists
     */
    boolean fileExists(String fileId);

    /**
     * Get file URL
     */
    String getFileUrl(String fileId);

    /**
     * Get all files by type
     */
    List<FileEntity> getFilesByType(FileType fileType);
}
