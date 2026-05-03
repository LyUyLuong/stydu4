package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.entity.FileEntity;
import com.lul.Stydu4.enums.FileType;
import com.lul.Stydu4.repository.IFileRepository;
import com.lul.Stydu4.repository.specification.TestSpecification;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.PartTestMapper;
import com.lul.Stydu4.mapper.TestMapper;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.ITestRepository;
import com.lul.Stydu4.service.IFileStorageService;
import com.lul.Stydu4.service.ITestService;
import com.lul.Stydu4.util.SlugHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.lul.Stydu4.util.EnumValidator.validateAndConvert;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestServiceImpl implements ITestService {

    ITestRepository testRepository;
    IPartTestRepository partTestRepository;
    TestMapper testMapper;
    PartTestMapper partTestMapper;

    IFileStorageService fileStorageService;
    IFileRepository fileRepository;

    PartTestHydrator partTestHydrator;

    @Override
    public TestDetailResponse create(TestCreationRequest request) {
        TestEntity entity = testMapper.toTestEntity(request);

        TestType testType = validateAndConvert(
                request.getType(),
                TestType.class,
                ErrorCode.INVALID_TEST_TYPE
        );
        entity.setType(testType);

        if (request.getAudioId() != null && !request.getAudioId().isBlank()) {
            // Tìm FileEntity từ audioId trong request
            FileEntity audioFile = fileRepository.findById(request.getAudioId())
                    .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND)); // Ví dụ

            // Gán file audio này cho test entity
            entity.setAudio(audioFile);
        }

        // Auto-generate slug if not provided or empty
        if (entity.getSlug() == null || entity.getSlug().trim().isEmpty()) {
            entity.setSlug(SlugHelper.toUniqueSlug(entity.getName()));
        } else {
            // Normalize provided slug
            entity.setSlug(SlugHelper.toSlug(entity.getSlug()));
        }

        return testMapper.toTestResponse(testRepository.save(entity));
    }

    @Override
    @Transactional
    public TestDetailResponse createWithAudio(TestCreationRequest request, MultipartFile audio) {
        log.info("Creating test with audio file: {}",
                audio != null ? audio.getOriginalFilename() : "none");

        TestEntity entity = testMapper.toTestEntity(request);
        TestType testType = validateAndConvert(
                request.getType(),
                TestType.class,
                ErrorCode.INVALID_TEST_TYPE
        );
        entity.setType(testType);

        // Auto-generate slug if not provided or empty
        if (entity.getSlug() == null || entity.getSlug().trim().isEmpty()) {
            entity.setSlug(SlugHelper.toUniqueSlug(entity.getName()));
        } else {
            // Normalize provided slug
            entity.setSlug(SlugHelper.toSlug(entity.getSlug()));
        }

        // Upload audio file if provided
        if (audio != null && !audio.isEmpty()) {
            FileEntity audioFile = fileStorageService.storeFile(audio, FileType.AUDIO, "tests");
            entity.setAudio(audioFile);
            log.info("Audio uploaded with ID: {}", audioFile.getId());
        }

        TestEntity saved = testRepository.save(entity);
        return testMapper.toTestResponse(saved);
    }

    @Transactional
    @Override
    @CacheEvict(value = {"test-details", "tests"}, key = "#testId")
    public TestDetailResponse update(String testId, TestUpdateRequest request) {
        log.info("Updating test and invalidating cache: {}", testId);
        TestEntity existing = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        if (request.getType() != null) {
            TestType testType = validateAndConvert(
                    request.getType(),
                    TestType.class,
                    ErrorCode.INVALID_TEST_TYPE
            );
            existing.setType(testType);
        }


        // Cập nhật các trường đơn lẻ; null sẽ bị bỏ qua theo cấu hình mapper
        testMapper.updateTestEntityFromRequest(request, existing);

        // Auto-update slug if slug is empty
        if (request.getSlug() == null || request.getSlug().trim().isEmpty()) {
            // Generate slug from new name if provided, otherwise use existing name
            String nameToUse = (request.getName() != null && !request.getName().trim().isEmpty()) 
                    ? request.getName() 
                    : existing.getName();
            if (nameToUse != null && !nameToUse.trim().isEmpty()) {
                existing.setSlug(SlugHelper.toUniqueSlug(nameToUse));
            }
        } else {
            // Normalize provided slug
            existing.setSlug(SlugHelper.toSlug(request.getSlug()));
        }

        // Ngữ nghĩa:
        // - partTestIds == null  -> KHÔNG thay đổi quan hệ parts
        // - partTestIds isEmpty  -> XÓA toàn bộ liên kết parts
        // - partTestIds có phần tử -> Gán đúng danh sách parts theo ids
        List<String> partIds = request.getPartTestIds();
        if (partIds != null) {
            Set<String> newIds = new HashSet<>(partIds);

            if (existing.getPartTestEntities() == null) {
                existing.setPartTestEntities(new ArrayList<>());
            }

            // Gỡ các part không còn trong danh sách mới
            List<PartTestEntity> current = new ArrayList<>(existing.getPartTestEntities());
            for (PartTestEntity p : current) {
                if (!newIds.contains(p.getId())) {
                    p.setTestEntity(null);              // cập nhật phía sở hữu
                    existing.getPartTestEntities().remove(p); // đồng bộ phía nghịch đảo
                }
            }

            // Gắn các part theo danh sách mới (kể cả danh sách rỗng để clear)
            List<PartTestEntity> newParts = partTestRepository.findAllById(newIds);
            for (PartTestEntity p : newParts) {
                if (p.getTestEntity() != existing) {
                    p.setTestEntity(existing);          // phía sở hữu ManyToOne
                }
                if (!existing.getPartTestEntities().contains(p)) {
                    existing.getPartTestEntities().add(p); // đồng bộ phía OneToMany
                }
            }
        }

        TestEntity saved = testRepository.save(existing);
        return testMapper.toTestResponse(saved);
    }

    @Override
    @Transactional
    public TestDetailResponse updateTestAudio(String testId, MultipartFile audio) {
        log.info("Updating audio for test: {}", testId);

        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        // Delete old audio if exists
        if (test.getAudio() != null) {
            String oldAudioId = test.getAudio().getId();
            test.setAudio(null);
            testRepository.save(test);

            try {
                fileStorageService.deleteFile(oldAudioId);
                log.info("Old audio deleted: {}", oldAudioId);
            } catch (Exception e) {
                log.warn("Failed to delete old audio: {}", oldAudioId, e);
            }
        }

        // Upload new audio
        if (audio != null && !audio.isEmpty()) {
            FileEntity newAudio = fileStorageService.storeFile(audio, FileType.AUDIO, "tests");
            test.setAudio(newAudio);
            log.info("New audio uploaded with ID: {}", newAudio.getId());
        }

        TestEntity saved = testRepository.save(test);
        return testMapper.toTestResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "test-details", key = "#testId")
    public TestDetailResponse getTestById(String testId) {
        log.info("Loading test from database (cache miss): {}", testId);

        // Q1: test + audio (1 row, 1 LEFT JOIN)
        TestEntity test = testRepository.findByIdWithAudio(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        // Q2: parts của test (phẳng, không nested)
        List<PartTestEntity> parts =
                partTestRepository.findByTestEntityIdOrderByCreatedDateAsc(testId);

        if (!parts.isEmpty()) {
            // Q3-Q6: hydrate parts (questions + groups + answers) trong 4 query phẳng
            partTestHydrator.hydrate(parts);
        }

        // Gắn lại vào aggregate root rồi để mapper trả response
        test.setPartTestEntities(parts);
        return testMapper.toTestResponse(test);
    }

    @Override
    @CacheEvict(value = {"test-details", "tests"}, key = "#testId")
    public void deleteTest(String testId) {
        log.info("Deleting test and invalidating cache: {}", testId);
        testRepository.deleteById(testId);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<TestSummaryResponse> getAllTests(int page, int size) {
        int pageNo = page > 0 ? page - 1 : 0;
        int pageSize = Math.min(size, 100);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<TestEntity> tests = testRepository.findAllBy(pageable);

        List<TestSummaryResponse> testSummaries = tests.getContent().stream()
                .map(testMapper::toTestSummary)
                .toList();

        return PageResponse.<TestSummaryResponse>builder()
                .currentPage(pageNo + 1)
                .totalPages(tests.getTotalPages())
                .totalElements(tests.getTotalElements())
                .pageSize(pageSize)
                .data(testSummaries)
                .build();
    }



    @Transactional(readOnly = true)
    @Override
    public PageResponse<TestSummaryResponse> searchTests(TestSearchRequest req, Pageable pageable) {
        int idx = Math.max(0, req.getPage() - 1);
        int size = Math.min(req.getSize(), 100);
        Sort sort = sanitizeSort(pageable.getSort());
        Pageable pg = PageRequest.of(idx, size, sort);

        long t0 = System.nanoTime();
        Page<TestEntity> page = testRepository.findAll(
                TestSpecification.buildSpecification(req), pg);
        long t1 = System.nanoTime();

        // Bulk count parts
        var ids = page.getContent().stream().map(TestEntity::getId).toList();

        Map<String, Long> counts;
        if (ids.isEmpty()) {
            counts = Collections.emptyMap(); // tránh gọi repo với []
        } else {
            counts = partTestRepository.countByTestIds(ids).stream()
                    .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        }

        var data = page.getContent().stream().map(e -> {
            var dto = testMapper.toTestSummary(e);
            dto.setPartsCount(counts.getOrDefault(e.getId(),0L).intValue());
            return dto;
        }).toList();

        log.info("searchTests latency={}ms, page={}, size={}, total={}",
                (t1-t0)/1_000_000, idx+1, size, page.getTotalElements());

        return PageResponse.<TestSummaryResponse>builder()
                .data(data)
                .currentPage(idx+1)
                .pageSize(size)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "type", "status", "createdDate", "modifiedDate"
    );

    /**
     * Sanitize sort: whitelist fields và set default nếu empty
     */
    private Sort sanitizeSort(Sort sort) {
        if (sort.isUnsorted()) {
            log.info("No sort provided, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        // Filter chỉ giữ các field hợp lệ
        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> {
                    boolean valid = ALLOWED_SORT_FIELDS.contains(order.getProperty());
                    if (!valid) {
                        log.warn("Invalid sort field '{}' detected, skipping", order.getProperty());
                    }
                    return valid;
                })
                .toList();

        // Nếu tất cả field đều invalid, dùng default
        if (validOrders.isEmpty()) {
            log.warn("All sort fields were invalid, using default: createdDate DESC");
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }

        return Sort.by(validOrders);
    }



}
