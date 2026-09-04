import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../../context/AuthContext';
import { UserResponseDTO, UserUpdateDTO, UserRoleResponseDTO } from '../types/userTypes';
import userService from '../services/userService';
import languageService, { LanguageResponseDTO } from '../../languages/services/languageService';
import userRoleService from '../services/userRoleService';

/**
 * Properties for the UserEditModal component.
 */
interface UserEditModalProps {
  open: boolean;
  user: UserResponseDTO | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Modal component for editing user information.
 * Updated to support multiple role selection (Indirect RBAC model).
 * Enforces privilege escalation prevention (only ADMINISTRATOR can assign/modify ADMINISTRATOR role).
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserEditModal: React.FC<UserEditModalProps> = ({ open, user, onClose, onSuccess }) => {
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
   * Fetches the necessary options for the form when opened.
   */
  useEffect(() => {
    const fetchOptions = async () => {
      if (!open) return;
      
      setLoading(true);
      try {
        const [langData, roleData] = await Promise.all([
          languageService.getActiveLanguages(),
          userRoleService.getAllRoles()
        ]);
        setLanguages(langData);
        setRoles(roleData);
      } catch (error) {
        message.error(t('users.load_options_error'));
      } finally {
        setLoading(false);
      }
    };

    fetchOptions();
  }, [open, t, message]);

  /**
   * Populates the form with the selected user's data.
   * Matches the array of roles directly with the multi-select component.
   */
  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        roles: user.roles, 
        languageId: user.language.id,
      });
    } else {
      form.resetFields();
    }
  }, [user, form]);

  /**
   * Processes the form submission.
   */
  const handleFinish = async (values: any) => {
    if (!user) return;

    setSubmitting(true);
    try {
      /* values.roles is already an array of strings due to mode="multiple" */
      const updateData: UserUpdateDTO = {
        ...values,
        languageId: values.languageId,
      };

      await userService.updateUser(user.id, updateData);
      message.success(t('users.update_success'));
      onSuccess();
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || t('users.update_error');
      message.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={t('users.edit_user')}
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

export default UserEditModal;