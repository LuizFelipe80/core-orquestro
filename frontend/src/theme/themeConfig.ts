import type { ThemeConfig } from 'antd';

/**
 * Custom Ant Design theme configuration for Orquestro.
 * Defines primary colors, border radius, and component-specific tokens.
 */
const theme: ThemeConfig = {
  token: {
    colorPrimary: '#102a43', // Deep Blue - Primary Identity
    colorInfo: '#102a43',
    borderRadius: 6,
    fontFamily: 'Inter, system-ui, sans-serif',
  },
  components: {
    Layout: {
      headerBg: '#ffffff',
      siderBg: '#001529',
    },
    Button: {
      fontWeight: 600,
    },
  },
};

export default theme;