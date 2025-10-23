package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.TestEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = PartTestMapper.class)
public interface TestMapper {

    // ✅ KEEP UNCHANGED - Request to Entity
    @Mapping(target = "partTestEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "audio", ignore = true)  // ✅ ADD THIS - Handle in service
    TestEntity toTestEntity(TestCreationRequest request);

    // ✅ KEEP UNCHANGED - Update Entity from Request
    @Mapping(target = "partTestEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "audio", ignore = true)  // ✅ ADD THIS - Handle in service
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTestEntityFromRequest(TestUpdateRequest request, @MappingTarget TestEntity entity);

    // ✅ UPDATED - Entity to Detail Response
    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "partTestEntities", target = "parts")
    @Mapping(source = "audio.id", target = "audioId")           // ✅ NEW
    @Mapping(source = "audio.fileUrl", target = "audioUrl")     // ✅ NEW
    TestDetailResponse toTestResponse(TestEntity entity);

    // ✅ UPDATED - Entity to Summary Response
    @Mapping(source = "type.name", target = "type")
    @Mapping(target = "partsCount", ignore = true)
    @Mapping(source = "audio.id", target = "audioId")           // ✅ NEW
    @Mapping(source = "audio.fileUrl", target = "audioUrl")     // ✅ NEW
    TestSummaryResponse toTestSummary(TestEntity entity);
}
