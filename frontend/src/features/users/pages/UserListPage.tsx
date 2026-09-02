import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Switch, Space, Typography, Card, Button, Tooltip, App as AntdApp } from 'antd';
import { EditOutlined, UnlockOutlined, LockOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import { UserResponseDTO } from '../types/userTypes';
import userService from '../services/userService';
import UserEditModal from '../components/UserEditModal';

const { Title } = Typography;

/**
 * Enhanced User List Page with security management.
 * Provides visual indicators for locked accounts and administrative unlock actions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserListPage: React.FC = () => {
  const { t } = useTranslation();
  const { message, modal } = AntdApp.useApp();
  
  const [users, setUsers] = useState<UserResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserResponseDTO | null>(null);

  const loadUsers = useCallback(async (page: number) => {
    setLoading(true);
    try {
      const data = await userService.getUsers(page - 1, pageSize);
      setUsers(data.content);
      setTotalElements(data.totalElements);
    } catch (error: any) {
      message.error(t('users.load_error') || 'Failed to load users.');
    } finally {
      setLoading(false);
    }
  }, [pageSize, t, message]);

  useEffect(() => {
    loadUsers(currentPage);
  }, [loadUsers, currentPage]);

  const handleToggleStatus = async (id: string) => {
    try {
      await userService.toggleUserStatus(id);
      message.success(t('users.status_updated'));
      loadUsers(currentPage);
    } catch (error: any) {
      message.error(t('users.status_error'));
    }
  };

  /**
   * Handles the account unlock process with a confirmation dialog.
   */
  const handleUnlock = (user: UserResponseDTO) => {
    modal.confirm({
      title: t('users.unlock_title') || 'Unlock Account',
      content: t('users.unlock_confirm', { name: user.firstName }) || `Are you sure you want to unlock ${user.firstName}'s account?`,
      okText: t('Procced') || 'Yes',
      cancelText: t('Cancel') || 'No',
      onOk: async () => {
        try {
          await userService.unlockUser(user.id);
          message.success(t('users.unlock_success') || 'Account successfully unlocked.');
          loadUsers(currentPage);
        } catch (error: any) {
          message.error(t('users.unlock_error') || 'Failed to unlock account.');
        }
      },
    });
  };

  const columns: ColumnsType<UserResponseDTO> = [
    {
      title: t('users.full_name') || 'Name',
      key: 'name',
      render: (_, record) => (
        <Space>
          {`${record.firstName} ${record.lastName}`}
          {record.accountLocked && (
            <Tooltip title={t('users.locked_hint') || 'Locked by brute force protection'}>
              <Tag color="error" icon={<LockOutlined />}>LOCKED</Tag>
            </Tooltip>
          )}
        </Space>
      ),
    },
    {
      title: t('users.email') || 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: t('users.role') || 'Role',
      dataIndex: 'globalRole',
      key: 'role',
      render: (role: string) => {
        let color = 'default';
        if (role === 'ROLE_ADMIN') color = 'blue';
        if (role === 'ROLE_MANAGER') color = 'cyan';
        return <Tag color={color}>{role.replace('ROLE_', '')}</Tag>;
      },
    },
    {
      title: t('users.status') || 'Active',
      dataIndex: 'active',
      key: 'status',
      render: (active: boolean, record) => (
        <Switch 
          checked={active} 
          onChange={() => handleToggleStatus(record.id)} 
          size="small"
        />
      ),
    },
    {
      title: t('common.actions') || 'Actions',
      key: 'actions',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="text" 
            icon={<EditOutlined />} 
            onClick={() => {
              setSelectedUser(record);
              setIsEditModalOpen(true);
            }}
          />
          {record.accountLocked && (
            <Tooltip title={t('users.unlock_action') || 'Unlock Account'}>
              <Button 
                type="text" 
                danger
                icon={<UnlockOutlined />} 
                onClick={() => handleUnlock(record)}
              />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false} style={{ boxShadow: '0 1px 2px rgba(0,0,0,0.03)' }}>
        <Title level={4} style={{ margin: 0 }}>{t('menu.users')}</Title>
      </Card>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{
          current: currentPage,
          total: totalElements,
          onChange: (page) => setCurrentPage(page),
          showSizeChanger: false,
        }}
        scroll={{ x: 1000 }}
      />

      <UserEditModal 
        open={isEditModalOpen}
        user={selectedUser}
        onClose={() => {
          setIsEditModalOpen(false);
          setSelectedUser(null);
        }}
        onSuccess={() => {
          setIsEditModalOpen(false);
          setSelectedUser(null);
          loadUsers(currentPage);
        }}
      />
    </Space>
  );
};

export default UserListPage;