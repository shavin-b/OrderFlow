import { apiClient } from './client';
import { AuditLog, PagedResponse } from '../types';

export const auditApi = {
  getLogs: async (page = 0, size = 20): Promise<PagedResponse<AuditLog>> => {
    const res = await apiClient.get(`/audit-logs?page=${page}&size=${size}`);
    return res.data.data;
  },
};
