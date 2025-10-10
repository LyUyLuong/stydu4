package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Answer.AnswerCreateRequest;
import com.lul.Stydu4.dto.request.Answer.AnswerUpdateRequest;
import com.lul.Stydu4.dto.response.Answer.AnswerDetailResponse;
import com.lul.Stydu4.dto.response.Answer.AnswerSummaryResponse;
import com.lul.Stydu4.entity.AnswerEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "question", ignore = true)
    @Mapping(target = "id", ignore = true)
    AnswerEntity toAnswerEntity(AnswerCreateRequest request);

    @Mapping(target = "question", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateAnswerEntity(AnswerUpdateRequest request, @MappingTarget AnswerEntity answerEntity);

    @Mapping(source = "question.id", target = "questionId")
    AnswerDetailResponse toAnswerDetailResponse(AnswerEntity entity);


    @Mapping(source = "question.id", target = "questionId")
    AnswerSummaryResponse toAnswerSummaryResponse(AnswerEntity entity);

}
