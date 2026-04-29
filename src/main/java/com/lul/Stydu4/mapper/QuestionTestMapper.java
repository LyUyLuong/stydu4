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
    // Build relative URL from id; never read fileUrl (legacy rows hold absolute localhost URLs).
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(target = "imageUrl",
             expression = "java(questionTestEntity.getImage() != null ? \"/api/v1/files/\" + questionTestEntity.getImage().getId() : null)")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(target = "audioUrl",
             expression = "java(questionTestEntity.getAudio() != null ? \"/api/v1/files/\" + questionTestEntity.getAudio().getId() : null)")
    QuestionTestSummaryResponse toQuestionSummaryResponse(QuestionTestEntity questionTestEntity);

    // ✅ NEW: Entity → Detail Response with file references
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questionGroupEntity.id", target = "questionGroupId")
    @Mapping(source = "answers", target = "answers")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(target = "imageUrl",
             expression = "java(questionTestEntity.getImage() != null ? \"/api/v1/files/\" + questionTestEntity.getImage().getId() : null)")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(target = "audioUrl",
             expression = "java(questionTestEntity.getAudio() != null ? \"/api/v1/files/\" + questionTestEntity.getAudio().getId() : null)")
    QuestionTestDetailResponse toQuestionDetailResponse(QuestionTestEntity questionTestEntity);
}
