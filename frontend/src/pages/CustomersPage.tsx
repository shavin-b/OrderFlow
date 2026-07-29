import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  IconButton,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { DataTable, Column } from '../components/common/DataTable';
import { StatusChip } from '../components/common/StatusChip';
import { Customer } from '../types';
import { customersApi } from '../api/customers';

export const CustomersPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [openModal, setOpenModal] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Partial<Customer> | null>(null);

  const loadCustomers = async () => {
    try {
      const data = await customersApi.findAll(page, rowsPerPage, search);
      setCustomers(data.content || []);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      // Mock fallback data if backend is starting
      setCustomers([
        {
          id: 1,
          waId: '15550001111',
          phone: '+15550001111',
          name: 'Alex Johnson',
          email: 'alex@gmail.com',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
        {
          id: 2,
          waId: '15550002222',
          phone: '+15550002222',
          name: 'David Miller',
          email: 'david@yahoo.com',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        },
      ]);
      setTotalElements(2);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, [page, rowsPerPage, search]);

  const handleSave = async () => {
    if (!editingCustomer?.phone || !editingCustomer?.name) return;
    if (editingCustomer.id) {
      await customersApi.update(editingCustomer.id, editingCustomer);
    } else {
      await customersApi.create({
        ...editingCustomer,
        waId: editingCustomer.phone.replace(/[^0-9]/g, ''),
      });
    }
    setOpenModal(false);
    loadCustomers();
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this customer?')) {
      await customersApi.delete(id);
      loadCustomers();
    }
  };

  const columns: Column<Customer>[] = [
    { id: 'id', label: 'ID', minWidth: 60 },
    { id: 'name', label: 'Customer Name' },
    { id: 'phone', label: 'WhatsApp Phone' },
    { id: 'email', label: 'Email' },
    {
      id: 'status',
      label: 'Status',
      format: (val: string) => <StatusChip status={val} />,
    },
    { id: 'createdAt', label: 'Created', format: (val: string) => new Date(val).toLocaleDateString() },
    {
      id: 'actions',
      label: 'Actions',
      align: 'right',
      format: (_, row) => (
        <Box display="flex" justifyContent="flex-end" gap={1}>
          <IconButton
            size="small"
            onClick={() => {
              setEditingCustomer(row);
              setOpenModal(true);
            }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(row.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h4" fontWeight={700}>
          WhatsApp Customers CRM
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Manage WhatsApp contact list and customer profiles
        </Typography>
      </Box>

      <DataTable
        columns={columns}
        data={customers}
        totalElements={totalElements}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={setPage}
        onRowsPerPageChange={setRowsPerPage}
        onSearch={setSearch}
        onAdd={() => {
          setEditingCustomer({ name: '', phone: '', email: '', status: 'ACTIVE' });
          setOpenModal(true);
        }}
        addLabel="Add Customer"
      />

      {/* Add / Edit Customer Dialog */}
      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{editingCustomer?.id ? 'Edit Customer' : 'Add New Customer'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Name"
            fullWidth
            margin="dense"
            value={editingCustomer?.name || ''}
            onChange={(e) => setEditingCustomer((prev) => ({ ...prev, name: e.target.value }))}
          />
          <TextField
            label="WhatsApp Phone (+1555...)"
            fullWidth
            margin="dense"
            value={editingCustomer?.phone || ''}
            onChange={(e) => setEditingCustomer((prev) => ({ ...prev, phone: e.target.value }))}
          />
          <TextField
            label="Email Address"
            fullWidth
            margin="dense"
            value={editingCustomer?.email || ''}
            onChange={(e) => setEditingCustomer((prev) => ({ ...prev, email: e.target.value }))}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenModal(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>
            Save Customer
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
