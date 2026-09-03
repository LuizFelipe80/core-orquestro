import React, { useState, useEffect, useCallback } from 'react';
import { Layout, Menu, Button, theme, Avatar, Dropdown, Space, Typography, Grid } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserOutlined,
  GlobalOutlined,
  LogoutOutlined,
  TranslationOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import languageService, { LanguageResponseDTO } from '../features/languages/services/languageService';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;
const { useBreakpoint } = Grid;

/**
 * Main Layout with Dynamic Language Selection.
 * Syncs the header language dropdown with the active languages in the database.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const MainLayout: React.FC = () => {
  const { t, i18n } = useTranslation();
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const screens = useBreakpoint();
  
  const [collapsed, setCollapsed] = useState(false);
  const [activeLanguages, setActiveLanguages] = useState<LanguageResponseDTO[]>([]);
  
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  /**
   * Fetches the list of active languages from the backend to populate the dropdown.
   */
  const fetchActiveLanguages = useCallback(async () => {
    try {
      const data = await languageService.getActiveLanguages();
      setActiveLanguages(data);
    } catch (error) {
      console.error('Failed to load active languages for the header selector.');
    }
  }, []);

  useEffect(() => {
    fetchActiveLanguages();
  }, [fetchActiveLanguages]);

  /**
   * Maps active languages to Ant Design Menu items.
   */
  const languageMenuItems = activeLanguages.map(lang => ({
    key: lang.code,
    label: lang.name,
    onClick: () => i18n.changeLanguage(lang.code),
    disabled: i18n.language === lang.code,
  }));

  const menuItems = [
    { key: '/', icon: <DashboardOutlined />, label: t('menu.dashboard') },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: t('menu.users'),
      disabled: !user?.roles.includes('ADMINISTRATOR') && !user?.roles.includes('MANAGER'),
    },
    {
      key: '/languages',
      icon: <GlobalOutlined />,
      label: t('menu.languages'),
      disabled: !user?.roles.includes('ADMINISTRATOR') && !user?.roles.includes('MANAGER'),
    },
  ];

  const userMenuItems = [
    { 
      key: 'profile', 
      label: t('menu.profile'), 
      icon: <UserOutlined />, 
      onClick: () => navigate('/profile') 
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      label: t('menu.sign_out'),
      icon: <LogoutOutlined />,
      danger: true,
      onClick: () => signOut(),
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} breakpoint="lg" onBreakpoint={(broken) => setCollapsed(broken)}>
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 'bold' }}>
          {collapsed ? 'O' : 'ORQUESTRO'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      
      <Layout>
        <Header style={{ padding: '0 16px', background: colorBgContainer, display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 1px 4px rgba(0,21,41,.08)', zIndex: 1 }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: '16px', width: 64, height: 64 }}
          />

          <Space size="large" style={{ paddingRight: 8 }}>
            <Dropdown menu={{ items: languageMenuItems }} placement="bottomRight">
              <Button type="text" icon={<TranslationOutlined />} style={{ fontSize: '16px' }}>
                {screens.md && (i18n.language.toUpperCase().split('-')[0])}
              </Button>
            </Dropdown>

            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                {screens.md && (
                  <div style={{ textAlign: 'right', lineHeight: '1.2' }}>
                    <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
                    <Text type="secondary" style={{ fontSize: '12px' }}>{user?.roles.join(', ')}</Text>
                  </div>
                )}
                <Avatar style={{ backgroundColor: '#102a43' }} icon={<UserOutlined />} />
              </Space>
            </Dropdown>
          </Space>
        </Header>

        <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280, background: colorBgContainer, borderRadius: borderRadiusLG }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;