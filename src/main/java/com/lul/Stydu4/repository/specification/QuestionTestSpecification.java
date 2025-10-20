package com.lul.Stydu4.repository.specification;

import com.lul.Stydu4.dto.request.Question.QuestionTestSearchRequest;
import com.lul.Stydu4.entity.QuestionTestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class QuestionTestSpecification {

    /**
     * Build specification từ search request
     */
    public static Specification<QuestionTestEntity> buildSpecification(QuestionTestSearchRequest request) {
        return Specification.allOf(
                nameLike(request.getName()),
                typeEquals(request.getType()),
                partNameLike(request.getPartName()),
                createdBetween(request.getCreatedFrom(), request.getCreatedTo())
        );
    }

    /**
     * Tìm kiếm theo name (case-insensitive, partial match)
     */
    private static Specification<QuestionTestEntity> nameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase().trim() + "%");
        };
    }


    /**
     * Tìm kiếm theo type (exact match, case-insensitive)
     */
    private static Specification<QuestionTestEntity> typeEquals(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) {
                return null;
            }
            return cb.equal(cb.lower(root.get("type")), type.toLowerCase().trim());
        };
    }

    /**
     * Tìm kiếm theo part name (join với PartTestEntity)
     */
    private static Specification<QuestionTestEntity> partNameLike(String partName) {
        return (root, query, cb) -> {
            if (partName == null || partName.isBlank()) {
                return null;
            }
            return cb.like(
                    cb.lower(root.get("partEntity").get("name")),
                    "%" + partName.toLowerCase().trim() + "%"
            );
        };
    }


    /**
     * Tìm kiếm theo khoảng thời gian tạo
     */
    private static Specification<QuestionTestEntity> createdBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }

            LocalDateTime start = from != null ? from.atStartOfDay() : null;
            LocalDateTime end = to != null ? to.atTime(23, 59, 59) : null;

            if (start != null && end != null) {
                return cb.between(root.get("createdDate"), start, end);
            }

            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("createdDate"), start);
            }

            return cb.lessThanOrEqualTo(root.get("createdDate"), end);
        };
    }
}
