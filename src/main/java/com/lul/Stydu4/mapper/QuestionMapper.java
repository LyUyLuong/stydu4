package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Question.QuestionCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionUpdateRequest;
import com.lul.Stydu4.dto.response.Question.QuestionDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionSummaryResponse;
import com.lul.Stydu4.entity.QuestionTestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionMapper {

    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    QuestionTestEntity toQuestionTestEntity(QuestionCreateRequest question);


    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionTestEntity(QuestionUpdateRequest question, @MappingTarget QuestionTestEntity questionTestEntity);

    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(target = "answersCount",
            expression = "java(questionTestEntity.getAnswers() == null ? 0 : questionTestEntity.getAnswers().size())")
    QuestionSummaryResponse toQuestionSummaryResponse(QuestionTestEntity questionTestEntity);

    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(source = "answers", target = "answers")
    QuestionDetailResponse toQuestionDetailResponse(QuestionTestEntity questionTestEntity);

}
