import React from 'react';
import { Grid, Box, Typography, Card, CardContent } from '@mui/material';
import MessageIcon from '@mui/icons-material/Message';
import ReplyAllIcon from '@mui/icons-material/ReplyAll';
import PeopleIcon from '@mui/icons-material/People';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import { StatCard } from '../components/common/StatCard';
import { MessagesChart } from '../components/charts/MessagesChart';
import { KeywordsChart } from '../components/charts/KeywordsChart';
import { SuccessRateChart } from '../components/charts/SuccessRateChart';

export const DashboardPage: React.FC = () => {
  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Dashboard Overview
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Real-time metrics and WhatsApp automation performance
        </Typography>
      </Box>

      {/* KPI Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Messages Today"
            value="1,420"
            trend="+12.5%"
            icon={<MessageIcon />}
            color="#6366f1"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Replies Today"
            value="1,385"
            trend="+14.2%"
            icon={<ReplyAllIcon />}
            color="#10b981"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Total Customers"
            value="8,540"
            trend="+8.1%"
            icon={<PeopleIcon />}
            color="#3b82f6"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <StatCard
            title="Monthly Revenue"
            value="$12,450"
            trend="+18.4%"
            icon={<AttachMoneyIcon />}
            color="#f59e0b"
          />
        </Grid>
      </Grid>

      {/* Analytics Charts */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, lg: 8 }}>
          <MessagesChart />
        </Grid>
        <Grid size={{ xs: 12, lg: 4 }}>
          <KeywordsChart />
        </Grid>
        <Grid size={{ xs: 12, lg: 6 }}>
          <SuccessRateChart />
        </Grid>

        {/* Live Engine Status */}
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card sx={{ height: 380 }}>
            <CardContent>
              <Typography variant="h6" mb={2}>
                System Engine Status & Throughput
              </Typography>

              <Box display="flex" flexDirection="column" gap={2} mt={2}>
                <Box display="flex" justifyContent="space-between" p={2} bgcolor="action.hover" borderRadius={2}>
                  <Typography variant="body2" fontWeight={600}>WhatsApp Cloud API Status</Typography>
                  <Typography variant="body2" color="success.main" fontWeight={700}>OPERATIONAL (200 OK)</Typography>
                </Box>
                <Box display="flex" justifyContent="space-between" p={2} bgcolor="action.hover" borderRadius={2}>
                  <Typography variant="body2" fontWeight={600}>Message Queue Processing</Typography>
                  <Typography variant="body2" color="success.main" fontWeight={700}>ACTIVE (0 Pending)</Typography>
                </Box>
                <Box display="flex" justifyContent="space-between" p={2} bgcolor="action.hover" borderRadius={2}>
                  <Typography variant="body2" fontWeight={600}>Non-Blocking Scheduler</Typography>
                  <Typography variant="body2" color="success.main" fontWeight={700}>RUNNING (ThreadPoolTaskScheduler)</Typography>
                </Box>
                <Box display="flex" justifyContent="space-between" p={2} bgcolor="action.hover" borderRadius={2}>
                  <Typography variant="body2" fontWeight={600}>Average Response Time</Typography>
                  <Typography variant="body2" color="primary.main" fontWeight={700}>142 ms</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
