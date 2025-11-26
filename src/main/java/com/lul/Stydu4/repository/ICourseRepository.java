package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.CourseEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICourseRepository extends JpaRepository<CourseEntity, String> {
    
    List<CourseEntity> findByIsPublished(Boolean isPublished);
    
    Optional<CourseEntity> findByIdAndIsPublished(String id, Boolean isPublished);
    
    /**
     * Find course by ID with tests loaded (prevent N+1)
     */
    @EntityGraph(attributePaths = {"tests"})
    @Query("SELECT c FROM CourseEntity c WHERE c.id = :id")
    Optional<CourseEntity> findByIdWithTests(@Param("id") String id);
    
    /**
     * Find published course by ID with tests loaded (prevent N+1)
     */
    @EntityGraph(attributePaths = {"tests"})
    @Query("SELECT c FROM CourseEntity c WHERE c.id = :id AND c.isPublished = :isPublished")
    Optional<CourseEntity> findByIdAndIsPublishedWithTests(@Param("id") String id, @Param("isPublished") Boolean isPublished);
}
