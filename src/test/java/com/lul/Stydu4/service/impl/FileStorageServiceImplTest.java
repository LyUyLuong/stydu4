package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.configuration.FileStorageProperties;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageServiceImpl Tests")
class FileStorageServiceImplTest {

    @Mock
    private FileStorageProperties fileStorageProperties;

    @Mock
    private IFileRepository fileRepository;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    private FileStorageProperties.Storage storageConfig;
    private FileStorageProperties.Upload uploadConfig;
    private FileStorageProperties.FileConfig imageConfig;
    private FileStorageProperties.FileConfig audioConfig;

    @BeforeEach
    void setUp() {
        // Setup storage config
        storageConfig = new FileStorageProperties.Storage();
        storageConfig.setLocation(tempDir.toString());

        // Setup image config
        imageConfig = new FileStorageProperties.FileConfig();
        imageConfig.setAllowedExtensions(List.of("jpg", "jpeg", "png", "gif"));
        imageConfig.setMaxSize("5MB");

        // Setup audio config
        audioConfig = new FileStorageProperties.FileConfig();
        audioConfig.setAllowedExtensions(List.of("mp3", "wav", "m4a"));
        audioConfig.setMaxSize("10MB");

        // Setup upload config
        uploadConfig = new FileStorageProperties.Upload();
        uploadConfig.setImages(imageConfig);
        uploadConfig.setAudio(audioConfig);

        // Mock servlet request for URL building
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        lenient().when(fileStorageProperties.getStorage()).thenReturn(storageConfig);
        lenient().when(fileStorageProperties.getUpload()).thenReturn(uploadConfig);
    }

    @Nested
    @DisplayName("storeFile Tests")
    class StoreFileTests {

