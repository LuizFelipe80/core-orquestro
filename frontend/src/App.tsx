import { ConfigProvider, App as AntdApp } from 'antd';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes'; // Importando o novo mapa de rotas
import theme from './theme/themeConfig';
import './assets/styles/global.css';

/**
 * Main Application Component.
 */
function App() {
  return (
    <ConfigProvider theme={theme}>
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