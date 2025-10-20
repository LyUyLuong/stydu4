package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.QuestionTestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionTestMapper {

    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true) // Sẽ handle manually trong service
    @Mapping(target = "id", ignore = true)
    QuestionTestEntity toQuestionTestEntity(QuestionTestCreateRequest request);

    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionTestEntity(QuestionTestUpdateRequest request, @MappingTarget QuestionTestEntity entity);

    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(target = "answersCount",
            expression = "java(questionTestEntity.getAnswers() == null ? 0 : questionTestEntity.getAnswers().size())")
    QuestionTestSummaryResponse toQuestionSummaryResponse(QuestionTestEntity questionTestEntity);

    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(source = "answers", target = "answers")
    QuestionTestDetailResponse toQuestionDetailResponse(QuestionTestEntity questionTestEntity);
}
