import React, { useState } from 'react';
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

const { Header, Sider, Content } = Layout;
const { Text } = Typography;
const { useBreakpoint } = Grid;

/**
 * Main Layout component with Multi-language Selector.
 * Handles the application shell and provides global navigation and locale switching.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const MainLayout: React.FC = () => {
  const { t, i18n } = useTranslation();
  const [collapsed, setCollapsed] = useState(false);
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const screens = useBreakpoint();
  
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  /**
   * Sidebar Menu Items - Translated dynamically using t()
   */
  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: t('menu.dashboard'),
    },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: t('menu.users'),
      disabled: user?.globalRole === 'ROLE_USER',
    },
    {
      key: '/languages',
      icon: <GlobalOutlined />,
      label: t('menu.languages'),
      disabled: user?.globalRole === 'ROLE_USER',
    },
  ];

  /**
   * Language selection menu items.
   */
  const languageMenuItems = [
    {
      key: 'en',
      label: 'English',
      onClick: () => i18n.changeLanguage('en'),
      disabled: i18n.language === 'en',
    },
    {
      key: 'pt-BR',
      label: 'Português',
      onClick: () => i18n.changeLanguage('pt-BR'),
      disabled: i18n.language === 'pt-BR',
    },
  ];

  const userMenuItems = [
    { key: 'profile', label: t('menu.profile'), icon: <UserOutlined /> },
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
            {/* Language Selector Dropdown */}
            <Dropdown menu={{ items: languageMenuItems }} placement="bottomRight">
              <Button type="text" icon={<TranslationOutlined />} style={{ fontSize: '16px' }}>
                {screens.md && (i18n.language === 'en' ? 'EN' : 'PT')}
              </Button>
            </Dropdown>

            {/* User Menu Dropdown */}
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                {screens.md && (
                  <div style={{ textAlign: 'right', lineHeight: '1.2' }}>
                    <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
                    <Text type="secondary" style={{ fontSize: '12px' }}>{user?.globalRole}</Text>
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