/**
 * Interface representing the detailed user information received from the backend.
 * Matches the UserResponseDTO Java record.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */

/**
 * Interface for users to update their own basic profile information.
 */
export interface UserProfileUpdateDTO {
  firstName: string;
  lastName: string;
  languageId: string;
}

/**
 * Interface for secure password change operations.
 */
export interface PasswordChangeRequestDTO {
  currentPassword?: string;
  newPassword?: string;
}

export interface UserResponseDTO {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  globalRole: 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_USER';
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
 * Interface for the paginated response structure provided by Spring Data JPA.
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Interface for updating an existing user.
 * Matches the UserUpdateDTO Java record.
 */
export interface UserUpdateDTO {
  firstName: string;
  lastName: string;
  email: string;
  globalRole: string;
  languageId: string;
}