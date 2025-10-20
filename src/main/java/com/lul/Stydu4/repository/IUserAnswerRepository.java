package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.UserAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserAnswerRepository extends JpaRepository<UserAnswerEntity, String> {
    List<UserAnswerEntity> findByResultId(String resultId);
}
