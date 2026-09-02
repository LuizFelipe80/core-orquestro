import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, App as AntdApp } from 'antd';
import { MailOutlined, LockOutlined } from '@ant-design/icons';
import { useAuth } from '../../../context/AuthContext';

const { Title, Text } = Typography;

/**
 * Login Page Component.
 * Provides a specialized interface for user authentication.
 * Integrates with AuthContext to handle the sign-in process and manages local loading states.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const { signIn } = useAuth();
  const { message } = AntdApp.useApp();

  /**
   * Handles the form submission.
   * On success, the user is redirected by the AuthProvider logic or subsequent route guards.
   * 
   * @param values The form values containing email and password.
   */
  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await signIn(values);
      message.success('Welcome to Orquestro!');
    } catch (error: any) {
      // Extracts the error message from our backend's ErrorResponseDTO
      const errorMessage = error.response?.data?.message || 'Failed to authenticate. Please check your credentials.';
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
      <Card 
        style={{ width: '100%', maxWidth: 400, boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
        bordered={false}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={2} style={{ margin: 0, color: '#102a43' }}>Orquestro</Title>
          <Text type="secondary">Management Platform Foundation</Text>
        </div>

        <Form
          name="login_form"
          layout="vertical"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Please enter your email' },
              { type: 'email', message: 'Please enter a valid email address' }
            ]}
          >
            <Input 
              prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} 
              placeholder="Email" 
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please enter your password' }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
              placeholder="Password"
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading} 
              block
              style={{ height: 45, fontSize: 16 }}
            >
              Sign In
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;