package com.lul.Stydu4.repository;

import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.TestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITestRepository
        extends JpaRepository<TestEntity, String>, JpaSpecificationExecutor<TestEntity> {

    /** List view: kéo audio cùng query để khỏi N+1 khi mapper đọc audio.id. */
    @EntityGraph(attributePaths = {"audio"})
    Page<TestEntity> findAllBy(Pageable pageable);

    /** Search view (override JpaSpecificationExecutor) — cũng kéo sẵn audio. */
    @EntityGraph(attributePaths = {"audio"})
    @Override
    Page<TestEntity> findAll(Specification<TestEntity> spec, Pageable pageable);

    Long countByStatus(Integer status);

    /** Detail view 1 test — chỉ cần audio, parts/questions service tự hydrate. */
    @EntityGraph(attributePaths = {"audio"})
    @Query("SELECT t FROM TestEntity t WHERE t.id = :id")
    Optional<TestEntity> findByIdWithAudio(@Param("id") String id);
}