import React from 'react';
import { Chip, ChipProps } from '@mui/material';

interface StatusChipProps {
  status: string;
  size?: 'small' | 'medium';
}

export const StatusChip: React.FC<StatusChipProps> = ({ status, size = 'small' }) => {
  let color: ChipProps['color'] = 'default';

  switch (status?.toUpperCase()) {
    case 'ACTIVE':
    case 'SENT':
    case 'DELIVERED':
    case 'READ':
    case 'ROLE_ADMIN':
    case 'TRIAL':
    case 'EXACT':
      color = 'success';
      break;
    case 'PENDING':
    case 'CONTAINS':
    case 'MONTHLY':
    case 'ROLE_MANAGER':
      color = 'info';
      break;
    case 'EXPIRED':
    case 'SUSPENDED':
    case 'FAILED':
    case 'BLOCKED':
      color = 'error';
      break;
    case 'STARTS_WITH':
    case 'ENDS_WITH':
    case 'REGEX':
    case 'YEARLY':
    case 'LIFETIME':
    case 'ROLE_SUPPORT':
      color = 'warning';
      break;
    default:
      color = 'default';
  }

  return (
    <Chip
      label={status}
      size={size}
      color={color}
      variant="outlined"
      sx={{ fontWeight: 600, borderRadius: 1.5 }}
    />
  );
};
