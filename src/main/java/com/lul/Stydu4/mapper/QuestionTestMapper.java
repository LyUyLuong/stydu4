package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.QuestionTestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionTestMapper {

    // ✅ Create: ignore type - sẽ set manually trong service
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    QuestionTestEntity toQuestionTestEntity(QuestionTestCreateRequest request);

    // ✅ Update: ignore type - sẽ set manually trong service
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionTestEntity(QuestionTestUpdateRequest request, @MappingTarget QuestionTestEntity entity);

    // ✅ Summary Response: Convert enum to String
    @Mapping(source = "type", target = "type",
            expression = "java(questionTestEntity.getType() != null ? questionTestEntity.getType().name() : null)")
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(target = "answersCount",
            expression = "java(questionTestEntity.getAnswers() == null ? 0 : questionTestEntity.getAnswers().size())")
    QuestionTestSummaryResponse toQuestionSummaryResponse(QuestionTestEntity questionTestEntity);

    // ✅ Detail Response: Convert enum to String
    @Mapping(source = "type", target = "type",
            expression = "java(questionTestEntity.getType() != null ? questionTestEntity.getType().name() : null)")
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(source = "answers", target = "answers")
    QuestionTestDetailResponse toQuestionDetailResponse(QuestionTestEntity questionTestEntity);
}
