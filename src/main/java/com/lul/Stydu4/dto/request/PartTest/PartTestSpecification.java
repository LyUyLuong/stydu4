package com.lul.Stydu4.dto.request.PartTest;

import com.lul.Stydu4.entity.PartTestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class PartTestSpecification {

    /**
     * Build specification từ search request
     */
    public static Specification<PartTestEntity> buildSpecification(PartTestSearchRequest request) {
        return Specification.allOf(
                nameLike(request.getName())
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

    private static Specification<PartTestEntity> testNameLike(String testName) {
        return (root, query, cb) -> {
            if (testName == null || testName.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("test").get("name")), "%" + testName.toLowerCase().trim() + "%");
        };
    }

}
