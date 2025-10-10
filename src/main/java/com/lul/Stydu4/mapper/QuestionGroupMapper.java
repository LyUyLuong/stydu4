package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Question.QuestionCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import com.lul.Stydu4.entity.QuestionTestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring" , uses = QuestionMapper.class)
public interface QuestionGroupMapper {



    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "id", ignore = true)
    QuestionGroupEntity tQuestionGroupEntity(QuestionGroupCreateRequest request);

    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionGroupEntity(QuestionGroupUpdateRequest request, @MappingTarget QuestionGroupEntity entity);

    @Mapping(source = "partEntity.id" , target = "partTestId")
    @Mapping(source = "questions", target = "questions")
    QuestionGroupDetailResponse toQuestionGroupDetailResponse(QuestionGroupEntity entity);

    @Mapping(source = "partEntity.id" , target = "partTestId")
    @Mapping(target = "questionsCount",
            expression = "java(entity.getQuestions() == null ? 0 : entity.getQuestions().size())")
    QuestionGroupSummaryResponse toQuestionGroupSummaryResponse(QuestionGroupEntity entity);


}
