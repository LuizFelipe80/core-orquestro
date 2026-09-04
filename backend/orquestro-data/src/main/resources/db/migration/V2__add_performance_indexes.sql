/*
 * Database: PostgreSQL
 * Author: L.F. Desenvolvimento de Softwares LTDA
 * Description: Performance Indexes for Foreign Keys and Query Optimizations
 */

-- Index foreign keys for session management
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Index foreign keys for user localization
CREATE INDEX IF NOT EXISTS idx_users_language_id ON users(language_id);

-- Index foreign keys for role mappings to accelerate authorization lookups
CREATE INDEX IF NOT EXISTS idx_user_assigned_roles_role_id ON user_assigned_roles(user_role_id);
CREATE INDEX IF NOT EXISTS idx_user_role_module_mapping_module_role_id ON user_role_module_mapping(module_role_id);
