package com.lul.Stydu4.repository;

import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.TestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITestRepository extends JpaRepository<TestEntity, String>, JpaSpecificationExecutor<TestEntity> {
    Page<TestEntity> findAllBy(Pageable pageable);
    
    Long countByStatus(Integer status);
    
    /**
     * Find test by ID with all related parts and audio loaded in single query
     * This prevents N+1 query problem
     */
    @EntityGraph(attributePaths = {"partTestEntities", "audio"})
    @Query("SELECT t FROM TestEntity t WHERE t.id = :id")
    Optional<TestEntity> findByIdWithParts(@Param("id") String id);
    
    /**
     * Find test by ID with audio only (lighter query for summary views)
     */
    @EntityGraph(attributePaths = {"audio"})
    @Query("SELECT t FROM TestEntity t WHERE t.id = :id")
    Optional<TestEntity> findByIdWithAudio(@Param("id") String id);
}
