import axiosInstance from './axiosInstance';

export const paymentApi = {
  /**
   * Sinh thông tin mã QR VietQR cho vé xe (nội dung bắt buộc: VE{ticketId})
   */
  createPaymentQr: (ticketId) => {
    return axiosInstance.post(`/payments/qr/${ticketId}`);
  },

  /**
   * Giả lập gửi Webhook Ngân hàng (dùng để test ngay trên UI frontend)
   */
  simulateWebhook: (webhookData) => {
    return axiosInstance.post('/payments/webhook', webhookData);
  },
};
