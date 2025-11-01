package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.EnrollmentEntity;
import com.lul.Stydu4.enums.EnrollmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEnrollmentRepository extends JpaRepository<EnrollmentEntity, String> {
    
    List<EnrollmentEntity> findByUserId(String userId);
    
    List<EnrollmentEntity> findByUserIdAndStatus(String userId, EnrollmentStatus status);
    
    Optional<EnrollmentEntity> findByUserIdAndCourseId(String userId, String courseId);
    
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    
    @EntityGraph(attributePaths = {"course", "user"})
    List<EnrollmentEntity> findTop10ByOrderByEnrolledAtDesc(Pageable pageable);
}
