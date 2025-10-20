package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.QuestionTestEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IQuestionTestRepository extends JpaRepository<QuestionTestEntity, String>, JpaSpecificationExecutor<QuestionTestEntity> {

    @Query("select q.partEntity.id, count(q.id) " +
            "from QuestionTestEntity q " +
            "where q.partEntity.id in :partIds " +
            "group by q.partEntity.id")
    List<Object[]> countQuestionsByPartIds(@Param("partIds") List<String> partIds);
}
