package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.PartTestMapper;
import com.lul.Stydu4.repository.IPartTestRepository;
import com.lul.Stydu4.repository.ITestRepository;
import com.lul.Stydu4.service.IPartTestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartTestServiceImpl implements IPartTestService {

    IPartTestRepository partTestRepository;
    ITestRepository testRepository;
    PartTestMapper partTestMapper;

    @Override
    public PartTestDetailResponse create(PartTestCreationRequest request) {
        // Map basic fields
        PartTestEntity entity = partTestMapper.toPartTestEntity(request);

        // Gán testEntity nếu testId hợp lệ
        if (request.getTestId() != null) {
            TestEntity test = testRepository.findById(request.getTestId())
                    .orElseThrow(() -> new RuntimeException("Test not found with id: " + request.getTestId()));
            entity.setTestEntity(test);
        }

        return partTestMapper.toPartTestResponse(partTestRepository.save(entity));
    }

    @Override
    public PartTestDetailResponse update(String partTestID, PartTestUpdateRequest request) {
        Optional<PartTestEntity> optional = partTestRepository.findById(partTestID);
        if (optional.isEmpty()) {
            throw new RuntimeException("PartTest not found with id: " + partTestID);
        }

        PartTestEntity existing = optional.get();
        partTestMapper.updatePartTestEntityFromRequest(request, existing);
        return partTestMapper.toPartTestResponse(partTestRepository.save(existing));
    }

    @Override
    public PageResponse<PartTestSummaryResponse> getAllPartTests(int page, int size) {

        int pageNo = page > 0 ? page - 1 : 0;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        Pageable pageable = PageRequest.of(pageNo,size,sort);


        Page<PartTestEntity> partTestEntities = partTestRepository.findAllBy(pageable);

        List<PartTestSummaryResponse> partTestSummaries = partTestEntities.getContent().stream().map(partTestMapper::toPartTestSummary).toList();

        return PageResponse.<PartTestSummaryResponse>builder()
                .currentPage(page)
                .totalPages(partTestEntities.getTotalPages())
                .totalElements(partTestEntities.getTotalElements())
                .pageSize(size)
                .data(partTestSummaries)
                .build();
    }

    @Override
    public PartTestDetailResponse getPartTestById(String partTestId) {
        PartTestEntity partTest = partTestRepository.findById(partTestId)
                .orElseThrow(() -> new AppException(ErrorCode.PART_TEST_NOT_FOUND));
        return partTestMapper.toPartTestResponse(partTest);
    }

    @Override
    public void deletePartTest(String partTestId) {
        partTestRepository.deleteById(partTestId);
    }
}
