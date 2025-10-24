package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestSearchRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPartTestService {

    PartTestDetailResponse create(PartTestCreationRequest request);
    PartTestDetailResponse update(String partTestId, PartTestUpdateRequest request);

    PageResponse<PartTestSummaryResponse> getAllPartTests(int page, int size);

    PartTestDetailResponse getPartTestById(String partTestId);

    void deletePartTest(String partTestId);

    PageResponse<PartTestSummaryResponse> searchPartTests(@Valid PartTestSearchRequest request, Pageable pageable);
}
