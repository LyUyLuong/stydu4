package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.ResultEntity;
import com.lul.Stydu4.entity.ResultHavePartsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IResultHavePartsRepository extends JpaRepository<ResultHavePartsEntity, String> {

}
