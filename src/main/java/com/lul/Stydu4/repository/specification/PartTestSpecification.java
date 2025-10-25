package com.lul.Stydu4.repository.specification;

import com.lul.Stydu4.dto.request.PartTest.PartTestSearchRequest;
import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.PartType;
import com.lul.Stydu4.util.EnumValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class PartTestSpecification {

    /**
     * Build specification từ search request
     */
    public static Specification<PartTestEntity> buildSpecification(PartTestSearchRequest request) {
        return Specification.allOf(
                nameLike(request.getName()),
                testIdEquals(request.getTestId()),
                testNameLike((request.getTestName())),
                typeEquals(request.getType()),
                createdBetween(request.getCreatedFrom(),request.getCreatedTo())

        );
    }
    private static Specification<PartTestEntity> nameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
        };
    }

    private static Specification<PartTestEntity> testIdEquals(String testId) {
        return (root, query, cb) -> {
            if (testId == null || testId.isBlank()) {
                return null;
            }
            return cb.equal(root.get("testEntity").get("id"), testId.trim());
        };
    }

    private static Specification<PartTestEntity> testNameLike(String testName) {
        return (root, query, cb) -> {
            if (testName == null || testName.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("testEntity").get("name")), "%" + testName.toLowerCase().trim() + "%");
        };
    }

    /**
     * Tìm kiếm theo type (exact match, case-insensitive)
     * Nếu invalid enum -> trả mảng rỗng
     */
    private static Specification<PartTestEntity> typeEquals(String typeStr) {
        return (root, query, cb) -> {
            if (typeStr == null || typeStr.isBlank()) {
                return null;
            }

            PartType type = EnumValidator.validateAndConvert(typeStr, PartType.class, ErrorCode.INVALID_PART_TYPE);

            if (type == null) {
                log.warn("Invalid TestType value: '{}'. Returning empty result.", typeStr);
                return cb.disjunction(); // WHERE 1=0 (mảng rỗng)
            }

            return cb.equal(root.get("type"), type);
        };
    }

    private static Specification<PartTestEntity> createdBetween(LocalDate from, LocalDate to) {
        return ((root, query, cb) ->
        {
            if (from == null && to == null){
                return null;
            }

            LocalDateTime start = from != null ? from.atStartOfDay() : null;
            LocalDateTime end = to != null ? to.atTime(23,59,59) : null;

            if(start != null && end != null) {
                return cb.between(root.get("createdDate"), start, end);
            }

            if(start != null) {
                return cb.greaterThanOrEqualTo(root.get("createdDate"),start);
            }

            return cb.lessThanOrEqualTo(root.get("createdDate"), end);
        });
    }
}
