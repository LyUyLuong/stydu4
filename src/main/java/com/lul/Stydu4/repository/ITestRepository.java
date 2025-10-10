package com.lul.Stydu4.repository;

import com.lul.Stydu4.dto.response.Test.TestSummaryResponse;
import com.lul.Stydu4.entity.TestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITestRepository extends JpaRepository<TestEntity, String> {
    Page<TestEntity> findAllBy(Pageable pageable);
}
