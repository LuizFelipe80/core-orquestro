import api from '../../../api/axios';

/**
 * Interface representing a language response from the API.
 */
export interface LanguageResponseDTO {
  id: string;
  name: string;
  code: string;
  isDefault: boolean;
}

/**
 * Service responsible for language-related API calls.
 * Used primarily to populate selectors and manage localization settings.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const languageService = {
  /**
   * Retrieves only the languages marked as active in the system.
   * 
   * @returns A promise with the list of active languages.
   */
  getActiveLanguages: async (): Promise<LanguageResponseDTO[]> => {
    const response = await api.get<LanguageResponseDTO[]>('/languages/active');
    return response.data;
  },

  /**
   * Retrieves all languages (including inactive ones) for administrative management.
   * 
   * @returns A promise with the full list of languages.
   */
  getAllLanguages: async (): Promise<LanguageResponseDTO[]> => {
    const response = await api.get<LanguageResponseDTO[]>('/languages');
    return response.data;
  }
};

export default languageService;