import React, { useState } from 'react';
import { Box, Typography, Button } from '@mui/material';
import { DataTable, Column } from '../components/common/DataTable';

interface ReplySequence {
  id: number;
  ruleName: string;
  sequenceStep: number;
  messageBody: string;
  delaySeconds: number;
  mediaType?: string;
}

export const RepliesPage: React.FC = () => {
  const [replies] = useState<ReplySequence[]>([
    {
      id: 1,
      ruleName: 'Order Status Inquiry',
      sequenceStep: 1,
      messageBody: 'Hello! Checking your order details now...',
      delaySeconds: 0,
    },
    {
      id: 2,
      ruleName: 'Order Status Inquiry',
      sequenceStep: 2,
      messageBody: 'Your order #ORD-8821 is currently OUT FOR DELIVERY via FedEx.',
      delaySeconds: 3,
    },
    {
      id: 3,
      ruleName: 'Pricing Information',
      sequenceStep: 1,
      messageBody: 'Our SaaS plans start at $29/mo with unlimited WhatsApp automation.',
      delaySeconds: 0,
    },
  ]);

  const columns: Column<ReplySequence>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'ruleName', label: 'Associated Rule' },
    { id: 'sequenceStep', label: 'Step #', align: 'center' },
    { id: 'messageBody', label: 'Reply Message Body' },
    { id: 'delaySeconds', label: 'Non-Blocking Delay (Sec)', align: 'center' },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          Automated Reply Sequences
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Multi-reply sequences executed non-blockingly with delayed response timing
        </Typography>
      </Box>

      <DataTable columns={columns} data={replies} />
    </Box>
  );
};
