package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.QuestionTestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IQuestionTestRepository extends JpaRepository<QuestionTestEntity, String>, JpaSpecificationExecutor<QuestionTestEntity> {

    @Query("select q.partEntity.id, count(q.id) " +
            "from QuestionTestEntity q " +
            "where q.partEntity.id in :partIds " +
            "group by q.partEntity.id")
    List<Object[]> countQuestionsByPartIds(@Param("partIds") List<String> partIds);

    /** Detail view: kéo đủ audio + image + answers cho 1 question. */
    @EntityGraph(attributePaths = {"audio", "image", "answers"})
    @Override
    Optional<QuestionTestEntity> findById(String id);

    /** List/search view: chỉ kéo audio + image (answers chưa cần để render summary). */
    @EntityGraph(attributePaths = {"audio", "image"})
    @Override
    Page<QuestionTestEntity> findAll(Pageable pageable);

    /** Câu hỏi rời (không thuộc group) của các part — dùng cho stepwise hydrate. */
    @EntityGraph(attributePaths = {"audio", "image"})
    @Query("SELECT q FROM QuestionTestEntity q " +
            "WHERE q.partEntity.id IN :partIds " +
            "  AND q.questionGroupEntity IS NULL")
    List<QuestionTestEntity> findStandaloneByPartIds(@Param("partIds") List<String> partIds);

    /** Câu hỏi thuộc các group — dùng cho stepwise hydrate. */
    @EntityGraph(attributePaths = {"audio", "image"})
    @Query("SELECT q FROM QuestionTestEntity q " +
            "WHERE q.questionGroupEntity.id IN :groupIds")
    List<QuestionTestEntity> findByGroupIdsWithMedia(@Param("groupIds") List<String> groupIds);


    long countByIdIn(Collection<String> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE question_test_entity SET part_id = NULL WHERE part_id = :partId",
            nativeQuery = true)
    int detachAllByPartId(@Param("partId") String partId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE question_test_entity SET part_id = NULL " +
            "WHERE part_id = :partId AND id NOT IN (:keepIds)",
            nativeQuery = true)
    int detachByPartIdExcluding(@Param("partId") String partId,
                                @Param("keepIds") Collection<String> keepIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE question_test_entity SET part_id = :partId WHERE id IN (:ids)",
            nativeQuery = true)
    int attachToPart(@Param("partId") String partId,
                     @Param("ids") Collection<String> ids);

}
