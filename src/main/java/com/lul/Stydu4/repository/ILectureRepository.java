package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILectureRepository extends JpaRepository<LectureEntity, String> {

    /**
     * Find all lectures for a course, ordered by orderIndex
     */
    @Query("SELECT l FROM LectureEntity l WHERE l.course.id = :courseId ORDER BY l.orderIndex ASC")
    List<LectureEntity> findByCourseIdOrderByOrderIndex(@Param("courseId") String courseId);

    /**
     * Find only published lectures for a course, ordered by orderIndex
     */
    @Query("SELECT l FROM LectureEntity l WHERE l.course.id = :courseId AND l.isPublished = true ORDER BY l.orderIndex ASC")
    List<LectureEntity> findByCourseIdAndIsPublishedTrueOrderByOrderIndex(@Param("courseId") String courseId);

    /**
     * Get maximum order index for a course
     */
    @Query("SELECT COALESCE(MAX(l.orderIndex), -1) FROM LectureEntity l WHERE l.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") String courseId);
}
