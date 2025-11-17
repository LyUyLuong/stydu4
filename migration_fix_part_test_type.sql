-- Migration script to fix part_test_entity.type column
-- Change from ENUM to VARCHAR(255) to support all PartType enum values

ALTER TABLE `part_test_entity` 
MODIFY COLUMN `type` VARCHAR(255) DEFAULT NULL;

