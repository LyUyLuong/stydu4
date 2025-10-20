package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Answer.AnswerCreateRequest;
import com.lul.Stydu4.dto.request.Answer.AnswerUpdateRequest;
import com.lul.Stydu4.dto.response.Answer.AnswerDetailResponse;
import com.lul.Stydu4.dto.response.Answer.AnswerSummaryResponse;
import com.lul.Stydu4.entity.AnswerEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    AnswerEntity toAnswerEntity(AnswerCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAnswerEntity(AnswerUpdateRequest request, @MappingTarget AnswerEntity entity);

    @Mapping(source = "question.id", target = "questionId")
    AnswerDetailResponse toAnswerDetailResponse(AnswerEntity entity);

    @Mapping(source = "question.id", target = "questionId")
    AnswerSummaryResponse toAnswerSummaryResponse(AnswerEntity entity);
}
