import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

export const KeywordsChart: React.FC = () => {
  const data = {
    labels: ['order status', 'tracking', 'pricing', 'support', 'help'],
    datasets: [
      {
        label: 'Triggers',
        data: [450, 320, 210, 180, 140],
        backgroundColor: [
          '#6366f1',
          '#3b82f6',
          '#10b981',
          '#f59e0b',
          '#ec4899',
        ],
        borderWidth: 0,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right' as const,
      },
    },
  };

  return (
    <Card sx={{ height: 380 }}>
      <CardContent>
        <Typography variant="h6" mb={2}>
          Top Keyword Triggers
        </Typography>
        <Box height={280}>
          <Doughnut data={data} options={options} />
        </Box>
      </CardContent>
    </Card>
  );
};
