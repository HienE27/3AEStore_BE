-- Migration: drop legacy integer rating column and its check constraint
-- Run this manually or via Flyway if configured.

-- Drop CHECK constraint if present (constraint name may differ)
ALTER TABLE reviews DROP CHECK reviews_chk_1;

-- Drop legacy column 'rating' if no longer needed
ALTER TABLE reviews DROP COLUMN rating;

-- Note: If the DROP CHECK fails due to constraint name mismatch,
-- inspect SHOW CREATE TABLE reviews; then replace reviews_chk_1 with the actual name.


