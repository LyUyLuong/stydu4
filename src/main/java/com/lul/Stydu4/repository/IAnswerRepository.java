package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAnswerRepository extends JpaRepository <AnswerEntity, String> {

    /** Lấy đáp án cho 1 loạt question — dùng cho stepwise hydrate. */
    @Query("SELECT a FROM AnswerEntity a WHERE a.question.id IN :questionIds")
    List<AnswerEntity> findByQuestionIds(@Param("questionIds") List<String> questionIds);

}
