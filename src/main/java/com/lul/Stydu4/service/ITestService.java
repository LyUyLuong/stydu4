package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;

import java.util.List;

public interface ITestService {

    TestDetailResponse create(TestCreationRequest request);
    TestDetailResponse update(String testId, TestUpdateRequest request);
    TestDetailResponse getTestById(String testId);

    void deleteTest(String testId);

    PageResponse<TestSummaryResponse> getAllTests(int page, int size);
}
