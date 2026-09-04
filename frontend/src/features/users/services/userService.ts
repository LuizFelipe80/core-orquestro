import api from '../../../api/axios';
import { UserResponseDTO, UserUpdateDTO, UserCreateDTO, PaginatedResponse, UserProfileUpdateDTO, PasswordChangeRequestDTO } from '../types/userTypes';

/**
 * Service responsible for user-related API calls.
 * Encapsulates the logic for fetching and manipulating user data
 * while maintaining type safety through TypeScript interfaces.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const userService = {
  /**
   * Fetches a paginated list of users from the backend.
   * 
   * @param page The page index (starting from 0).
   * @param size The number of items per page.
   * @returns A promise with the paginated user data.
   */
  getUsers: async (page = 0, size = 20): Promise<PaginatedResponse<UserResponseDTO>> => {
    const response = await api.get<PaginatedResponse<UserResponseDTO>>('/users', {
      params: { page, size, sort: 'firstName,asc' }
    });
    return response.data;
  },

  /**
   * Fetches a single user by their unique identifier.
   * 
   * @param id The UUID of the user.
   * @returns A promise with the user details.
   */
  getUserById: async (id: string): Promise<UserResponseDTO> => {
    const response = await api.get<UserResponseDTO>(`/users/${id}`);
    return response.data;
  },

  /**
   * Updates an existing user's information.
   * 
   * @param id The UUID of the user to update.
   * @param data The updated user data.
   * @returns A promise with the updated user details.
   */
  updateUser: async (id: string, data: UserUpdateDTO): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>(`/users/${id}`, data);
    return response.data;
  },

  /**
   * Toggles the active/inactive status of a user.
   * 
   * @param id The UUID of the user.
   * @returns A promise that resolves when the operation is complete.
   */
  toggleUserStatus: async (id: string): Promise<void> => {
    await api.patch(`/users/${id}/status`);
  },

  /**
   * Unlocks a user account that was blocked by brute force protection.
   * 
   * @param id The unique identifier of the user to unlock.
   * @returns A promise that resolves when the operation is complete.
   */
  unlockUser: async (id: string): Promise<void> => {
    await api.patch(`/users/${id}/unlock`);
  },

  /* Self-Service Methods (Authenticated User) */
  
  /**
   * Fetches the profile of the currently logged-in user.
   */
  getMyProfile: async (): Promise<UserResponseDTO> => {
    const response = await api.get<UserResponseDTO>('/users/me');
    return response.data;
  },

  /**
   * Updates the current user's profile names and language.
   */
  updateMyProfile: async (data: UserProfileUpdateDTO): Promise<UserResponseDTO> => {
    const response = await api.put<UserResponseDTO>('/users/me', data);
    return response.data;
  },

  /**
   * Changes the current user's password.
   */
  changeMyPassword: async (data: PasswordChangeRequestDTO): Promise<void> => {
    await api.patch('/users/me/password', data);
  },

  /**
   * Administratively creates a new user.
   */
  createUser: async (data: UserCreateDTO): Promise<UserResponseDTO> => {
    const response = await api.post<UserResponseDTO>('/users', data);
    return response.data;
  },
};

export default userService;