package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPartTestRepository extends JpaRepository<PartTestEntity, String>, JpaSpecificationExecutor<PartTestEntity> {
    Page<PartTestEntity> findAllBy(Pageable pageable);
    List<PartTestEntity> findByTestEntityIdOrderByCreatedDateAsc(String testEntityId);

    @Query("select p.testEntity.id, count(p.id) " +
            "from PartTestEntity p " +
            "where p.testEntity.id in :ids " +
            "group by p.testEntity.id")
    List<Object[]> countByTestIds(@Param("ids") List<String> testIds);
}
