import React, { useEffect, useState } from 'react';
import { Modal, Form, Input, Select, Switch, App as AntdApp } from 'antd';
import { useTranslation } from 'react-i18next';
import accessControlService, { UserRoleRequestDTO } from '../services/accessControlService';
import { UserRoleResponseDTO, ModuleRoleResponseDTO } from '../../users/types/userTypes';

interface UserRoleModalProps {
  open: boolean;
  role: UserRoleResponseDTO | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Modal component for creating or editing user access profiles.
 * Handles the mapping between high-level roles and granular module permissions.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const UserRoleModal: React.FC<UserRoleModalProps> = ({ open, role, onClose, onSuccess }) => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  
  const [catalog, setCatalog] = useState<ModuleRoleResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  /**
   * Loads the technical permissions catalog to populate the mapping selector.
   */
  useEffect(() => {
    const fetchCatalog = async () => {
      if (!open) return;
      setLoading(true);
      try {
        const data = await accessControlService.getModuleRoleCatalog();
        setCatalog(data.filter(item => item.active));
      } catch (error) {
        message.error(t('access.catalog_load_error'));
      } finally {
        setLoading(false);
      }
    };

    fetchCatalog();
  }, [open, t, message]);

  /**
   * Synchronizes form fields with the selected role for editing.
   */
  useEffect(() => {
    if (open) {
      if (role) {
        form.setFieldsValue({
          name: role.name,
          description: role.description,
          active: role.active,
          moduleRoleIds: role.moduleRoles.map(mr => mr.id),
        });
      } else {
        form.resetFields();
      }
    }
  }, [open, role, form]);

  /**
   * Handles the submission to the access control service.
   */
  const handleFinish = async (values: any) => {
    setSubmitting(true);
    try {
      const payload: UserRoleRequestDTO = {
        name: values.name,
        description: values.description,
        active: values.active,
        moduleRoleIds: values.moduleRoleIds || [],
      };

      if (role) {
        await accessControlService.updateUserRole(role.id, payload);
        message.success(t('access.profile_update_success'));
      } else {
        await accessControlService.createUserRole(payload);
        message.success(t('access.profile_create_success'));
      }
      onSuccess();
    } catch (error: any) {
      message.error(error.response?.data?.message || t('access.save_error'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={role ? t('access.edit_profile') : t('access.create_profile')}
      open={open}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={submitting}
      destroyOnClose
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleFinish}
        initialValues={{ active: true }}
        disabled={loading}
      >
        <Form.Item
          name="name"
          label={t('access.profile_name')}
          rules={[{ required: true, message: t('access.name_required') }]}
        >
          <Input placeholder="e.g. PRODUCTION_SUPERVISOR" />
        </Form.Item>

        <Form.Item
          name="description"
          label={t('access.profile_description')}
        >
          <Input.TextArea rows={2} />
        </Form.Item>

        <Form.Item
          name="moduleRoleIds"
          label={t('access.map_permissions')}
          extra={t('access.map_hint')}
        >
          <Select
            mode="multiple"
            placeholder={t('access.select_permissions')}
            loading={loading}
            allowClear
          >
            {catalog.map(item => (
              <Select.Option key={item.id} value={item.id}>
                {item.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="active"
          label={t('access.status')}
          valuePropName="checked"
        >
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserRoleModal;