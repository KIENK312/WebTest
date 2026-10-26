import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { useToast } from './ToastContext';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const toast = useToast();

  useEffect(() => {
    // Khởi tạo từ LocalStorage khi trang tải lại
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        console.error('Lỗi phân tích thông tin user từ localStorage:', e);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (usernameOrEmail, password) => {
    try {
      const res = await authApi.login({ usernameOrEmail, password });
      const authData = res.data;

      const userObj = {
        userId: authData.userId,
        username: authData.username,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        operatorId: authData.operatorId,
      };

      setToken(authData.accessToken);
      setUser(userObj);

      localStorage.setItem('token', authData.accessToken);
      localStorage.setItem('user', JSON.stringify(userObj));

      toast.success(
        `Chào mừng trở lại, ${authData.fullName}!`,
        'Đăng Nhập Thành Công'
      );

      return userObj;
    } catch (err) {
      console.error('Đăng nhập thất bại:', err);
      throw err;
    }
  };

  const register = async (userData) => {
    try {
      const res = await authApi.register(userData);
      const authData = res.data;

      const userObj = {
        userId: authData.userId,
        username: authData.username,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        operatorId: authData.operatorId,
      };

      setToken(authData.accessToken);
      setUser(userObj);

      localStorage.setItem('token', authData.accessToken);
      localStorage.setItem('user', JSON.stringify(userObj));

      toast.success(
        `Tài khoản ${authData.username} đã được tạo thành công!`,
        'Đăng Ký Thành Công'
      );

      return userObj;
    } catch (err) {
      console.error('Đăng ký thất bại:', err);
      throw err;
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');

    toast.info('Bạn đã đăng xuất khỏi hệ thống an toàn.', 'Đã Đăng Xuất');
  };

  const isAuthenticated = !!token;
  const isAdmin = user?.role === 'ROLE_ADMIN';
  const isOperator = user?.role === 'ROLE_OPERATOR' || isAdmin;
  const isCustomer = user?.role === 'ROLE_CUSTOMER';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated,
        isAdmin,
        isOperator,
        isCustomer,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
