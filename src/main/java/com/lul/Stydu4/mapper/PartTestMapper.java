package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.PartTest.PartTestCreationRequest;
import com.lul.Stydu4.dto.request.PartTest.PartTestUpdateRequest;
import com.lul.Stydu4.dto.response.PartTest.PartTestDetailResponse;
import com.lul.Stydu4.dto.response.PartTest.PartTestSummaryResponse;
import com.lul.Stydu4.entity.PartTestEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class, QuestionGroupMapper.class})
public interface PartTestMapper {


    @Mapping(target = "testEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "questionGroups", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    PartTestEntity toPartTestEntity(PartTestCreationRequest request);


    @Mapping(target = "testEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "questionGroups", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePartTestEntityFromRequest(PartTestUpdateRequest request, @MappingTarget PartTestEntity entity);


    @Mapping(source = "testEntity.id", target = "testId")
    @Mapping(source = "questions", target = "questions")
    @Mapping(source = "questionGroups", target = "questionGroups")
    PartTestDetailResponse toPartTestResponse(PartTestEntity entity);


    @Mapping(source = "testEntity.id", target = "testId")
    @Mapping(target = "questionsCount",
            expression = "java(entity.getQuestions() == null ? 0 : entity.getQuestions().size())")
    @Mapping(target = "questionGroupsCount",
            expression = "java(entity.getQuestionGroups() == null ? 0 : entity.getQuestionGroups().size())")
    PartTestSummaryResponse toPartTestSummary(PartTestEntity entity);


}
