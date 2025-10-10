package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
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
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestServiceImpl implements ITestService {

    ITestRepository testRepository;
    IPartTestRepository partTestRepository;
    TestMapper testMapper;
    PartTestMapper partTestMapper;

    @Override
    public TestDetailResponse create(TestCreationRequest request) {
        TestEntity entity = testMapper.toTestEntity(request);
        return testMapper.toTestResponse(testRepository.save(entity));
    }

    @Transactional
    @Override
    public TestDetailResponse update(String testId, TestUpdateRequest request) {
        TestEntity existing = testRepository.findById(testId)
                .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));

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

}
