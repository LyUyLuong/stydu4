package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.entity.AnswerEntity;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import com.lul.Stydu4.repository.IAnswerRepository;
import com.lul.Stydu4.repository.IQuestionGroupRepository;
import com.lul.Stydu4.repository.IQuestionTestRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Nạp đầy questions/questionGroups/answers cho danh sách PartTestEntity bằng
 * 4 truy vấn phẳng — không Cartesian, không N+1, KHÔNG sửa DTO/response.
 *
 * Ý tưởng: thay vì để Hibernate lazy-load từng nhánh (dẫn tới hàng trăm SELECT)
 * hoặc fetch đồng thời nhiều bag (Cartesian / MultipleBagFetchException), ta
 * chủ động kéo từng tầng bằng IN (?,?,...) rồi lắp ráp trong bộ nhớ.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartTestHydrator {

    IQuestionTestRepository questionTestRepository;
    IQuestionGroupRepository questionGroupRepository;
    IAnswerRepository answerRepository;

    /**
     * Sau khi gọi xong, mỗi part trong list có:
     *   - questions: đầy đủ câu hỏi rời (kèm audio/image/answers)
     *   - questionGroups: đầy đủ group (kèm audio/image, questions, answers)
     *
     * Tổng cộng tối đa 4 query bất kể số part / group / câu hỏi.
     */
    public void hydrate(List<PartTestEntity> parts) {
        if (parts == null || parts.isEmpty()) return;

        List<String> partIds = parts.stream().map(PartTestEntity::getId).toList();

        // Q1: câu hỏi rời (questionGroupEntity IS NULL) thuộc các part
        List<QuestionTestEntity> standaloneQs =
                questionTestRepository.findStandaloneByPartIds(partIds);

        // Q2: tất cả group thuộc các part
        List<QuestionGroupEntity> groups =
                questionGroupRepository.findByPartIdsWithMedia(partIds);

        // Q3: tất cả câu hỏi thuộc các group
        List<String> groupIds = groups.stream().map(QuestionGroupEntity::getId).toList();
        List<QuestionTestEntity> groupQs = groupIds.isEmpty()
                ? Collections.emptyList()
                : questionTestRepository.findByGroupIdsWithMedia(groupIds);

        // Q4: tất cả đáp án của tất cả câu hỏi nói trên
        List<String> allQIds = Stream.concat(standaloneQs.stream(), groupQs.stream())
                .map(QuestionTestEntity::getId)
                .toList();

        Map<String, List<AnswerEntity>> answersByQ = allQIds.isEmpty()
                ? Collections.emptyMap()
                : answerRepository.findByQuestionIds(allQIds).stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        // Lắp ráp in-memory
        standaloneQs.forEach(q -> q.setAnswers(
                new ArrayList<>(answersByQ.getOrDefault(q.getId(), List.of()))));
        groupQs.forEach(q -> q.setAnswers(
                new ArrayList<>(answersByQ.getOrDefault(q.getId(), List.of()))));

        Map<String, List<QuestionTestEntity>> qsByGroup = groupQs.stream()
                .collect(Collectors.groupingBy(q -> q.getQuestionGroupEntity().getId()));
        groups.forEach(g -> g.setQuestions(
                new ArrayList<>(qsByGroup.getOrDefault(g.getId(), List.of()))));

        Map<String, List<QuestionTestEntity>> standaloneByPart = standaloneQs.stream()
                .collect(Collectors.groupingBy(q -> q.getPartEntity().getId()));
        Map<String, List<QuestionGroupEntity>> groupsByPart = groups.stream()
                .collect(Collectors.groupingBy(g -> g.getPartEntity().getId()));

        parts.forEach(p -> {
            p.setQuestions(new ArrayList<>(standaloneByPart.getOrDefault(p.getId(), List.of())));
            p.setQuestionGroups(new ArrayList<>(groupsByPart.getOrDefault(p.getId(), List.of())));
        });
    }
}