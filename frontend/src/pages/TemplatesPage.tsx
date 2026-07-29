import React, { useState } from 'react';
import { Box, Typography } from '@mui/material';
import { DataTable, Column } from '../components/common/DataTable';
import { StatusChip } from '../components/common/StatusChip';

interface Template {
  id: number;
  name: string;
  category: 'MARKETING' | 'UTILITY' | 'AUTHENTICATION';
  language: string;
  status: 'APPROVED' | 'PENDING' | 'REJECTED';
  body: string;
}

export const TemplatesPage: React.FC = () => {
  const [templates] = useState<Template[]>([
    {
      id: 1,
      name: 'order_shipped_notification',
      category: 'UTILITY',
      language: 'en_US',
      status: 'APPROVED',
      body: 'Hi {{1}}, your Order #{{2}} has been shipped via {{3}}. Tracking link: {{4}}',
    },
    {
      id: 2,
      name: 'welcome_discount_offer',
      category: 'MARKETING',
      language: 'en_US',
      status: 'APPROVED',
      body: 'Welcome to OrderFlow {{1}}! Use code SAVE20 to get 20% off your next purchase.',
    },
  ]);

  const columns: Column<Template>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'name', label: 'Template Name' },
    { id: 'category', label: 'Category' },
    { id: 'language', label: 'Language' },
    {
      id: 'status',
      label: 'Meta Status',
      format: (val: string) => <StatusChip status={val} />,
    },
    { id: 'body', label: 'Template Body' },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          WhatsApp Message Templates
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Manage Meta-approved HSM message templates for outbound notifications
        </Typography>
      </Box>

      <DataTable columns={columns} data={templates} />
    </Box>
  );
};
