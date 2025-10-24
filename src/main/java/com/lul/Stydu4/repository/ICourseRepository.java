package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICourseRepository extends JpaRepository<CourseEntity, String> {
    
    List<CourseEntity> findByIsPublished(Boolean isPublished);
    
    Optional<CourseEntity> findByIdAndIsPublished(String id, Boolean isPublished);
}
