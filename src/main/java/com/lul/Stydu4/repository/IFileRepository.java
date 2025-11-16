package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFileRepository extends JpaRepository<FileEntity, String> {

    List<FileEntity> findByFileType(FileType fileType);
    
    Page<FileEntity> findByFileType(FileType fileType, Pageable pageable);

    List<FileEntity> findByOriginalFilenameContaining(String filename);

    boolean existsByFilePath(String filePath);
    
    /**
     * Check if a file exists in database by its stored name (filename in storage)
     * Used by FileCleanupScheduler to identify orphaned files
     * 
     * @param storedFilename The filename stored in the file system
     * @return true if file record exists in database
     */
    boolean existsByStoredFilename(String storedFilename);
}
