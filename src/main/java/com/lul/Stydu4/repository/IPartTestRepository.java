package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.PartTestEntity;
import com.lul.Stydu4.entity.TestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPartTestRepository extends JpaRepository<PartTestEntity, String>, JpaSpecificationExecutor<PartTestEntity> {
    Page<PartTestEntity> findAllBy(Pageable pageable);
    List<PartTestEntity> findByTestEntityIdOrderByCreatedDateAsc(String testEntityId);

    @Query("select p.testEntity.id, count(p.id) " +
            "from PartTestEntity p " +
            "where p.testEntity.id in :ids " +
            "group by p.testEntity.id")
    List<Object[]> countByTestIds(@Param("ids") List<String> testIds);
    
    /**
     * Find part by ID with all nested entities loaded (prevent N+1)
     * Loads: questionGroups, questionTests, and their audio/image/answers/questions
     */
    @EntityGraph(attributePaths = {
        "questionGroups",
        "questionGroups.audio",
        "questionGroups.image",
        "questionGroups.questions",
        "questionGroups.questions.audio",
        "questionGroups.questions.image",
        "questionGroups.questions.answers",
        "questionTests",
        "questionTests.audio",
        "questionTests.image",
        "questionTests.answers"
    })
    @Query("SELECT p FROM PartTestEntity p WHERE p.id = :id")
    Optional<PartTestEntity> findByIdWithQuestions(@Param("id") String id);

    /**
     * Find parts by test ID with all nested entities loaded (prevent N+1)
     * Loads: questionGroups, questionTests, and their audio/image/answers/questions
     */
    @EntityGraph(attributePaths = {
        "questionGroups",
        "questionGroups.audio",
        "questionGroups.image",
        "questionGroups.questions",
        "questionGroups.questions.audio",
        "questionGroups.questions.image",
        "questionGroups.questions.answers",
        "questionTests",
        "questionTests.audio",
        "questionTests.image",
        "questionTests.answers"
    })
    @Query("SELECT p FROM PartTestEntity p WHERE p.testEntity.id = :testId ORDER BY p.createdDate ASC")
    List<PartTestEntity> findByTestEntityIdWithQuestions(@Param("testId") String testId);
}

