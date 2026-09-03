import api from '../../../api/axios';
import { UserRoleResponseDTO } from '../types/userTypes';

/**
 * Service responsible for managing high-level access profiles (UserRoles).
 * Provides methods to retrieve profile definitions from the backend.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const userRoleService = {
  /**
   * Retrieves all user role profiles registered in the system.
   * Useful for administrative management and populating assignment selectors.
   * 
   * @returns A promise with the list of user role profiles.
   */
  getAllRoles: async (): Promise<UserRoleResponseDTO[]> => {
    const response = await api.get<UserRoleResponseDTO[]>('/user-roles');
    return response.data;
  },

  /**
   * Retrieves a specific user role by its ID.
   * 
   * @param id The unique identifier of the user role.
   * @returns A promise with the detailed user role data.
   */
  getRoleById: async (id: string): Promise<UserRoleResponseDTO> => {
    const response = await api.get<UserRoleResponseDTO>(`/user-roles/${id}`);
    return response.data;
  }
};

export default userRoleService;