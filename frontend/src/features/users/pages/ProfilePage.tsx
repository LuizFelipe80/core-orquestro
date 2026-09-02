import React, { useEffect, useState } from 'react';
import { Card, Tabs, Form, Input, Button, Select, Typography, Space, App as AntdApp } from 'antd';
import { UserOutlined, LockOutlined, GlobalOutlined, SaveOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import userService from '../services/userService';
import languageService, { LanguageResponseDTO } from '../../languages/services/languageService';
import { UserResponseDTO } from '../types/userTypes';

const { Title, Text } = Typography;

/**
 * User Profile Page.
 * Provides a self-service interface for users to update their personal 
 * information and change their security credentials.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const ProfilePage: React.FC = () => {
  const { t, i18n } = useTranslation();
  const { message } = AntdApp.useApp();
  
  const [profileForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);

  /**
   * Loads the current user's profile and the list of available languages.
   */
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const [profileData, activeLanguages] = await Promise.all([
          userService.getMyProfile(),
          languageService.getActiveLanguages()
        ]);
        
        setLanguages(activeLanguages);
        profileForm.setFieldsValue({
          firstName: profileData.firstName,
          lastName: profileData.lastName,
          languageId: profileData.language.id
        });
      } catch (error) {
        message.error(t('users.profile_load_error') || 'Failed to load profile information.');
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, [profileForm, t, message]);

  /**
   * Handles personal information updates.
   */
  const handleProfileUpdate = async (values: any) => {
    setSubmitting(true);
    try {
      const updatedUser = await userService.updateMyProfile(values);
      
      /* If the language was changed, update the UI localization immediately */
      if (updatedUser.language.code !== i18n.language) {
        i18n.changeLanguage(updatedUser.language.code);
      }
      
      message.success(t('users.profile_update_success') || 'Profile updated successfully.');
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || t('users.profile_update_error');
      message.error(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Handles secure password change.
   */
  const handlePasswordChange = async (values: any) => {
    setSubmitting(true);
    try {
      await userService.changeMyPassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword
      });
      message.success(t('users.password_update_success') || 'Password changed successfully.');
      passwordForm.resetFields();
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || t('users.password_update_error');
      message.error(errorMsg);
    } finally {
      setSubmitting(false);
    }
  };

  const tabItems = [
    {
      key: 'info',
      label: (
        <span>
          <UserOutlined /> {t('General') || 'General Information'}
        </span>
      ),
      children: (
        <Form 
          form={profileForm} 
          layout="vertical" 
          onFinish={handleProfileUpdate}
          disabled={loading}
        >
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <Form.Item
              name="firstName"
              label={t('users.first_name')}
              rules={[{ required: true, message: t('users.first_name_required') }]}
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="lastName"
              label={t('users.last_name')}
              rules={[{ required: true, message: t('users.last_name_required') }]}
            >
              <Input />
            </Form.Item>
          </div>
          <Form.Item
            name="languageId"
            label={t('users.language')}
            rules={[{ required: true, message: t('users.language_required') }]}
          >
            <Select>
              {languages.map(lang => (
                <Select.Option key={lang.id} value={lang.id}>
                  {lang.name} ({lang.code})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={submitting} 
              icon={<SaveOutlined />}
            >
              {t('save_changes') || 'Save Changes'}
            </Button>
          </Form.Item>
        </Form>
      ),
    },
    {
      key: 'security',
      label: (
        <span>
          <LockOutlined /> {t('Security') || 'Security'}
        </span>
      ),
      children: (
        <Form 
          form={passwordForm} 
          layout="vertical" 
          onFinish={handlePasswordChange}
          style={{ maxWidth: 400 }}
        >
          <Form.Item
            name="currentPassword"
            label={t('users.current_password')}
            rules={[{ required: true, message: t('users.current_password_required') }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label={t('users.new_password')}
            rules={[
              { required: true, message: t('users.new_password_required') },
              { min: 8, message: t('users.password_too_short') }
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label={t('users.confirm_password')}
            dependencies={['newPassword']}
            rules={[
              { required: true, message: t('users.confirm_password_required') },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error(t('users.password_mismatch') || 'Passwords do not match'));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={submitting} 
              danger
            >
              {t('users.update_password') || 'Update Password'}
            </Button>
          </Form.Item>
        </Form>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false} style={{ marginBottom: 0 }}>
        <Title level={4} style={{ margin: 0 }}>{t('menu.profile')}</Title>
        <Text type="secondary">{t('users.profile_description') || 'Manage your account settings and security preferences.'}</Text>
      </Card>
      
      <Card bordered={false} loading={loading}>
        <Tabs defaultActiveKey="info" items={tabItems} />
      </Card>
    </Space>
  );
};

export default ProfilePage;