import axiosInstance from './axiosInstance';

export const operatorApi = {
  /**
   * Lấy danh sách đội xe
   */
  getBuses: (operatorId) => {
    return axiosInstance.get('/operator/buses', {
      params: operatorId ? { operatorId } : {},
    });
  },

  /**
   * Bật/tắt trạng thái bảo dưỡng của xe (ACTIVE / MAINTENANCE)
   */
  updateBusStatus: (busId, status) => {
    return axiosInstance.put(`/operator/buses/${busId}/status`, null, {
      params: { status },
    });
  },

  /**
   * Lấy danh sách tuyến đường
   */
  getRoutes: (operatorId) => {
    return axiosInstance.get('/operator/routes', {
      params: operatorId ? { operatorId } : {},
    });
  },

  /**
   * Lấy danh sách chuyến xe
   */
  getTrips: (operatorId) => {
    return axiosInstance.get('/operator/trips', {
      params: operatorId ? { operatorId } : {},
    });
  },

  /**
   * Tạo chuyến xe mới (kiểm tra xung đột xe bảo trì)
   */
  createTrip: (tripData) => {
    return axiosInstance.post('/operator/trips', tripData);
  },

  /**
   * Lên lịch bảo trì xe theo quy tắc an toàn đệm 48h
   */
  scheduleMaintenance: (data) => {
    return axiosInstance.post('/operator/maintenance', data);
  },

  /**
   * Xem lịch sử bảo dưỡng của một xe
   */
  getMaintenanceLogs: (busId) => {
    return axiosInstance.get(`/operator/maintenance/bus/${busId}`);
  },
};
