import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Switch, Space, Typography, Card, Button, App as AntdApp } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import { UserResponseDTO } from '../types/userTypes';
import userService from '../services/userService';
import UserEditModal from '../components/UserEditModal';

const { Title } = Typography;

/**
 * User List Page Component.
 * Displays a paginated table of users and manages the lifecycle 
 * of user editing and status toggling.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserListPage: React.FC = () => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  
  /* State for data and pagination */
  const [users, setUsers] = useState<UserResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);

  /* State for Edit Modal */
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserResponseDTO | null>(null);

  /**
   * Fetches the paginated user list from the service layer.
   */
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

  /**
   * Opens the edit modal for a specific user.
   */
  const handleEditClick = (user: UserResponseDTO) => {
    setSelectedUser(user);
    setIsEditModalOpen(true);
  };

  /**
   * Closes the modal and clears the selection.
   */
  const handleModalClose = () => {
    setIsEditModalOpen(false);
    setSelectedUser(null);
  };

  /**
   * Refreshes the list and closes the modal after a successful update.
   */
  const handleModalSuccess = () => {
    handleModalClose();
    loadUsers(currentPage);
  };

  /**
   * Handles the activation/deactivation of a user account.
   */
  const handleToggleStatus = async (id: string) => {
    try {
      await userService.toggleUserStatus(id);
      message.success(t('users.status_updated') || 'User status updated successfully.');
      loadUsers(currentPage);
    } catch (error: any) {
      message.error(t('users.status_error') || 'Failed to update user status.');
    }
  };

  /**
   * Table columns definition including the new Actions column.
   */
  const columns: ColumnsType<UserResponseDTO> = [
    {
      title: t('users.full_name') || 'Name',
      key: 'name',
      render: (_, record) => `${record.firstName} ${record.lastName}`,
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
        const label = role.replace('ROLE_', '');
        return <Tag color={color}>{label}</Tag>;
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
      width: 100,
      render: (_, record) => (
        <Button 
          type="text" 
          icon={<EditOutlined />} 
          onClick={() => handleEditClick(record)}
          title={t('common.edit') || 'Edit'}
        />
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false} style={{ boxShadow: '0 1px 2px rgba(0,0,0,0.03)' }}>
        <Title level={4} style={{ margin: 0 }}>
          {t('menu.users') || 'Users Management'}
        </Title>
      </Card>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{
          current: currentPage,
          pageSize: pageSize,
          total: totalElements,
          onChange: (page) => setCurrentPage(page),
          showSizeChanger: false,
          position: ['bottomRight']
        }}
        scroll={{ x: 1000 }}
      />

      <UserEditModal 
        open={isEditModalOpen}
        user={selectedUser}
        onClose={handleModalClose}
        onSuccess={handleModalSuccess}
      />
    </Space>
  );
};

export default UserListPage;