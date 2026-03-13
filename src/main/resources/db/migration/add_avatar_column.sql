-- Migration: Add avatar_url column to customers table
-- Date: 2026-02-01

ALTER TABLE `customers`
ADD COLUMN `avatar_url` VARCHAR(500) NULL DEFAULT NULL
AFTER `activated`;

-- Verify the column was added
-- SELECT id, email, first_name, last_name, avatar_url FROM customers;



