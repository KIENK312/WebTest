import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';

export const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();
  const toast = useToast();

  if (loading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    toast.warning('Vui lòng đăng nhập để truy cập trang này!', 'Yêu Cầu Xác Thực');
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />;
  }

  if (requiredRole) {
    const userRole = user?.role;
    let hasPermission = false;

    if (requiredRole === 'ROLE_ADMIN') {
      hasPermission = userRole === 'ROLE_ADMIN';
    } else if (requiredRole === 'ROLE_OPERATOR') {
      hasPermission = userRole === 'ROLE_OPERATOR' || userRole === 'ROLE_ADMIN';
    } else if (requiredRole === 'ROLE_CUSTOMER') {
      hasPermission = true; // All authenticated users can access customer features
    }

    if (!hasPermission) {
      toast.error('Bạn không có quyền truy cập vào phân hệ này!', 'Bị Từ Chối (403)');
      return <Navigate to="/" replace />;
    }
  }

  return children;
};
