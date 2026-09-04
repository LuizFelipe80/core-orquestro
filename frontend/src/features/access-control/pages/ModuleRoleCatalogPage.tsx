import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Typography, Card, Space, App as AntdApp } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import { InfoCircleOutlined, SecurityScanOutlined } from '@ant-design/icons';
import accessControlService from '../services/accessControlService';
import { ModuleRoleResponseDTO } from '../../users/types/userTypes';

const { Title, Text } = Typography;

/**
 * Module Role Catalog Page.
 * provides a read-only view of the technical permissions defined in the system.
 * This catalog helps administrators understand the business roles available 
 * for mapping into high-level user profiles.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const ModuleRoleCatalogPage: React.FC = () => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  
  const [roles, setRoles] = useState<ModuleRoleResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);

  /**
   * Fetches the module role catalog from the backend.
   */
  const loadCatalog = useCallback(async () => {
    setLoading(true);
    try {
      const data = await accessControlService.getModuleRoleCatalog();
      setRoles(data);
    } catch (error: any) {
      message.error(t('access.catalog_load_error') || 'Failed to load the permissions catalog.');
    } finally {
      setLoading(false);
    }
  }, [t, message]);

  useEffect(() => {
    loadCatalog();
  }, [loadCatalog]);

  /**
   * Table columns definition for the catalog.
   */
  const columns: ColumnsType<ModuleRoleResponseDTO> = [
    {
      title: t('access.role_name') || 'Permission Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => (
        <Text strong style={{ color: '#102a43' }}>
          <SecurityScanOutlined /> {name}
        </Text>
      ),
    },
    {
      title: t('access.role_description') || 'Description',
      dataIndex: 'description',
      key: 'description',
      render: (text: string) => <Text type="secondary">{text}</Text>,
    },
    {
      title: t('access.status') || 'Status',
      dataIndex: 'active',
      key: 'active',
      width: 120,
      render: (active: boolean) => (
        <Tag color={active ? 'success' : 'error'}>
          {active ? t('common.active') : t('common.inactive')}
        </Tag>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false} style={{ boxShadow: '0 1px 2px rgba(0,0,0,0.03)' }}>
        <Title level={4} style={{ margin: 0 }}>
          {t('access.catalog_title') || 'Permissions Catalog'}
        </Title>
        <Text type="secondary">
          <InfoCircleOutlined /> {t('access.catalog_subtitle') || 'Technical business roles defined by system developers. These roles are used to compose user profiles.'}
        </Text>
      </Card>

      <Table
        columns={columns}
        dataSource={roles}
        rowKey="id"
        loading={loading}
        pagination={false}
        locale={{ emptyText: t('access.no_roles_found') || 'No permissions found in the catalog.' }}
      />
    </Space>
  );
};

export default ModuleRoleCatalogPage;
