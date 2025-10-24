package com.lul.Stydu4.service;


import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupSearchRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.PageResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IQuestionGroupService {
    QuestionGroupDetailResponse create(QuestionGroupCreateRequest request);

    QuestionGroupDetailResponse createWithFiles(
            QuestionGroupCreateRequest request,
            MultipartFile audio,
            MultipartFile image
    );

    PageResponse<QuestionGroupSummaryResponse> getAllQuestionGroups(int page, int size);

    PageResponse<QuestionGroupSummaryResponse> searchQuestionGroups(
            QuestionGroupSearchRequest request,
            Pageable pageable
    );

    QuestionGroupDetailResponse getQuestionGroupById(String questionGroupId);

    QuestionGroupDetailResponse update(String questionGroupId, QuestionGroupUpdateRequest request);

    void deleteQuestionGroup(String questionGroupId);

    QuestionGroupDetailResponse updateGroupAudio(String questionGroupId, MultipartFile audio);

    QuestionGroupDetailResponse updateGroupImage(String questionGroupId, MultipartFile image);
}
