-- =====================================================
-- CONTACT MANAGEMENT SYSTEM - DATABASE SCHEMA
-- =====================================================
-- Database: java_project
-- Username: root
-- Password: root
-- =====================================================

-- Drop database if exists and recreate
DROP DATABASE IF EXISTS java_project;
CREATE DATABASE java_project;
USE java_project;

-- =====================================================
-- USERS TABLE
-- Each user owns their own set of contacts
-- =====================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CONTACTS TABLE  (linked to users via user_id)
-- =====================================================
CREATE TABLE contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    number VARCHAR(15) NOT NULL,
    email VARCHAR(50),
    category VARCHAR(30) DEFAULT 'Friends',
    is_deleted TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_contacts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Unique constraints scoped per user (active records only)
ALTER TABLE contacts
ADD CONSTRAINT unique_name_user_active UNIQUE (user_id, name, is_deleted);

ALTER TABLE contacts
ADD CONSTRAINT unique_number_user_active UNIQUE (user_id, number, is_deleted);

-- Index for faster per-user lookups
CREATE INDEX idx_contacts_user_id ON contacts(user_id);

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Sample users (password stored as plain text for demo; use hashing in production)
INSERT INTO users (username, password, email) VALUES
('admin', 'admin123', 'admin@example.com'),
('alice', 'alice123', 'alice@example.com');

-- Sample contacts for user 1 (admin)
INSERT INTO contacts (user_id, name, number, email, category) VALUES
(1, 'John Doe',   '1234567890', 'john@example.com', 'Friends'),
(1, 'Jane Smith', '9876543210', 'jane@example.com', 'Family'),
(1, 'Bob Wilson', '5551234567', 'bob@example.com',  'Work');

-- Sample contacts for user 2 (alice)
INSERT INTO contacts (user_id, name, number, email, category) VALUES
(2, 'Carol King',  '1112223333', 'carol@example.com', 'Friends'),
(2, 'Dave Brown',  '4445556666', 'dave@example.com',  'Work');

-- Verify data
SELECT u.username, c.name, c.number, c.category
FROM users u
JOIN contacts c ON c.user_id = u.id
WHERE c.is_deleted = 0
ORDER BY u.username, c.name;
