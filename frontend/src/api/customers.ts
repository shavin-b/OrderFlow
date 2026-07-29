import { apiClient } from './client';
import { Customer, PagedResponse } from '../types';

export const customersApi = {
  findAll: async (page = 0, size = 10, search = ''): Promise<PagedResponse<Customer>> => {
    const url = search
      ? `/customers/search?query=${encodeURIComponent(search)}&page=${page}&size=${size}`
      : `/customers?page=${page}&size=${size}`;
    const res = await apiClient.get(url);
    return res.data.data;
  },

  create: async (data: Partial<Customer>): Promise<Customer> => {
    const res = await apiClient.post('/customers', data);
    return res.data.data;
  },

  update: async (id: number, data: Partial<Customer>): Promise<Customer> => {
    const res = await apiClient.put(`/customers/${id}`, data);
    return res.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/customers/${id}`);
  },
};
