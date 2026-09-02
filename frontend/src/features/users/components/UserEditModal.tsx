import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
import { UserResponseDTO, UserUpdateDTO } from '../types/userTypes';
import userService from '../services/userService';
import languageService, { LanguageResponseDTO } from '../../languages/services/languageService';

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
 * Manages its own internal state for languages and form submission logic.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserEditModal: React.FC<UserEditModalProps> = ({ open, user, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);
  const [submitting, setSubmitting] = useState(false);

  /**
   * Loads the list of active languages to populate the selection field.
   */
  useEffect(() => {
    if (open) {
      languageService.getActiveLanguages()
        .then(setLanguages)
        .catch(() => message.error(t('languages.load_error')));
    }
  }, [open, t, message]);

  /**
   * Resets and populates the form whenever the selected user changes.
   */
  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        globalRole: user.globalRole,
        languageId: user.language.id,
      });
    } else {
      form.resetFields();
    }
  }, [user, form]);

  /**
   * Handles the form submission by calling the user service.
   * 
   * @param values The validated form data.
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
      maskClosable={false}
      width={500}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        initialValues={{ globalRole: 'ROLE_USER' }}
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
          <Select>
            <Select.Option value="ROLE_ADMIN">Administrator</Select.Option>
            <Select.Option value="ROLE_MANAGER">Manager</Select.Option>
            <Select.Option value="ROLE_USER">Standard User</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="languageId"
          label={t('users.language')}
          rules={[{ required: true, message: t('users.language_required') }]}
        >
          <Select loading={languages.length === 0}>
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