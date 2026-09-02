import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
import userService from '../services/userService';
import languageService, { LanguageResponseDTO } from '../../languages/services/languageService';

interface UserCreateModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Modal component for administrative user creation.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserCreateModal: React.FC<UserCreateModalProps> = ({ open, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open) {
      form.resetFields();
      languageService.getActiveLanguages()
        .then(setLanguages)
        .catch(() => message.error(t('languages.load_error')));
    }
  }, [open, t, message, form]);

  const handleFinish = async (values: any) => {
    setSubmitting(true);
    try {
      // Reuses the backend registration logic
      await userService.createUser({
        ...values,
        languageCode: languages.find(l => l.id === values.languageId)?.code
      });
      message.success(t('users.create_success') || 'User created successfully.');
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
      title={t('users.create_user') || 'Create New User'}
      open={open}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={submitting}
      destroyOnClose
      width={500}
    >
      <Form form={form} layout="vertical" onFinish={handleFinish} initialValues={{ globalRole: 'ROLE_USER' }}>
        <Form.Item name="firstName" label={t('users.first_name')} rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="lastName" label={t('users.last_name')} rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="email" label={t('users.email')} rules={[{ required: true, type: 'email' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="password" label={t('auth.password_label')} rules={[{ required: true, min: 8 }]}>
          <Input.Password />
        </Form.Item>
        <Form.Item name="globalRole" label={t('users.role')} rules={[{ required: true }]}>
          <Select>
            <Select.Option value="ROLE_ADMIN">Administrator</Select.Option>
            <Select.Option value="ROLE_MANAGER">Manager</Select.Option>
            <Select.Option value="ROLE_USER">Standard User</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="languageId" label={t('users.language')} rules={[{ required: true }]}>
          <Select>
            {languages.map(lang => (
              <Select.Option key={lang.id} value={lang.id}>{lang.name}</Select.Option>
            ))}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserCreateModal;