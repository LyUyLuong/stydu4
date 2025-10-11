package com.lul.Stydu4.dto.request.Test;

import com.lul.Stydu4.dto.request.Test.TestSearchRequest;
import com.lul.Stydu4.entity.TestEntity;
import com.lul.Stydu4.enums.TestType;
import com.lul.Stydu4.util.EnumValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import static com.lul.Stydu4.util.EnumValidator.convertOrNull;

@Slf4j
public class TestSpecification {

    /**
     * Build specification từ search request
     */
    public static Specification<TestEntity> buildSpecification(TestSearchRequest request) {
        return Specification.allOf(
                nameLike(request.getName()),
                typeEquals(request.getType()),
                statusEquals(request.getStatus())
        );
    }

    /**
     * Tìm kiếm theo name (partial match, case-insensitive)
     */
    private static Specification<TestEntity> nameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
        };
    }


    /**
     * Tìm kiếm theo type (exact match, case-insensitive)
     * Nếu invalid enum -> trả mảng rỗng
     */
    private static Specification<TestEntity> typeEquals(String typeStr) {
        return (root, query, cb) -> {
            if (typeStr == null || typeStr.isBlank()) {
                return null;
            }

            TestType type = convertOrNull(typeStr, TestType.class);

            if (type == null) {
                log.warn("Invalid TestType value: '{}'. Returning empty result.", typeStr);
                return cb.disjunction(); // WHERE 1=0 (mảng rỗng)
            }

            return cb.equal(root.get("type"), type);
        };
    }

    /**
     * Tìm kiếm theo status (exact match)
     */
    private static Specification<TestEntity> statusEquals(Integer status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }
}

