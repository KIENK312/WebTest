import axiosInstance from './axiosInstance';

export const tripApi = {
  /**
   * Tìm kiếm chuyến xe theo điểm xuất phát, điểm đến và ngày khởi hành
   * params: { departurePoint, arrivalPoint, departureDate (YYYY-MM-DD) }
   */
  searchTrips: (params) => {
    return axiosInstance.get('/trips/search', { params });
  },

  /**
   * Lấy sơ đồ ghế và trạng thái chi tiết của từng ghế (AVAILABLE, HELD, BOOKED)
   */
  getTripSeats: (tripId) => {
    return axiosInstance.get(`/trips/${tripId}/seats`);
  },
};
