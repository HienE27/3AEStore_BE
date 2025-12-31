-- V4: Create review_replies with BINARY(16) keys and safely add moderation columns
-- Drop any partially created table from previous failed migration
DROP TABLE IF EXISTS review_replies;

-- Create replies table using BINARY(16) to match reviews.id (UUID stored as binary)
CREATE TABLE IF NOT EXISTS review_replies (
  id BINARY(16) NOT NULL PRIMARY KEY,
  review_id BINARY(16) NOT NULL,
  author_id VARCHAR(100),
  author_name VARCHAR(200),
  author_role VARCHAR(50),
  content TEXT,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_reply_review FOREIGN KEY (review_id) REFERENCES reviews(id)
) ENGINE=InnoDB;

-- Add moderation columns to reviews only if they don't exist.
-- Use information_schema checks + dynamic ALTER to avoid syntax errors on older MySQL.

-- visible
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'visible');
SET @sql := IF(@has_col = 0, 'ALTER TABLE reviews ADD COLUMN visible TINYINT(1) DEFAULT 1', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- moderated_by
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'moderated_by');
SET @sql := IF(@has_col = 0, 'ALTER TABLE reviews ADD COLUMN moderated_by VARCHAR(255)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- moderated_at
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' AND COLUMN_NAME = 'moderated_at');
SET @sql := IF(@has_col = 0, 'ALTER TABLE reviews ADD COLUMN moderated_at TIMESTAMP NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


