import axiosInstance from './axiosInstance';

export const adminApi = {
  /**
   * Bảng điều khiển tổng quan cho Admin
   */
  getOverview: () => {
    return axiosInstance.get('/admin/overview');
  },

  /**
   * Danh sách nhà xe đối tác
   */
  getPartners: () => {
    return axiosInstance.get('/admin/partners');
  },

  /**
   * Duyệt / Cập nhật trạng thái nhà xe (ACTIVE, SUSPENDED, INACTIVE)
   */
  updatePartnerStatus: (partnerId, status) => {
    return axiosInstance.put(`/admin/partners/${partnerId}/status`, null, {
      params: { status },
    });
  },

  /**
   * Thêm nhà xe đối tác mới
   */
  createPartner: (partnerData) => {
    return axiosInstance.post('/admin/partners', partnerData);
  },
};
