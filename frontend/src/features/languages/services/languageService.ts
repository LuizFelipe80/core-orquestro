import api from '../../../api/axios';

export interface LanguageResponseDTO {
  id: string;
  name: string;
  code: string;
  active: boolean;
  isDefault: boolean;
}

export interface LanguageUpdateDTO {
  name: string;
  code: string;
  active: boolean;
  isDefault: boolean;
}

/**
 * Service for managing language configurations.
 * Focused on activation and system default settings.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const languageService = {
  getAllLanguages: async (): Promise<LanguageResponseDTO[]> => {
    const response = await api.get<LanguageResponseDTO[]>('/languages');
    return response.data;
  },

  getActiveLanguages: async (): Promise<LanguageResponseDTO[]> => {
    const response = await api.get<LanguageResponseDTO[]>('/languages/active');
    return response.data;
  },

  /**
   * Updates language status or default settings.
   */
  updateLanguage: async (id: string, data: LanguageUpdateDTO): Promise<LanguageResponseDTO> => {
    const response = await api.put<LanguageResponseDTO>(`/languages/${id}`, data);
    return response.data;
  }
};

export default languageService;