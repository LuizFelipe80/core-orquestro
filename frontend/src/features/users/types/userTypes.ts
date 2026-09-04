/**
 * Detailed information about a module-specific permission.
 */
export interface ModuleRoleResponseDTO {
  id: string;
  name: string;
  description: string;
  active: boolean;
}

/**
 * High-level access profile that aggregates multiple module roles.
 */
export interface UserRoleResponseDTO {
  id: string;
  name: string;
  description: string;
  active: boolean;
  moduleRoles: {
    id: string;
    name: string;
  }[];
}

/**
 * Interface representing user data.
 * Supports multiple roles inherited from the indirect RBAC model.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
export interface UserResponseDTO {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  active: boolean;
  accountLocked: boolean;
  lastLoginAt: string | null;
  createdAt: string;
  language: {
    id: string;
    name: string;
    code: string;
  };
}

/**
 * Payload for administrative user creation.
 */
export interface UserCreateDTO {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  languageId: string;
  roles: string[];
}

/**
 * Generic interface for paginated data from Spring Data JPA.
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Payload for administrative user updates.
 * Updated to support multiple roles assignment.
 */
export interface UserUpdateDTO {
  firstName: string;
  lastName: string;
  email: string;
  roles: string[]; 
  languageId: string;
}

/**
 * Payload for self-service profile updates.
 */
export interface UserProfileUpdateDTO {
  firstName: string;
  lastName: string;
  languageId: string;
}

/**
 * Payload for secure password change.
 */
export interface PasswordChangeRequestDTO {
  currentPassword?: string;
  newPassword?: string;
}