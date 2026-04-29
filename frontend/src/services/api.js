import axios from 'axios';
import { authService } from './authService';

const API_URL = '/api';
const isAuthEndpoint = (url = '') => url.includes('/auth/login') || url.includes('/auth/register');

const api = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = authService.getToken();
    if (token && !isAuthEndpoint(config.url)) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = error.config?.url || '';
    const shouldHandleAsSessionExpiry = error.response?.status === 401 && !isAuthEndpoint(requestUrl);

    if (shouldHandleAsSessionExpiry && window.location.pathname !== '/login') {
      authService.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
