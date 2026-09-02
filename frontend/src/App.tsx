import { useState, useEffect } from 'react';
import { ConfigProvider, App as AntdApp } from 'antd';
import { BrowserRouter } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/* Ant Design Locales */
import enUS from 'antd/locale/en_US';
import ptBR from 'antd/locale/pt_BR';
import { Locale } from 'antd/es/locale';

/* Internal Imports */
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes';
import theme from './theme/themeConfig';
import './i18n/config'; 
import './assets/styles/global.css';

/**
 * Mapping object to correlate i18next language codes with Ant Design locale objects.
 */
const antLocales: Record<string, Locale> = {
  'en': enUS,
  'pt-BR': ptBR,
};

/**
 * Main Application Component.
 * Orchestrates the configuration providers for Theme (AntD), Localization (i18n),
 * Authentication (Context), and Routing.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
function App() {
  const { i18n } = useTranslation();
  const [currentLocale, setCurrentLocale] = useState<Locale>(antLocales[i18n.language] || enUS);

  /**
   * Effect to synchronize Ant Design's internal language with i18next state changes.
   */
  useEffect(() => {
    const handleLanguageChange = (lng: string) => {
      // Normalizes the language code to match our mapping
      const normalizedLng = lng.split('-')[0] === 'pt' ? 'pt-BR' : 'en';
      setCurrentLocale(antLocales[normalizedLng] || enUS);
    };

    i18n.on('languageChanged', handleLanguageChange);

    return () => {
      i18n.off('languageChanged', handleLanguageChange);
    };
  }, [i18n]);

  return (
    <ConfigProvider theme={theme} locale={currentLocale}>
      <AntdApp>
        <AuthProvider>
          <BrowserRouter>
            <AppRoutes />
          </BrowserRouter>
        </AuthProvider>
      </AntdApp>
    </ConfigProvider>
  );
}

export default App;