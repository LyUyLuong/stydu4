package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAnswerRepository extends JpaRepository <AnswerEntity, String> {
}
