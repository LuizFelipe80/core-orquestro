import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import enTranslations from './locales/en.json';
import ptBrTranslations from './locales/pt-BR.json';

/**
 * Resources object containing the mapping for each supported language.
 * Each key corresponds to a namespace defined in our JSON files.
 */
const resources = {
  en: {
    translation: enTranslations,
  },
  'pt-BR': {
    translation: ptBrTranslations,
  },
} as const;

/**
 * i18next Configuration.
 * Initializes the translation engine with:
 * - Browser language detection.
 * - React integration via hooks.
 * - Fallback to English if a translation is missing.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false, // React already protects against XSS
    },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    },
  });

export default i18n;