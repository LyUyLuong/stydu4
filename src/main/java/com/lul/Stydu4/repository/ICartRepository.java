package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.CartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartRepository extends JpaRepository<CartEntity, String> {
    
    @EntityGraph(attributePaths = {"course"})
    List<CartEntity> findByUserId(String userId);
    
    Optional<CartEntity> findByUserIdAndCourseId(String userId, String courseId);
    
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    
    void deleteByUserIdAndCourseId(String userId, String courseId);
    
    void deleteByUserId(String userId);
}
