import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, App as AntdApp } from 'antd';
import { MailOutlined, LockOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../../context/AuthContext';

const { Title, Text } = Typography;

/**
 * Login Page Component with Internationalization support.
 * Uses i18next to provide localized labels and messages.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const LoginPage: React.FC = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const { signIn } = useAuth();
  const { message } = AntdApp.useApp();

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await signIn(values);
      // t('auth.welcome_back') uses interpolation
      message.success(t('auth.welcome_back', { name: values.email }));
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || t('auth.login_error_generic');
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #102a43 0%, #243b53 100%)'
    }}>
      <Card style={{ width: '100%', maxWidth: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }} bordered={false}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={2} style={{ margin: 0, color: '#102a43' }}>{t('auth.login_title')}</Title>
          <Text type="secondary">{t('auth.login_subtitle')}</Text>
        </div>

        <Form name="login_form" layout="vertical" onFinish={onFinish} size="large">
          <Form.Item
            name="email"
            rules={[
              { required: true, message: t('auth.email_required') },
              { type: 'email', message: t('auth.email_invalid') }
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder={t('auth.email_label')} />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: t('auth.password_required') }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder={t('auth.password_label')} />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block style={{ height: 45 }}>
              {t('auth.sign_in_button')}
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;