import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Switch, Space, Typography, Card, Button, Tooltip, App as AntdApp } from 'antd';
import { 
  EditOutlined, 
  UnlockOutlined, 
  LockOutlined, 
  UserAddOutlined 
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import { UserResponseDTO } from '../types/userTypes';
import userService from '../services/userService';
import UserEditModal from '../components/UserEditModal';
import UserCreateModal from '../components/UserCreateModal';

const { Title } = Typography;

/**
 * Enhanced User List Page supporting multiple roles (Indirect RBAC).
 * Displays user information with a collection of role tags and provides 
 * administrative actions for account management and security.
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
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserResponseDTO | null>(null);

  /**
   * Loads the paginated user data from the backend.
   */
  const loadUsers = useCallback(async (page: number) => {
    setLoading(true);
    try {
      const data = await userService.getUsers(page - 1, pageSize);
      setUsers(data.content);
      setTotalElements(data.totalElements);
    } catch (error: any) {
      message.error(t('users.load_error'));
    } finally {
      setLoading(false);
    }
  }, [pageSize, t, message]);

  useEffect(() => {
    loadUsers(currentPage);
  }, [loadUsers, currentPage]);

  /**
   * Toggles the active status of a user.
   */
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
   * Triggers the account unlock confirmation and process.
   */
  const handleUnlock = (user: UserResponseDTO) => {
    modal.confirm({
      title: t('users.unlock_title'),
      content: t('users.unlock_confirm', { name: user.firstName }),
      okText: t('common.yes'),
      cancelText: t('common.no'),
      onOk: async () => {
        try {
          await userService.unlockUser(user.id);
          message.success(t('users.unlock_success'));
          loadUsers(currentPage);
        } catch (error: any) {
          message.error(t('users.unlock_error'));
        }
      },
    });
  };

  /**
   * Column definitions for the Ant Design Table.
   */
  const columns: ColumnsType<UserResponseDTO> = [
    {
      title: t('users.full_name'),
      key: 'name',
      render: (_, record) => (
        <Space>
          {`${record.firstName} ${record.lastName}`}
          {record.accountLocked && (
            <Tooltip title={t('users.locked_hint')}>
              <Tag color="error" icon={<LockOutlined />}>LOCKED</Tag>
            </Tooltip>
          )}
        </Space>
      ),
    },
    {
      title: t('users.email'),
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: t('users.role'),
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: string[]) => (
        <Space size={[0, 4]} wrap>
          {roles.map(role => {
            let color = 'default';
            if (role === 'ADMINISTRATOR') color = 'blue';
            if (role === 'MANAGER') color = 'cyan';
            return <Tag color={color} key={role}>{role}</Tag>;
          })}
        </Space>
      ),
    },
    {
      title: t('users.status'),
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
      title: t('common.actions'),
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
            <Tooltip title={t('users.unlock_action')}>
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} style={{ margin: 0 }}>{t('menu.users')}</Title>
          <Button 
            type="primary" 
            icon={<UserAddOutlined />} 
            onClick={() => setIsCreateModalOpen(true)}
          >
            {t('users.new_user')}
          </Button>
        </div>
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

      <UserCreateModal 
        open={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={() => {
          setIsCreateModalOpen(false);
          loadUsers(currentPage);
        }}
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