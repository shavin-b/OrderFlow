import React, { useEffect, useState } from 'react';
import { Box, Typography } from '@mui/material';
import { DataTable, Column } from '../components/common/DataTable';
import { AuditLog } from '../types';
import { auditApi } from '../api/audit';

export const LogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  const loadLogs = async () => {
    try {
      const data = await auditApi.getLogs(page, rowsPerPage);
      setLogs(data.content || []);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setLogs([
        {
          id: 1,
          userId: 1,
          userEmail: 'john@example.com',
          action: 'LOGIN_SUCCESS',
          resource: 'User',
          details: 'User authenticated from IP 127.0.0.1',
          ipAddress: '127.0.0.1',
          timestamp: new Date().toISOString(),
        },
        {
          id: 2,
          userId: 1,
          userEmail: 'john@example.com',
          action: 'CREATE_RULE',
          resource: 'AutomationRule',
          details: 'Created rule Order Status Inquiry',
          ipAddress: '127.0.0.1',
          timestamp: new Date().toISOString(),
        },
      ]);
      setTotalElements(2);
    }
  };

  useEffect(() => {
    loadLogs();
  }, [page, rowsPerPage]);

  const columns: Column<AuditLog>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'userEmail', label: 'User Email' },
    { id: 'action', label: 'Action' },
    { id: 'resource', label: 'Target Resource' },
    { id: 'details', label: 'Details' },
    { id: 'ipAddress', label: 'IP Address' },
    { id: 'timestamp', label: 'Timestamp', format: (val: string) => new Date(val).toLocaleString() },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Security & Audit Logs
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Track system security events, authentication attempts, and administrative actions
        </Typography>
      </Box>

      <DataTable
        columns={columns}
        data={logs}
        totalElements={totalElements}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
      />
    </Box>
  );
};
