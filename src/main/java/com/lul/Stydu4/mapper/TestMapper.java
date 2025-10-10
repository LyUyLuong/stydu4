package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.Test.TestCreationRequest;
import com.lul.Stydu4.dto.request.Test.TestUpdateRequest;
import com.lul.Stydu4.dto.response.Test.TestDetailResponse;
import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.TestEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = PartTestMapper.class)
public interface TestMapper {


    @Mapping(target = "partTestEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    TestEntity toTestEntity(TestCreationRequest request);


    @Mapping(target = "partTestEntities", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTestEntityFromRequest(TestUpdateRequest request, @MappingTarget TestEntity entity);


    @Mapping(source = "partTestEntities", target = "parts")
    TestDetailResponse toTestResponse(TestEntity entity);



    @Mapping(target = "partsCount",
            expression = "java(entity.getPartTestEntities() == null ? 0 : entity.getPartTestEntities().size())")
    TestSummaryResponse toTestSummary(TestEntity entity);


}
