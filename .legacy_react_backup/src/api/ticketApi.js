import axiosInstance from './axiosInstance';

export const ticketApi = {
  /**
   * Giữ chỗ ghế trong 15 phút (Optimistic Locking)
   */
  holdSeat: (payload) => {
    return axiosInstance.post('/tickets/hold', payload);
  },

  /**
   * Tra cứu thông tin vé và trạng thái thanh toán theo ID
   */
  getTicketById: (ticketId) => {
    return axiosInstance.get(`/tickets/${ticketId}`);
  },

  /**
   * Lấy lịch sử vé của người dùng
   */
  getMyTickets: (params = {}) => {
    return axiosInstance.get('/tickets/my-history', { params });
  },

  /**
   * Hủy vé hoặc hủy giữ chỗ
   */
  cancelTicket: (ticketId) => {
    return axiosInstance.put(`/tickets/cancel/${ticketId}`);
  },
};
