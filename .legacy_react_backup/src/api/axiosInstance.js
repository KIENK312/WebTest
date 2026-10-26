import axios from 'axios';

// Base URL: Dùng relative path để Vite dev server proxy tự động chuyển tiếp tới http://localhost:8080
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// 1. Request Interceptor: Tự động gắn Token Bearer vào Header
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Event emitter helper để bắn thông báo Toast từ Axios Interceptor ra UI
export const dispatchApiToast = (type, message, title) => {
  window.dispatchEvent(
    new CustomEvent('app:toast', {
      detail: { type, message, title },
    })
  );
};

// 2. Response Interceptor: Bắt lỗi 401 (chuyển hướng), 400 và 409 (Toast cảnh báo)
axiosInstance.interceptors.response.use(
  (response) => {
    // Trả về trực tiếp phần data bên trong của ApiResponse<T>
    return response.data;
  },
  (error) => {
    const { response } = error;

    if (!response) {
      const offlineMsg = 'Không thể kết nối đến máy chủ Backend (http://localhost:8080). Vui lòng kiểm tra lại dịch vụ!';
      dispatchApiToast('error', offlineMsg, 'Lỗi Kết Nối');
      return Promise.reject(new Error(offlineMsg));
    }

    const status = response.status;
    const errorData = response.data;
    const errorMessage =
      errorData?.message ||
      errorData?.error ||
      errorData?.data ||
      'Đã xảy ra lỗi không xác định từ hệ thống!';

    // Xử lý 401 Unauthorized: Hết hạn phiên hoặc chưa đăng nhập
    if (status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('user');

      dispatchApiToast('warning', 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!', 'Chưa Xác Thực (401)');

      // Chuyển hướng về login nếu không phải đang ở trang login/register
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        setTimeout(() => {
          window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`;
        }, 1200);
      }
    } 
    // Xử lý 409 Conflict (Ghế bị trùng, Xung đột bảo trì 48h)
    else if (status === 409) {
      dispatchApiToast('error', errorMessage, 'Xung Đột Dữ Liệu (409)');
    } 
    // Xử lý 400 Bad Request (Tham số không hợp lệ, dữ liệu lỗi)
    else if (status === 400) {
      dispatchApiToast('error', errorMessage, 'Yêu Cầu Không Hợp Lệ (400)');
    }
    // Xử lý 403 Forbidden (Không đủ quyền)
    else if (status === 403) {
      dispatchApiToast('error', 'Bạn không có quyền truy cập vào chức năng này!', 'Bị Từ Chối (403)');
    }
    // Xử lý 500 Internal Server Error
    else if (status >= 500) {
      dispatchApiToast('error', 'Hệ thống máy chủ gặp sự cố nội bộ. Vui lòng thử lại sau!', 'Lỗi Máy Chủ (500)');
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
