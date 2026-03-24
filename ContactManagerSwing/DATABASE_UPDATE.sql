-- ============================================
-- DATABASE UPDATE SCRIPT
-- Contact Management System - Users + java_project DB
-- ============================================
-- NOTE: If starting fresh, use database.sql instead.
-- This script updates an EXISTING database to the new java_project structure.
-- ============================================

USE java_project;

-- 1. Add users table if it does not exist
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add user_id column to contacts if missing
ALTER TABLE contacts ADD COLUMN IF NOT EXISTS user_id INT NOT NULL DEFAULT 1;

-- 3. Add category column if missing
ALTER TABLE contacts ADD COLUMN IF NOT EXISTS category VARCHAR(30) DEFAULT 'Friends';

-- 4. Set default category for old rows
UPDATE contacts SET category = 'Friends' WHERE category IS NULL;

-- 5. Add foreign key for user_id
ALTER TABLE contacts
    ADD CONSTRAINT fk_contacts_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 6. Verify
DESCRIBE contacts;
DESCRIBE users;

-- ============================================
-- NOTES:
-- ============================================
-- - Database is now: java_project
-- - Each contact is linked to a user via user_id
-- - Allowed categories: Friends, Family, Work, Emergency
-- ============================================