        @Test
        @DisplayName("Should store image file successfully")
        void storeFile_ValidImage_Success() throws IOException {
            // GIVEN
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            FileEntity savedFile = FileEntity.builder()
                    .id("file-123")
                    .originalFilename("test-image.jpg")
                    .storedFilename("uuid-123.jpg")
                    .fileType(FileType.IMAGE)
                    .fileSize(imageFile.getSize())
                    .contentType("image/jpeg")
                    .build();

            when(fileRepository.save(any(FileEntity.class))).thenReturn(savedFile);

            // WHEN
            FileEntity result = fileStorageService.storeFile(imageFile, FileType.IMAGE, "courses");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("file-123");
            assertThat(result.getOriginalFilename()).isEqualTo("test-image.jpg");
            assertThat(result.getFileType()).isEqualTo(FileType.IMAGE);

            ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
            verify(fileRepository).save(captor.capture());
            
            FileEntity capturedFile = captor.getValue();
            assertThat(capturedFile.getOriginalFilename()).isEqualTo("test-image.jpg");
            assertThat(capturedFile.getFileType()).isEqualTo(FileType.IMAGE);
            assertThat(capturedFile.getContentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("Should store audio file successfully")
        void storeFile_ValidAudio_Success() throws IOException {
            // GIVEN
            MockMultipartFile audioFile = new MockMultipartFile(
                    "file",
                    "test-audio.mp3",
                    "audio/mpeg",
                    "test audio content".getBytes()
            );

            FileEntity savedFile = FileEntity.builder()
                    .id("file-456")
                    .originalFilename("test-audio.mp3")
                    .storedFilename("uuid-456.mp3")
                    .fileType(FileType.AUDIO)
                    .fileSize(audioFile.getSize())
                    .contentType("audio/mpeg")
                    .build();

            when(fileRepository.save(any(FileEntity.class))).thenReturn(savedFile);

            // WHEN
            FileEntity result = fileStorageService.storeFile(audioFile, FileType.AUDIO, "questions");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getFileType()).isEqualTo(FileType.AUDIO);
            assertThat(result.getOriginalFilename()).isEqualTo("test-audio.mp3");
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void storeFile_NullFile_ThrowException() {
            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.storeFile(null, FileType.IMAGE, "courses"))
                    .isInstanceOf(NullPointerException.class);

            verify(fileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void storeFile_EmptyFile_ThrowException() {
            // GIVEN
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile, FileType.IMAGE, "courses"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_EMPTY);

            verify(fileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when file extension is invalid")
        void storeFile_InvalidExtension_ThrowException() {
            // GIVEN
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "test content".getBytes()
            );

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.storeFile(invalidFile, FileType.IMAGE, "courses"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_INVALID_EXTENSION);

            verify(fileRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when file size exceeds limit")
        void storeFile_FileTooLarge_ThrowException() {
            // GIVEN
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB (exceeds 5MB limit)
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large-image.jpg",
                    "image/jpeg",
                    largeContent
            );

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.storeFile(largeFile, FileType.IMAGE, "courses"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_TOO_LARGE);

            verify(fileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("loadFileAsResource Tests")
    class LoadFileAsResourceTests {

        @Test
        @DisplayName("Should load file as resource successfully")
        void loadFileAsResource_ValidFile_Success() throws IOException {
            // GIVEN
            Path testFilePath = tempDir.resolve("images/courses/2024/01/15/test.jpg");
            Files.createDirectories(testFilePath.getParent());
            Files.write(testFilePath, "test content".getBytes());

            FileEntity fileEntity = FileEntity.builder()
                    .id("file-123")
                    .filePath("images/courses/2024/01/15/test.jpg")
                    .build();

            when(fileRepository.findById("file-123")).thenReturn(Optional.of(fileEntity));

            // WHEN
            Resource resource = fileStorageService.loadFileAsResource("file-123");

            // THEN
            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();

            verify(fileRepository).findById("file-123");
        }

        @Test
        @DisplayName("Should throw exception when file not found in database")
        void loadFileAsResource_FileNotInDatabase_ThrowException() {
            // GIVEN
            when(fileRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.loadFileAsResource("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

            verify(fileRepository).findById("invalid-id");
        }

        @Test
        @DisplayName("Should throw exception when file not found in filesystem")
        void loadFileAsResource_FileNotInFilesystem_ThrowException() {
            // GIVEN
            FileEntity fileEntity = FileEntity.builder()
                    .id("file-123")
                    .filePath("images/nonexistent/file.jpg")
                    .build();

            when(fileRepository.findById("file-123")).thenReturn(Optional.of(fileEntity));

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.loadFileAsResource("file-123"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete file successfully")
        void deleteFile_ValidFile_Success() throws IOException {
            // GIVEN
            Path testFilePath = tempDir.resolve("images/test.jpg");
            Files.createDirectories(testFilePath.getParent());
            Files.write(testFilePath, "test content".getBytes());

            FileEntity fileEntity = FileEntity.builder()
                    .id("file-123")
                    .filePath("images/test.jpg")
                    .build();

            when(fileRepository.findById("file-123")).thenReturn(Optional.of(fileEntity));
            doNothing().when(fileRepository).delete(fileEntity);

            // WHEN
            fileStorageService.deleteFile("file-123");

            // THEN
            assertThat(Files.exists(testFilePath)).isFalse();
            verify(fileRepository).findById("file-123");
            verify(fileRepository).delete(fileEntity);
        }

        @Test
        @DisplayName("Should throw exception when file not found in database")
        void deleteFile_FileNotInDatabase_ThrowException() {
            // GIVEN
            when(fileRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> fileStorageService.deleteFile("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

            verify(fileRepository).findById("invalid-id");
            verify(fileRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("fileExists Tests")
    class FileExistsTests {

        @Test
        @DisplayName("Should return true when file exists")
        void fileExists_FileExists_ReturnTrue() {
            // GIVEN
            when(fileRepository.existsById("file-123")).thenReturn(true);

            // WHEN
            boolean result = fileStorageService.fileExists("file-123");

            // THEN
            assertThat(result).isTrue();
            verify(fileRepository).existsById("file-123");
        }

        @Test
        @DisplayName("Should return false when file does not exist")
        void fileExists_FileNotExists_ReturnFalse() {
            // GIVEN
            when(fileRepository.existsById("invalid-id")).thenReturn(false);

            // WHEN
            boolean result = fileStorageService.fileExists("invalid-id");

            // THEN
            assertThat(result).isFalse();
            verify(fileRepository).existsById("invalid-id");
        }
    }

    @Nested
    @DisplayName("getFilesByType Tests")
    class GetFilesByTypeTests {

        @Test
        @DisplayName("Should return all files of specified type")
        void getFilesByType_Success() {
            // GIVEN
            FileEntity imageFile1 = FileEntity.builder()
                    .id("file-1")
                    .fileType(FileType.IMAGE)
                    .build();

            FileEntity imageFile2 = FileEntity.builder()
                    .id("file-2")
                    .fileType(FileType.IMAGE)
                    .build();

            when(fileRepository.findByFileType(FileType.IMAGE))
                    .thenReturn(List.of(imageFile1, imageFile2));

            // WHEN
            List<FileEntity> result = fileStorageService.getFilesByType(FileType.IMAGE);

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFileType()).isEqualTo(FileType.IMAGE);
            assertThat(result.get(1).getFileType()).isEqualTo(FileType.IMAGE);

            verify(fileRepository).findByFileType(FileType.IMAGE);
        }

        @Test
        @DisplayName("Should return empty list when no files of type exist")
        void getFilesByType_EmptyList() {
            // GIVEN
            when(fileRepository.findByFileType(FileType.AUDIO)).thenReturn(List.of());

            // WHEN
            List<FileEntity> result = fileStorageService.getFilesByType(FileType.AUDIO);

            // THEN
            assertThat(result).isEmpty();
            verify(fileRepository).findByFileType(FileType.AUDIO);
        }
    }
}
