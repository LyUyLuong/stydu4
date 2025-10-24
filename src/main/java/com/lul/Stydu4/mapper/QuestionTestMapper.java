package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Question.QuestionTestCreateRequest;
import com.lul.Stydu4.dto.request.Question.QuestionTestUpdateRequest;
import com.lul.Stydu4.dto.response.Question.QuestionTestDetailResponse;
import com.lul.Stydu4.dto.response.Question.QuestionTestSummaryResponse;
import com.lul.Stydu4.entity.QuestionTestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionTestMapper {

    // ✅ Request → Entity: Ignore FileEntity (handled in service)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "audio", ignore = true)
    QuestionTestEntity toQuestionTestEntity(QuestionTestCreateRequest request);

    // ✅ Update: Ignore FileEntity (handled in service)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questionGroupEntity", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionTestEntity(QuestionTestUpdateRequest request, @MappingTarget QuestionTestEntity entity);

    // ✅ NEW: Entity → Summary Response with file references
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(target = "answersCount",
            expression = "java(questionTestEntity.getAnswers() == null ? 0 : questionTestEntity.getAnswers().size())")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image.fileUrl", target = "imageUrl")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(source = "audio.fileUrl", target = "audioUrl")
    QuestionTestSummaryResponse toQuestionSummaryResponse(QuestionTestEntity questionTestEntity);

    // ✅ NEW: Entity → Detail Response with file references
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(source = "answers", target = "answers")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image.fileUrl", target = "imageUrl")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(source = "audio.fileUrl", target = "audioUrl")
    QuestionTestDetailResponse toQuestionDetailResponse(QuestionTestEntity questionTestEntity);
}
