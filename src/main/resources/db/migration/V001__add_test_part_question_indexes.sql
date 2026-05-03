-- =========================================================================
-- Tăng tốc các API Test/PartTest/QuestionTest
-- An toàn cho production: mọi index thêm bằng ALGORITHM=INPLACE, LOCK=NONE
-- → InnoDB online DDL, không khoá table, vẫn đọc/ghi bình thường.
--
-- Trước khi chạy: nên xem index hiện có để khỏi bị duplicate
--   SHOW INDEX FROM part_test_entity;
--   SHOW INDEX FROM question_test_entity;
--   SHOW INDEX FROM question_group_entity;
--
-- LƯU Ý 1: MySQL InnoDB tự tạo index 1 cột cho mọi FOREIGN KEY (test_id,
-- part_id, question_group_id, question_id...) → KHÔNG cần thêm index
-- 1 cột trùng. Các index dưới đây đều là COMPOSITE hoặc cột phụ trợ
-- mà FK-auto-index không thay thế được.
--
-- LƯU Ý 2: Cột timestamp trong DB là `created_date` (snake_case) do
-- SpringPhysicalNamingStrategy convert từ @Column(name="createdDate")
-- trong Java. Luôn dùng tên snake_case ở SQL tay.
-- =========================================================================

-- ⚠️ Khi chạy trên prod nhớ chọn đúng database trước:
 USE db_stydu4;
-- Khi chạy local:
-- USE stydu5;

-- PartTest: phục vụ findByTestEntityIdOrderByCreatedDateAsc
-- (WHERE test_id=? ORDER BY created_date) → composite này loại bỏ filesort.
ALTER TABLE part_test_entity
    ADD INDEX idx_part_test_id_created (test_id, created_date),
    ALGORITHM=INPLACE, LOCK=NONE;

-- PartTest: pagination admin (ORDER BY created_date DESC LIMIT/OFFSET).
ALTER TABLE part_test_entity
    ADD INDEX idx_part_created (created_date),
    ALGORITHM=INPLACE, LOCK=NONE;

-- QuestionTest: phục vụ findStandaloneByPartIds
-- (WHERE part_id IN (...) AND question_group_id IS NULL).
-- FK-auto-index chỉ phủ part_id; composite mới phủ thêm IS NULL filter
-- → MySQL dùng index range scan luôn cả 2 cột.
ALTER TABLE question_test_entity
    ADD INDEX idx_question_part_group (part_id, question_group_id),
    ALGORITHM=INPLACE, LOCK=NONE;

-- QuestionTest: pagination admin và search ORDER BY created_date.
ALTER TABLE question_test_entity
    ADD INDEX idx_question_created (created_date),
    ALGORITHM=INPLACE, LOCK=NONE;

-- QuestionGroup: pagination admin.
ALTER TABLE question_group_entity
    ADD INDEX idx_qg_created (created_date),
    ALGORITHM=INPLACE, LOCK=NONE;