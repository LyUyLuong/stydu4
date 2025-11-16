package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.QuestionGroupEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IQuestionGroupRepository extends JpaRepository<QuestionGroupEntity, String>, JpaSpecificationExecutor<QuestionGroupEntity> {

    @Query("select qg.partEntity.id, count(qg.id) " +
            "from QuestionGroupEntity qg " +
            "where qg.partEntity.id in :partIds " +
            "group by qg.partEntity.id")
    List<Object[]> countQuestionGroupsByPartIds(@Param("partIds") List<String> partIds);

    /**
     * Find all question groups with related entities (prevent N+1)
     * WARNING: This can be heavy, use with pagination
     */
    @EntityGraph(attributePaths = {"audio", "image", "questions", "questions.audio", "questions.image"})
    @Override
    java.util.List<QuestionGroupEntity> findAll();
    
    /**
     * Find question group by ID with all related entities (prevent N+1)
     */
    @EntityGraph(attributePaths = {"audio", "image", "questions", "questions.audio", "questions.image", "questions.answers"})
    @Query("SELECT qg FROM QuestionGroupEntity qg WHERE qg.id = :id")
    Optional<QuestionGroupEntity> findByIdWithDetails(@Param("id") String id);
    
    /**
     * Find question groups by part ID with related entities (prevent N+1)
     */
    @EntityGraph(attributePaths = {"audio", "image", "questions", "questions.audio", "questions.image", "questions.answers"})
    @Query("SELECT qg FROM QuestionGroupEntity qg WHERE qg.partEntity.id = :partId")
    List<QuestionGroupEntity> findByPartIdWithDetails(@Param("partId") String partId);
}