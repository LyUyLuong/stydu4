package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.PartTestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IPartTestRepository
        extends JpaRepository<PartTestEntity, String>, JpaSpecificationExecutor<PartTestEntity> {

    Page<PartTestEntity> findAllBy(Pageable pageable);

    List<PartTestEntity> findByTestEntityIdOrderByCreatedDateAsc(String testEntityId);

    @Query("select p.testEntity.id, count(p.id) " +
            "from PartTestEntity p " +
            "where p.testEntity.id in :ids " +
            "group by p.testEntity.id")
    List<Object[]> countByTestIds(@Param("ids") List<String> testIds);

    /** Validate tồn tại trước khi attach — tránh tạo FK trỏ vào id bịa. */
    long countByIdIn(Collection<String> ids);

    /** Gỡ TẤT CẢ part đang trỏ vào test này (khi user clear list). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE part_test_entity SET test_id = NULL WHERE test_id = :testId",
            nativeQuery = true)
    int detachAllByTestId(@Param("testId") String testId);

    /** Gỡ các part đang thuộc test nhưng KHÔNG nằm trong danh sách giữ lại. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE part_test_entity SET test_id = NULL " +
            "WHERE test_id = :testId AND id NOT IN (:keepIds)",
            nativeQuery = true)
    int detachByTestIdExcluding(@Param("testId") String testId,
                                @Param("keepIds") Collection<String> keepIds);

    /** Gắn các part vào test (kể cả part đang thuộc test khác — semantics cũ). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE part_test_entity SET test_id = :testId WHERE id IN (:ids)",
            nativeQuery = true)
    int attachToTest(@Param("testId") String testId,
                     @Param("ids") Collection<String> ids);
}
