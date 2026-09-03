/*
 * Database: PostgreSQL
 * Author: L.F. Desenvolvimento de Softwares LTDA
 * Description: Unified Core Schema with indirect RBAC (User -> UserRole -> ModuleRole).
 * This script consolidates all core entities and established the flexible permission hierarchy.
 */

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

/* 1. Core Localization */
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

/* 2. Identity Management */
CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    language_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_user_language FOREIGN KEY (language_id) REFERENCES languages (id)
);

/* 3. Session Management */
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

/* 4. Granular RBAC Foundation */

/* Catalog of specific business permissions defined by developers */
CREATE TABLE module_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

/* High-level profiles manageable by the administrator */
CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

/* Mapping: Which UserRole inherits which ModuleRoles */
CREATE TABLE user_role_module_mapping (
    user_role_id UUID NOT NULL,
    module_role_id UUID NOT NULL,
    PRIMARY KEY (user_role_id, module_role_id),
    CONSTRAINT fk_mapping_user_role FOREIGN KEY (user_role_id) REFERENCES user_roles (id),
    CONSTRAINT fk_mapping_module_role FOREIGN KEY (module_role_id) REFERENCES module_roles (id)
);

/* Assignment: Which User holds which UserRoles */
CREATE TABLE user_assigned_roles (
    user_id UUID NOT NULL,
    user_role_id UUID NOT NULL,
    PRIMARY KEY (user_id, user_role_id),
    CONSTRAINT fk_assignment_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_assignment_user_role FOREIGN KEY (user_role_id) REFERENCES user_roles (id)
);

/* 5. Seed Data - Initial Setup */

/* Languages */
INSERT INTO languages (id, name, code, active, is_default, created_at, version)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'English', 'en', TRUE, TRUE, NOW(), 0),
       ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Português', 'pt-BR', TRUE, FALSE, NOW(), 0);

/* Initial Module Roles (Permissions) */
INSERT INTO module_roles (id, name, description, created_at, version)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'OEE_ANALYST', 'Ability to view and analyze OEE metrics', NOW(), 0),
       ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', 'OEE_OPERATOR', 'Ability to input production data', NOW(), 0);

/* Initial User Roles (Profiles) */
INSERT INTO user_roles (id, name, description, created_at, version)
VALUES ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'ADMINISTRATOR', 'Full system access', NOW(), 0),
       ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c12', 'MANAGER', 'Management and oversight', NOW(), 0),
       ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c13', 'USER', 'Standard application user', NOW(), 0); -- Adicionado

/* Default Profile Mapping */
INSERT INTO user_role_module_mapping (user_role_id, module_role_id)
VALUES ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11'),
       ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12');

/* Seed Users (Password: password123) */
-- Administrator
INSERT INTO users (id, first_name, last_name, email, password, language_id, active, created_at, version)
VALUES ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'System', 'Administrator', 'admin@orquestro.com', 
        '$2a$10$CYbuQ2vQPB3omf755RcHU.RosCSPm7DcJtwlqCaJZvMxDS3NE6uUa', 
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), 0);

-- Standard User
INSERT INTO users (id, first_name, last_name, email, password, language_id, active, created_at, version)
VALUES ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Standard', 'User', 'user@orquestro.com', 
        '$2a$10$CYbuQ2vQPB3omf755RcHU.RosCSPm7DcJtwlqCaJZvMxDS3NE6uUa', 
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', TRUE, NOW(), 0); -- Adicionado

/* Assign Roles to Users */
INSERT INTO user_assigned_roles (user_id, user_role_id)
VALUES ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11'), -- Admin -> ADMINISTRATOR
       ('d8eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c13'); -- User -> USER