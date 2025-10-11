package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.dto.request.Test.TestSpecification;
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
import com.lul.Stydu4.service.ITestService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

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


    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "type", "status", "createdDate", "modifiedDate"
    );




    @Override
    public TestDetailResponse create(TestCreationRequest request) {
        TestEntity entity = testMapper.toTestEntity(request);

        TestType testType = validateAndConvert(
                request.getType(),
                TestType.class,
                ErrorCode.INVALID_TEST_TYPE
        );
        entity.setType(testType);

        return testMapper.toTestResponse(testRepository.save(entity));
    }

    @Transactional
    @Override
    public TestDetailResponse update(String testId, TestUpdateRequest request) {
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
    public TestDetailResponse getTestById(String testId) {
        TestEntity test = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

        TestDetailResponse testDetailResponse = testMapper.toTestResponse(test);
        List<PartTestDetailResponse> partTestDetailRespons = test.getPartTestEntities().stream().map(partTestMapper::toPartTestResponse).toList();
        testDetailResponse.setParts(partTestDetailRespons);

        return testDetailResponse;
    }

    @Override
    public void deleteTest(String testId) {
        testRepository.deleteById(testId);
    }

    @Override
    public PageResponse<TestSummaryResponse> getAllTests(int page, int size) {

        int pageNo = page > 0 ? page - 1 : 0;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        Pageable pageable = PageRequest.of(pageNo, size,sort);

        Page<TestEntity> tests = testRepository.findAllBy(pageable);

        List<TestSummaryResponse> testSummaries = tests.getContent().stream().map(testMapper::toTestSummary).toList();

        return PageResponse.<TestSummaryResponse>builder()
                .currentPage(page)
                .totalPages(tests.getTotalPages())
                .totalElements(tests.getTotalElements())
                .pageSize(size)
                .data(testSummaries)
                .build();
    }



    @Override
    public PageResponse<TestSummaryResponse> searchTests(TestSearchRequest request, Pageable pageable) {

        log.info("Searching tests with request: {}", request);
        log.info("Original pageable: {}", pageable);

        // 1. Convert page từ 1-indexed (user) → 0-indexed (database)
        int pageIndex = request.getPage() > 0 ? request.getPage() - 1 : 0;

        // 2. Sanitize sort (whitelist + default)
        Sort sanitizedSort = sanitizeSort(pageable.getSort());
        log.info("Sanitized sort: {}", sanitizedSort);

        // 3. Rebuild pageable với page và sort đã xử lý
        Pageable sanitizedPageable = PageRequest.of(
                pageIndex,
                request.getSize(),
                sanitizedSort
        );

        // 4. Build specification từ filter fields
        Specification<TestEntity> spec = TestSpecification.buildSpecification(request);

        // 5. Query database
        Page<TestEntity> page = testRepository.findAll(spec, sanitizedPageable);

        log.info("Found {} tests, total elements: {}", page.getNumberOfElements(), page.getTotalElements());

        // 6. Map Entity → DTO
        List<TestSummaryResponse> data = page.getContent().stream()
                .map(testMapper::toTestSummary)
                .toList();

        // 7. Build response (convert page lại sang 1-indexed cho user)
        return PageResponse.<TestSummaryResponse>builder()
                .data(data)
                .currentPage(pageIndex + 1)
                .pageSize(request.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

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
