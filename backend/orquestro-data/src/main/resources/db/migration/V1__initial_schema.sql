-- Database: PostgreSQL
-- Author: L.F. Desenvolvimento de Softwares LTDA
-- Description: Initial schema creation and seed data for the Orquestro platform.

-- Enable pgcrypto for UUID generation if needed (standard in many Postgres setups)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Create table 'languages'
CREATE TABLE languages (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 2. Create table 'users'
CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    global_role VARCHAR(50) NOT NULL,
    language_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_user_language FOREIGN KEY (language_id) REFERENCES languages (id)
);

-- 3. Create table 'user_sessions'
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    user_agent VARCHAR(255),
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 4. Seed Data: Initial Languages
-- English (Default)
INSERT INTO languages (id, name, code, active, is_default, created_at, version)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'English', 'en', TRUE, TRUE, NOW(), 0);

-- Portuguese
INSERT INTO languages (id, name, code, active, is_default, created_at, version)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Português', 'pt-BR', TRUE, FALSE, NOW(), 0);

-- 5. Seed Data: Initial Users
-- Password for all seed users: password123
-- BCrypt hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DM99Xo7N95S.

-- Administrator
INSERT INTO users (id, first_name, last_name, email, password, global_role, language_id, active, created_at, version)
VALUES ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'System', 'Administrator', 'admin@orquestro.com', 
        '$2a$10$CYbuQ2vQPB3omf755RcHU.RosCSPm7DcJtwlqCaJZvMxDS3NE6uUa', 'ROLE_ADMIN', 
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), 0);

-- Standard User
INSERT INTO users (id, first_name, last_name, email, password, global_role, language_id, active, created_at, version)
VALUES ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Standard', 'User', 'user@orquestro.com', 
        '$2a$10$CYbuQ2vQPB3omf755RcHU.RosCSPm7DcJtwlqCaJZvMxDS3NE6uUa', 'ROLE_USER', 
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), 0);