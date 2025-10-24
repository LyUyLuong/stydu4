package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IResultRepository extends JpaRepository <ResultEntity, String>{

    List<ResultEntity> findByTestIdAndUserUsernameOrderByCreatedDateDesc(String testId, String userName);
}
