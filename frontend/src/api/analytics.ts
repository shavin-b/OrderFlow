import { apiClient } from './client';
import { AnalyticsSummary, MonthlyStat, Report } from '../types';

export const analyticsApi = {
  getSummary: async (startDate?: string, endDate?: string): Promise<AnalyticsSummary> => {
    let url = '/analytics/summary';
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (params.toString()) url += `?${params.toString()}`;

    const res = await apiClient.get(url);
    return res.data.data;
  },

  getMonthlyStats: async (): Promise<MonthlyStat[]> => {
    const res = await apiClient.get('/analytics/monthly');
    return res.data.data;
  },

  getReports: async (): Promise<Report[]> => {
    const res = await apiClient.get('/analytics/reports');
    return res.data.data;
  },

  generateReport: async (reportType: 'CSV' | 'EXCEL' | 'PDF', startDate: string, endDate: string) => {
    const res = await apiClient.post('/analytics/reports/generate', {
      reportType,
      startDate,
      endDate,
      reportTitle: `Enterprise ${reportType} Report`,
    });
    return res.data.data;
  },

  downloadFile: async (type: 'csv' | 'excel' | 'pdf', startDate?: string, endDate?: string) => {
    let url = `/analytics/reports/export/${type}`;
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (params.toString()) url += `?${params.toString()}`;

    const response = await apiClient.get(url, { responseType: 'blob' });
    const mimeTypes: Record<string, string> = {
      csv: 'text/csv',
      excel: 'application/vnd.ms-excel',
      pdf: 'application/pdf',
    };

    const blob = new Blob([response.data], { type: mimeTypes[type] });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', `orderflow-analytics.${type === 'excel' ? 'xml' : type}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },
};
