import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

export const SuccessRateChart: React.FC = () => {
  const data = {
    labels: ['Delivered & Read', 'Sent Pending', 'Failed / Retried'],
    datasets: [
      {
        label: 'Message Delivery Rate',
        data: [96.4, 2.8, 0.8],
        backgroundColor: ['#10b981', '#3b82f6', '#ef4444'],
        borderWidth: 0,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
      },
    },
  };

  return (
    <Card sx={{ height: 380 }}>
      <CardContent>
        <Typography variant="h6" mb={2}>
          Automation Delivery Success Rate (%)
        </Typography>
        <Box height={280}>
          <Doughnut data={data} options={options} />
        </Box>
      </CardContent>
    </Card>
  );
};
