package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupCreateRequest;
import com.lul.Stydu4.dto.request.QuestionGroup.QuestionGroupUpdateRequest;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupDetailResponse;
import com.lul.Stydu4.dto.response.QuestionGroupResponse.QuestionGroupSummaryResponse;
import com.lul.Stydu4.entity.QuestionGroupEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = QuestionTestMapper.class)
public interface QuestionGroupMapper {

    // ✅ Request → Entity: Ignore FileEntity (handled in service)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "audio", ignore = true)
    QuestionGroupEntity toQuestionGroupEntity(QuestionGroupCreateRequest request);

    // ✅ Update: Ignore FileEntity (handled in service)
    @Mapping(target = "partEntity", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestionGroupEntity(QuestionGroupUpdateRequest request, @MappingTarget QuestionGroupEntity entity);

    // ✅ NEW: Entity → Detail Response with file references
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(source = "questions", target = "questions")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image.fileUrl", target = "imageUrl")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(source = "audio.fileUrl", target = "audioUrl")
    QuestionGroupDetailResponse toQuestionGroupDetailResponse(QuestionGroupEntity entity);

    // ✅ NEW: Entity → Summary Response with file references
    @Mapping(source = "partEntity.id", target = "partTestId")
    @Mapping(target = "questionsCount",
            expression = "java(entity.getQuestions() == null ? 0 : entity.getQuestions().size())")
    @Mapping(source = "image.id", target = "imageId")
    @Mapping(source = "image.fileUrl", target = "imageUrl")
    @Mapping(source = "audio.id", target = "audioId")
    @Mapping(source = "audio.fileUrl", target = "audioUrl")
    QuestionGroupSummaryResponse toQuestionGroupSummaryResponse(QuestionGroupEntity entity);
}
