import { apiClient } from './client';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../types';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const res = await apiClient.post('/auth/login', credentials);
    return res.data.data;
  },

  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const res = await apiClient.post('/auth/register', userData);
    return res.data.data;
  },

  getProfile: async (): Promise<User> => {
    const res = await apiClient.get('/profile');
    return res.data.data;
  },

  updateProfile: async (data: Partial<User>): Promise<User> => {
    const res = await apiClient.put('/profile', data);
    return res.data.data;
  },
};
