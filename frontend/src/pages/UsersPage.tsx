import React, { useState } from 'react';
import { Box, Typography } from '@mui/material';
import { DataTable, Column } from '../components/common/DataTable';
import { StatusChip } from '../components/common/StatusChip';
import { User } from '../types';

export const UsersPage: React.FC = () => {
  const [users] = useState<User[]>([
    {
      id: 1,
      email: 'john@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phone: '+15550001111',
      emailVerified: true,
      status: 'ACTIVE',
      roles: ['ROLE_ADMIN'],
      createdAt: '2026-07-20T10:00:00',
    },
    {
      id: 2,
      email: 'manager@example.com',
      firstName: 'Sarah',
      lastName: 'Smith',
      phone: '+15550002222',
      emailVerified: true,
      status: 'ACTIVE',
      roles: ['ROLE_MANAGER'],
      createdAt: '2026-07-22T14:30:00',
    },
    {
      id: 3,
      email: 'support@example.com',
      firstName: 'Michael',
      lastName: 'Brown',
      phone: '+15550003333',
      emailVerified: true,
      status: 'ACTIVE',
      roles: ['ROLE_SUPPORT'],
      createdAt: '2026-07-25T09:15:00',
    },
  ]);

  const columns: Column<User>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'firstName', label: 'Name', format: (_, row) => `${row.firstName} ${row.lastName || ''}` },
    { id: 'email', label: 'Email' },
    { id: 'phone', label: 'Phone' },
    {
      id: 'roles',
      label: 'Role',
      format: (val: string[]) => <StatusChip status={val?.[0] || 'ROLE_USER'} />,
    },
    {
      id: 'status',
      label: 'Status',
      format: (val: string) => <StatusChip status={val} />,
    },
    { id: 'createdAt', label: 'Registered', format: (val: string) => new Date(val).toLocaleDateString() },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          SaaS Users Management
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Manage system users and role-based permissions
        </Typography>
      </Box>

      <DataTable columns={columns} data={users} />
    </Box>
  );
};
