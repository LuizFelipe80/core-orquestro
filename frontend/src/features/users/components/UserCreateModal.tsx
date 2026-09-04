import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../../context/AuthContext';
import userService from '../services/userService';
import languageService, { LanguageResponseDTO } from '../../languages/services/languageService';
import userRoleService from '../services/userRoleService';
import { UserCreateDTO, UserRoleResponseDTO } from '../types/userTypes';

/**
 * Properties for the UserCreateModal component.
 */
interface UserCreateModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Modal component for administrative user creation.
 * Dynamically fetches languages and access roles from the backend to populate the form.
 * Enforces privilege escalation prevention (only ADMINISTRATOR can grant ADMINISTRATOR).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserCreateModal: React.FC<UserCreateModalProps> = ({ open, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const { hasRole } = useAuth();
  const [form] = Form.useForm();
  
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);
  const [roles, setRoles] = useState<UserRoleResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const isCurrentUserAdmin = hasRole('ADMINISTRATOR');

  /**
   * Fetches required data for the form when the modal opens.
   */
  useEffect(() => {
    const fetchInitialData = async () => {
      if (!open) return;
      
      setLoading(true);
      try {
        const [languageData, roleData] = await Promise.all([
          languageService.getActiveLanguages(),
          userRoleService.getAllRoles()
        ]);
        
        setLanguages(languageData);
        setRoles(roleData);
      } catch (error) {
        message.error(t('users.load_options_error') || 'Failed to load form options.');
      } finally {
        setLoading(false);
      }
    };

    if (open) {
      form.resetFields();
      fetchInitialData();
    }
  }, [open, form, t, message]);

  /**
   * Handles the submission of the user creation form.
   * 
   * @param values The validated form data.
   */
  const handleFinish = async (values: any) => {
    setSubmitting(true);
    try {
      const payload: UserCreateDTO = {
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        password: values.password,
        languageId: values.languageId,
        roles: values.roles || [],
      };

      await userService.createUser(payload);
      message.success(t('users.create_success'));
      onSuccess();
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || t('users.create_error');
      message.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={t('users.create_user')}
      open={open}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={submitting}
      destroyOnClose
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        disabled={loading}
      >
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

        <Form.Item
          name="email"
          label={t('users.email')}
          rules={[
            { required: true, message: t('users.email_required') },
            { type: 'email', message: t('users.email_invalid') }
          ]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          name="password"
          label={t('auth.password_label')}
          rules={[
            { required: true, message: t('auth.password_required') },
            { min: 8, message: t('users.password_too_short') }
          ]}
        >
          <Input.Password />
        </Form.Item>

        <Form.Item
          name="roles"
          label={t('users.role')}
          rules={[{ required: true, message: t('users.role_required') }]}
        >
          <Select 
            mode="multiple"
            placeholder={t('users.select_roles')}
            loading={loading}
            allowClear
          >
            {roles
              .filter(role => isCurrentUserAdmin || role.name !== 'ADMINISTRATOR')
              .map(role => (
                <Select.Option key={role.id} value={role.name}>
                  {role.name}
                </Select.Option>
              ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="languageId"
          label={t('users.language')}
          rules={[{ required: true, message: t('users.language_required') }]}
        >
          <Select loading={loading}>
            {languages.map(lang => (
              <Select.Option key={lang.id} value={lang.id}>
                {lang.name} ({lang.code})
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserCreateModal;