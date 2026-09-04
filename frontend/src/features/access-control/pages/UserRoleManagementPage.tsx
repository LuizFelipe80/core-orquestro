import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Space, Typography, Card, Button, Switch, App as AntdApp } from 'antd';
import { PlusOutlined, EditOutlined, TeamOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import accessControlService from '../services/accessControlService';
import { UserRoleResponseDTO } from '../../users/types/userTypes';
import UserRoleModal from '../components/UserRoleModal';

const { Title, Text } = Typography;

/**
 * User Role Management Page.
 * Interface for administrators to define high-level profiles and manage 
 * their association with granular module permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserRoleManagementPage: React.FC = () => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  
  const [roles, setRoles] = useState<UserRoleResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<UserRoleResponseDTO | null>(null);

  /**
   * Fetches the user role profiles from the backend.
   */
  const loadRoles = useCallback(async () => {
    setLoading(true);
    try {
      const data = await accessControlService.getUserRoles();
      setRoles(data);
    } catch (error) {
      message.error(t('access.profiles_load_error') || 'Failed to load access profiles.');
    } finally {
      setLoading(false);
    }
  }, [t, message]);

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  /**
   * Toggles the active status of a user role profile.
   */
  const handleToggleStatus = async (id: string) => {
    try {
      await accessControlService.toggleUserRoleStatus(id);
      message.success(t('access.status_updated'));
      loadRoles();
    } catch (error) {
      message.error(t('access.status_error'));
    }
  };

  /**
   * Opens the modal to create a new profile.
   */
  const handleCreate = () => {
    setSelectedRole(null);
    setIsModalOpen(true);
  };

  /**
   * Opens the modal to edit an existing profile.
   */
  const handleEdit = (role: UserRoleResponseDTO) => {
    setSelectedRole(role);
    setIsModalOpen(true);
  };

  /**
   * Closes the modal and refreshes data if requested.
   */
  const handleModalClose = (refresh = false) => {
    setIsModalOpen(false);
    setSelectedRole(null);
    if (refresh) loadRoles();
  };

  /**
   * Columns definition for the UserRoles table.
   */
  const columns: ColumnsType<UserRoleResponseDTO> = [
    {
      title: t('access.profile_name'),
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: t('access.profile_description'),
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: t('access.mapped_permissions'),
      key: 'permissions',
      render: (_, record) => (
        <Space size={[0, 4]} wrap>
          {record.moduleRoles.map(mr => (
            <Tag key={mr.id} color="geekblue" style={{ fontSize: '11px' }}>
              {mr.name}
            </Tag>
          ))}
          {record.moduleRoles.length === 0 && (
            <Text type="danger" italic style={{ fontSize: '12px' }}>
              {t('access.no_permissions_mapped')}
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: t('access.status'),
      dataIndex: 'active',
      key: 'status',
      width: 100,
      render: (active: boolean, record) => (
        <Switch 
          size="small" 
          checked={active} 
          onChange={() => handleToggleStatus(record.id)} 
        />
      ),
    },
    {
      title: t('common.actions'),
      key: 'actions',
      width: 80,
      fixed: 'right',
      render: (_, record) => (
        <Button 
          type="text" 
          icon={<EditOutlined />} 
          onClick={() => handleEdit(record)} 
        />
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false} style={{ boxShadow: '0 1px 2px rgba(0,0,0,0.03)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>
              <TeamOutlined /> {t('access.management_title') || 'Access Profiles'}
            </Title>
            <Text type="secondary">
              {t('access.management_subtitle') || 'Define high-level profiles by aggregating permissions from the catalog.'}
            </Text>
          </div>
          <Button 
            type="primary" 
            icon={<PlusOutlined />} 
            onClick={handleCreate}
          >
            {t('access.new_profile')}
          </Button>
        </div>
      </Card>

      <Table
        columns={columns}
        dataSource={roles}
        rowKey="id"
        loading={loading}
        pagination={false}
        scroll={{ x: 800 }}
      />

      <UserRoleModal
        open={isModalOpen}
        role={selectedRole}
        onClose={() => handleModalClose(false)}
        onSuccess={() => handleModalClose(true)}
      />
    </Space>
  );
};

export default UserRoleManagementPage;