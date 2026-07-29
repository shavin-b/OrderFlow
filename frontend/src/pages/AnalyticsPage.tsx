import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  TextField,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  CircularProgress,
  Stack,
  Tooltip,
} from '@mui/material';
import MessageIcon from '@mui/icons-material/Message';
import ReplyAllIcon from '@mui/icons-material/ReplyAll';
import ErrorIcon from '@mui/icons-material/Error';
import TimerIcon from '@mui/icons-material/Timer';
import PeopleIcon from '@mui/icons-material/People';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import TableChartIcon from '@mui/icons-material/TableChart';
import { StatCard } from '../components/common/StatCard';
import { MessagesChart } from '../components/charts/MessagesChart';
import { KeywordsChart } from '../components/charts/KeywordsChart';
import { SuccessRateChart } from '../components/charts/SuccessRateChart';
import { DataTable, Column } from '../components/common/DataTable';
import { analyticsApi } from '../api/analytics';
import { AnalyticsSummary, DailyStat } from '../types';

export const AnalyticsPage: React.FC = () => {
  const [rangePreset, setRangePreset] = useState<'7days' | '30days' | 'custom'>('30days');
  const [startDate, setStartDate] = useState(
    new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0]
  );
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);

  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState<string | null>(null);

  const loadAnalytics = async () => {
    setLoading(true);
    try {
      const data = await analyticsApi.getSummary(startDate, endDate);
      setSummary(data);
    } catch (err) {
      // Mock data fallback for standalone frontend preview
      setSummary({
        totalIncomingMessages: 14200,
        totalOutgoingReplies: 13850,
        totalFailedReplies: 350,
        avgResponseTimeMs: 142,
        activeCustomersCount: 8540,
        monthlyRevenue: 12450.0,
        successRatePercentage: 97.5,
        topKeywordPattern: 'order status',
        dailyBreakdown: Array.from({ length: 14 }).map((_, i) => ({
          statDate: new Date(Date.now() - (13 - i) * 86400000).toISOString().split('T')[0],
          incomingMessages: 900 + i * 40,
          outgoingReplies: 870 + i * 38,
          failedReplies: 30 - (i % 5),
          avgResponseTimeMs: 135 + (i % 10),
          activeCustomers: 7000 + i * 110,
          topKeyword: i % 2 === 0 ? 'order status' : 'tracking',
        })),
        topKeywords: [
          { pattern: 'order status', triggerCount: 450 },
          { pattern: 'tracking', triggerCount: 320 },
          { pattern: 'pricing', triggerCount: 210 },
          { pattern: 'support', triggerCount: 180 },
          { pattern: 'help', triggerCount: 140 },
        ],
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, [startDate, endDate]);

  const handleRangePresetChange = (preset: '7days' | '30days' | 'custom') => {
    setRangePreset(preset);
    const end = new Date().toISOString().split('T')[0];
    setEndDate(end);
    if (preset === '7days') {
      setStartDate(new Date(Date.now() - 7 * 86400000).toISOString().split('T')[0]);
    } else if (preset === '30days') {
      setStartDate(new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0]);
    }
  };

  const handleExport = async (type: 'csv' | 'excel' | 'pdf') => {
    setDownloading(type);
    try {
      await analyticsApi.downloadFile(type, startDate, endDate);
    } catch (err) {
      alert(`Export to ${type.toUpperCase()} failed.`);
    } finally {
      setDownloading(null);
    }
  };

  const columns: Column<DailyStat>[] = [
    { id: 'statDate', label: 'Date', format: (val) => new Date(val).toLocaleDateString() },
    { id: 'incomingMessages', label: 'Incoming Messages', align: 'center' },
    { id: 'outgoingReplies', label: 'Outgoing Replies', align: 'center' },
    { id: 'failedReplies', label: 'Failed Replies', align: 'center' },
    { id: 'avgResponseTimeMs', label: 'Avg Latency (ms)', align: 'center', format: (val) => `${val} ms` },
    { id: 'topKeyword', label: 'Top Keyword' },
  ];

  return (
    <Box>
      {/* Header & Date Range Controls */}
      <Box
        display="flex"
        flexDirection={{ xs: 'column', md: 'row' }}
        justifyContent="space-between"
        alignItems={{ md: 'center' }}
        gap={2}
        mb={3}
      >
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Enterprise Analytics & Reports
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Comprehensive metric tracking, keyword conversions, and exportable reports
          </Typography>
        </Box>

        {/* Date Filter & Export Toolbar */}
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems="center">
          <FormControl size="small" sx={{ minWidth: 140 }}>
            <InputLabel>Date Range</InputLabel>
            <Select
              value={rangePreset}
              label="Date Range"
              onChange={(e) => handleRangePresetChange(e.target.value as any)}
            >
              <MenuItem value="7days">Last 7 Days</MenuItem>
              <MenuItem value="30days">Last 30 Days</MenuItem>
              <MenuItem value="custom">Custom Range</MenuItem>
            </Select>
          </FormControl>

          {rangePreset === 'custom' && (
            <>
              <TextField
                label="Start Date"
                type="date"
                size="small"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="End Date"
                type="date"
                size="small"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </>
          )}

          {/* Export Buttons */}
          <Tooltip title="Export CSV Report">
            <Button
              variant="outlined"
              color="primary"
              size="small"
              startIcon={<FileDownloadIcon />}
              disabled={downloading === 'csv'}
              onClick={() => handleExport('csv')}
            >
              {downloading === 'csv' ? <CircularProgress size={16} /> : 'CSV'}
            </Button>
          </Tooltip>

          <Tooltip title="Export Excel Spreadsheet">
            <Button
              variant="outlined"
              color="success"
              size="small"
              startIcon={<TableChartIcon />}
              disabled={downloading === 'excel'}
              onClick={() => handleExport('excel')}
            >
              {downloading === 'excel' ? <CircularProgress size={16} /> : 'Excel'}
            </Button>
          </Tooltip>

          <Tooltip title="Export Executive PDF Report">
            <Button
              variant="contained"
              color="error"
              size="small"
              startIcon={<PictureAsPdfIcon />}
              disabled={downloading === 'pdf'}
              onClick={() => handleExport('pdf')}
            >
              {downloading === 'pdf' ? <CircularProgress size={16} color="inherit" /> : 'PDF Report'}
            </Button>
          </Tooltip>
        </Stack>
      </Box>

      {/* KPI Cards Grid */}
      <Grid container spacing={2.5} mb={4}>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard
            title="Incoming Messages"
            value={summary?.totalIncomingMessages.toLocaleString() || 0}
            trend="+12.4%"
            icon={<MessageIcon />}
            color="#6366f1"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard
            title="Outgoing Replies"
            value={summary?.totalOutgoingReplies.toLocaleString() || 0}
            trend="+14.1%"
            icon={<ReplyAllIcon />}
            color="#10b981"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard
            title="Failed Replies"
            value={summary?.totalFailedReplies.toLocaleString() || 0}
            trend="-5.2%"
            isPositive={false}
            icon={<ErrorIcon />}
            color="#ef4444"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard
            title="Avg Latency"
            value={`${summary?.avgResponseTimeMs || 0} ms`}
            trend="-8 ms"
            icon={<TimerIcon />}
            color="#f59e0b"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard
            title="Delivery Success"
            value={`${summary?.successRatePercentage || 0}%`}
            trend="+1.2%"
            icon={<CheckCircleIcon />}
            color="#3b82f6"
          />
        </Grid>
      </Grid>

      {/* Interactive Charts Section */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} lg={8}>
          <MessagesChart />
        </Grid>
        <Grid item xs={12} lg={4}>
          <KeywordsChart />
        </Grid>
        <Grid item xs={12}>
          <SuccessRateChart />
        </Grid>
      </Grid>

      {/* Detailed Daily Breakdown Data Table */}
      <Typography variant="h6" fontWeight={700} mb={2}>
        Daily Performance Breakdown
      </Typography>
      <DataTable
        columns={columns}
        data={summary?.dailyBreakdown || []}
        rowsPerPage={10}
      />
    </Box>
  );
};
