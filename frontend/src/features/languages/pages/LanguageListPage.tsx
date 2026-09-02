import React, { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Switch, Space, Typography, Card, App as AntdApp } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';
import { CheckCircleOutlined, InfoCircleOutlined } from '@ant-design/icons';
import languageService, { LanguageResponseDTO } from '../services/languageService';

const { Title, Text } = Typography;

/**
 * Enhanced Language Management Page.
 * Synchronizes activation status and ensures business rules for default system language.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
const LanguageListPage: React.FC = () => {
  const { t } = useTranslation();
  const { message } = AntdApp.useApp();
  const [languages, setLanguages] = useState<LanguageResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);

  /**
   * Refreshes the language list from the server.
   */
  const loadLanguages = useCallback(async () => {
    setLoading(true);
    try {
      const data = await languageService.getAllLanguages();
      setLanguages(data);
    } catch (error) {
      message.error(t('languages.load_error') || 'Failed to load languages.');
    } finally {
      setLoading(false);
    }
  }, [t, message]);

  useEffect(() => {
    loadLanguages();
  }, [loadLanguages]);

  /**
   * Handles individual field updates with integrated business rules.
   */
  const handleUpdate = async (record: LanguageResponseDTO, field: 'active' | 'isDefault', newValue: boolean) => {
    setLoading(true);
    try {
      const updateData = {
        name: record.name,
        code: record.code,
        active: field === 'active' ? newValue : record.active,
        isDefault: field === 'isDefault' ? newValue : record.isDefault,
      };

      /* Business Rule: A default language MUST be active */
      if (field === 'isDefault' && newValue === true) {
        updateData.active = true;
      }

      await languageService.updateLanguage(record.id, updateData);
      message.success(t('languages.update_success'));
      await loadLanguages(); // Forced refresh to sync with backend rotation logic
    } catch (error: any) {
      message.error(error.response?.data?.message || t('languages.update_error'));
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<LanguageResponseDTO> = [
    {
      title: t('languages.name') || 'Language',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          {text}
          {record.isDefault && <Tag color="gold" icon={<CheckCircleOutlined />}>{t('languages.default_label') || 'Default'}</Tag>}
        </Space>
      ),
    },
    {
      title: t('languages.code') || 'Code',
      dataIndex: 'code',
      key: 'code',
      render: (code) => <Tag>{code}</Tag>,
    },
    {
      title: t('languages.active_status') || 'Available',
      dataIndex: 'active',
      key: 'active',
      render: (active, record) => (
        <Switch 
          checked={active} 
          disabled={record.isDefault} // Cannot disable the default language
          onChange={(val) => handleUpdate(record, 'active', val)} 
          loading={loading}
        />
      ),
    },
    {
      title: t('languages.set_default') || 'System Default',
      dataIndex: 'isDefault',
      key: 'isDefault',
      render: (isDefault, record) => (
        <Switch 
          checked={isDefault} 
          disabled={isDefault || !record.active} // Must be active to become default
          onChange={(val) => handleUpdate(record, 'isDefault', val)}
          checkedChildren={<CheckCircleOutlined />}
          loading={loading}
        />
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ display: 'flex' }}>
      <Card bordered={false}>
        <Title level={4}>{t('menu.languages')}</Title>
        <Text type="secondary">
          <InfoCircleOutlined /> {t('languages.management_hint') || 'Configure system languages. Default language is forced to be active.'}
        </Text>
      </Card>

      <Table
        columns={columns}
        dataSource={languages}
        rowKey="id"
        loading={loading}
        pagination={false}
      />
    </Space>
  );
};

export default LanguageListPage;