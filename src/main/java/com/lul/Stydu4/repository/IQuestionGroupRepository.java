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

@Repository
public interface IQuestionGroupRepository extends JpaRepository<QuestionGroupEntity, String>, JpaSpecificationExecutor<QuestionGroupEntity> {

    @Query("select qg.partEntity.id, count(qg.id) " +
            "from QuestionGroupEntity qg " +
            "where qg.partEntity.id in :partIds " +
            "group by qg.partEntity.id")
    List<Object[]> countQuestionGroupsByPartIds(@Param("partIds") List<String> partIds);

    @EntityGraph(attributePaths = {"audio", "image", "questions", "questions.audio", "questions.image"})
    @Override
    java.util.List<QuestionGroupEntity> findAll();
}