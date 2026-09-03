import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
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
 * Dynamically fetches roles and languages to ensure data consistency 
 * with the backend's entity-based RBAC model.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserEditModal: React.FC<UserEditModalProps> = ({ open, user, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);
  const [roles, setRoles] = useState<UserRoleResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  /**
   * Loads the necessary form options (languages and roles) when the modal opens.
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
   * Syncs the form fields with the selected user's data.
   * Note: It takes the first role from the array as the primary role for the selector.
   */
  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        globalRole: user.roles[0] || '',
        languageId: user.language.id,
      });
    } else {
      form.resetFields();
    }
  }, [user, form]);

  /**
   * Processes the update request.
   * 
   * @param values The form values to be sent to the server.
   */
  const handleFinish = async (values: any) => {
    if (!user) return;

    setSubmitting(true);
    try {
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
          name="globalRole"
          label={t('users.role')}
          rules={[{ required: true, message: t('users.role_required') }]}
        >
          <Select loading={loading}>
            {roles.map(role => (
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