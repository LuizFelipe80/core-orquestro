import api from '../../../api/axios';
import { ModuleRoleResponseDTO, UserRoleResponseDTO } from '../../users/types/userTypes';

/**
 * Interface for creating or updating a high-level access profile.
 */
export interface UserRoleRequestDTO {
  name: string;
  description: string;
  active: boolean;
  moduleRoleIds: string[];
}

/**
 * Service responsible for managing the relationship between high-level profiles
 * and granular module permissions (Indirect RBAC).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const accessControlService = {
  /**
   * Retrieves the full catalog of module-specific roles (developer defined).
   * 
   * @returns A promise with the list of available module roles.
   */
  getModuleRoleCatalog: async (): Promise<ModuleRoleResponseDTO[]> => {
    const response = await api.get<ModuleRoleResponseDTO[]>('/module-roles');
    return response.data;
  },

  /**
   * Retrieves all high-level user profiles.
   * 
   * @returns A promise with the list of user roles.
   */
  getUserRoles: async (): Promise<UserRoleResponseDTO[]> => {
    const response = await api.get<UserRoleResponseDTO[]>('/user-roles');
    return response.data;
  },

  /**
   * Creates a new user profile and maps it to module roles.
   */
  createUserRole: async (data: UserRoleRequestDTO): Promise<UserRoleResponseDTO> => {
    const response = await api.post<UserRoleResponseDTO>('/user-roles', data);
    return response.data;
  },

  /**
   * Updates an existing profile and its permission mapping.
   */
  updateUserRole: async (id: string, data: UserRoleRequestDTO): Promise<UserRoleResponseDTO> => {
    const response = await api.put<UserRoleResponseDTO>(`/user-roles/${id}`, data);
    return response.data;
  },

  /**
   * Toggles the active status of a profile.
   */
  toggleUserRoleStatus: async (id: string): Promise<void> => {
    await api.patch(`/user-roles/${id}/status`);
  }
};

export default accessControlService;