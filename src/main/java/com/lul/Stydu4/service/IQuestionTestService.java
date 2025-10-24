package com.lul.Stydu4.service;


import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;

import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IQuestionTestService {
    QuestionTestDetailResponse create(QuestionTestCreateRequest request);

    QuestionTestDetailResponse createWithFiles(
            QuestionTestCreateRequest request,
            MultipartFile audio,
            MultipartFile image
    );

    PageResponse<QuestionTestSummaryResponse> getAllQuestionTests(int page, int size);

    PageResponse<QuestionTestSummaryResponse> searchQuestionTests(
            QuestionTestSearchRequest request,
            Pageable pageable
    );

    QuestionTestDetailResponse getQuestionTestById(String questionTestId);

    QuestionTestDetailResponse update(String questionTestId, QuestionTestUpdateRequest request);

    void deleteQuestionTest(String questionTestId);

    QuestionTestDetailResponse updateQuestionAudio(String questionTestId, MultipartFile audio);

    QuestionTestDetailResponse updateQuestionImage(String questionTestId, MultipartFile image);
}
