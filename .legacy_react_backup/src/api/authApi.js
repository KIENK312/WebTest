import axiosInstance from './axiosInstance';

export const authApi = {
  login: (credentials) => {
    return axiosInstance.post('/auth/login', credentials);
  },

  register: (userData) => {
    return axiosInstance.post('/auth/register', userData);
  },
};
