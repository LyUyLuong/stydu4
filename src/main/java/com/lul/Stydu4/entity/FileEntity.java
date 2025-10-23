package com.lul.Stydu4.entity;

import com.lul.Stydu4.enums.FileType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "files")
public class FileEntity extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 255)
    private String originalFilename;  // avatar.jpg

    @Column(nullable = false, length = 255)
    private String storedFilename;    // uuid-avatar.jpg

    @Column(nullable = false, length = 500)
    private String filePath;          // /images/questions/2025/10/22/uuid-avatar.jpg

    @Column(nullable = false, length = 500)
    private String fileUrl;           // http://localhost:8080/api/v1/files/uuid

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileType fileType;        // IMAGE, AUDIO

    @Column(nullable = false)
    private Long fileSize;            // bytes

    @Column(nullable = false, length = 100)
    private String contentType;       // image/jpeg, audio/mpeg

    @Column(columnDefinition = "TEXT")
    private String description;       // Optional description
}
