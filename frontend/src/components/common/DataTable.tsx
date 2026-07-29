import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  TextField,
  Box,
  Button,
  InputAdornment,
  IconButton,
  Tooltip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import AddIcon from '@mui/icons-material/Add';

export interface Column<T> {
  id: keyof T | 'actions';
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: any, row: T) => React.ReactNode;
}

interface DataTableProps<T> {
  title?: string;
  columns: Column<T>[];
  data: T[];
  totalElements?: number;
  page?: number;
  rowsPerPage?: number;
  onPageChange?: (newPage: number) => void;
  onRowsPerPageChange?: (newRowsPerPage: number) => void;
  onSearch?: (query: string) => void;
  onAdd?: () => void;
  addLabel?: string;
  onExportCsv?: () => void;
  onImportCsv?: (file: File) => void;
}

export function DataTable<T extends { id?: any }>({
  columns,
  data,
  totalElements = data.length,
  page = 0,
  rowsPerPage = 10,
  onPageChange,
  onRowsPerPageChange,
  onSearch,
  onAdd,
  addLabel = 'Add New',
  onExportCsv,
  onImportCsv,
}: DataTableProps<T>) {
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    if (onSearch) {
      onSearch(e.target.value);
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0] && onImportCsv) {
      onImportCsv(e.target.files[0]);
    }
  };

  const exportToCsv = () => {
    if (onExportCsv) {
      onExportCsv();
      return;
    }
    // Default CSV exporter
    if (!data.length) return;
    const keys = columns.filter((c) => c.id !== 'actions').map((c) => String(c.id));
    const header = columns.filter((c) => c.id !== 'actions').map((c) => c.label).join(',');
    const rows = data.map((row) =>
      keys.map((k) => `"${(row as any)[k] ?? ''}"`).join(',')
    );
    const csvContent = [header, ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', 'export_data.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Paper sx={{ width: '100%', overflow: 'hidden' }}>
      <Box
        p={2}
        display="flex"
        flexDirection={{ xs: 'column', sm: 'row' }}
        justifyContent="space-between"
        alignItems={{ sm: 'center' }}
        gap={2}
      >
        <TextField
          placeholder="Search..."
          size="small"
          value={searchQuery}
          onChange={handleSearchChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color="action" />
              </InputAdornment>
            ),
          }}
          sx={{ minWidth: 260 }}
        />

        <Box display="flex" gap={1} flexWrap="wrap">
          {onExportCsv !== undefined || data.length > 0 ? (
            <Tooltip title="Export to CSV">
              <Button
                variant="outlined"
                startIcon={<FileDownloadIcon />}
                onClick={exportToCsv}
                size="small"
              >
                Export CSV
              </Button>
            </Tooltip>
          ) : null}

          {onImportCsv && (
            <Tooltip title="Import from CSV">
              <Button
                variant="outlined"
                component="label"
                startIcon={<FileUploadIcon />}
                size="small"
              >
                Import CSV
                <input type="file" accept=".csv" hidden onChange={handleFileUpload} />
              </Button>
            </Tooltip>
          )}

          {onAdd && (
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={onAdd}
              size="small"
            >
              {addLabel}
            </Button>
          )}
        </Box>
      </Box>

      <TableContainer>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={String(column.id)}
                  align={column.align}
                  style={{ minWidth: column.minWidth, fontWeight: 700 }}
                >
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row, idx) => (
              <TableRow hover tabIndex={-1} key={row.id ?? idx}>
                {columns.map((column) => {
                  const value = (row as any)[column.id];
                  return (
                    <TableCell key={String(column.id)} align={column.align}>
                      {column.format ? column.format(value, row) : String(value ?? '')}
                    </TableCell>
                  );
                })}
              </TableRow>
            ))}
            {data.length === 0 && (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 4 }}>
                  No records found
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {onPageChange && (
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => onPageChange(newPage)}
          onRowsPerPageChange={(e) =>
            onRowsPerPageChange && onRowsPerPageChange(parseInt(e.target.value, 10))
          }
        />
      )}
    </Paper>
  );
}
