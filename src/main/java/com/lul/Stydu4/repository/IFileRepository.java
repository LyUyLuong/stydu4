package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFileRepository extends JpaRepository<FileEntity, String> {

    List<FileEntity> findByFileType(FileType fileType);

    List<FileEntity> findByOriginalFilenameContaining(String filename);

    boolean existsByFilePath(String filePath);
}
