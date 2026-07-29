import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': 'dev-api-key-change-in-production',
  },
});

// Request Interceptor to inject JWT access token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('orderflow_access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor to handle expired tokens and subscription locks
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('orderflow_refresh_token');
      if (refreshToken) {
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });
          const { accessToken, refreshToken: newRefreshToken } = res.data.data;
          localStorage.setItem('orderflow_access_token', accessToken);
          localStorage.setItem('orderflow_refresh_token', newRefreshToken);
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return apiClient(originalRequest);
        } catch (refreshErr) {
          localStorage.removeItem('orderflow_access_token');
          localStorage.removeItem('orderflow_refresh_token');
          window.location.href = '/login?expired=true';
        }
      }
    } else if (error.response?.status === 402) {
      // Payment Required - Subscription Expired
      window.location.href = '/subscriptions?upgradeRequired=true';
    }
    return Promise.reject(error);
  }
);
