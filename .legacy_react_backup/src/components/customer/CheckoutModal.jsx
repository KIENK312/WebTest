import React, { useState } from 'react';
import { X, User, Phone, Mail, MapPin, ShieldCheck, Clock, AlertTriangle, ArrowRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { ticketApi } from '../../api/ticketApi';
import { useToast } from '../../context/ToastContext';

export const CheckoutModal = ({ trip, seat, onClose, onBookingSuccess }) => {
  const { user } = useAuth();
  const toast = useToast();

  const [customerName, setCustomerName] = useState(user?.fullName || '');
  const [customerPhone, setCustomerPhone] = useState('0987654321');
  const [customerEmail, setCustomerEmail] = useState(user?.email || 'khachhang@gmail.com');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!seat || !trip) return;

    setLoading(true);
    try {
      const payload = {
        tripId: trip.tripId,
        seatId: seat.seatId,
        userId: user?.userId || null,
        customerName: customerName.trim(),
        customerPhone: customerPhone.trim(),
        customerEmail: customerEmail.trim(),
      };

      const res = await ticketApi.holdSeat(payload);
      const holdData = res.data;

      toast.success(
        `Giữ chỗ thành công ghế ${seat.seatNumber}! Vé được giữ trong 15 phút.`,
        'Giữ Chỗ Thành Công'
      );

      onBookingSuccess(holdData);
    } catch (err) {
      console.error('Lỗi khi giữ chỗ:', err);
      // Lỗi 409 hoặc 400 đã được axiosInstance bắt và bắn Toast
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header Modal */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-gradient-to-r from-orange-50 to-amber-50/50">
          <div>
            <h3 className="font-extrabold text-lg text-slate-900">Thông Tin Hành Khách</h3>
            <p className="text-xs text-slate-500">Bước 1/2: Giữ chỗ & Xác nhận vé điện tử</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-white text-slate-400 hover:text-slate-700 flex items-center justify-center border border-slate-200 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Journey Summary snippet */}
        <div className="px-6 py-4 bg-slate-50/80 border-b border-slate-100 flex items-center justify-between text-xs">
          <div>
            <span className="font-bold text-slate-900">{trip.origin} → {trip.destination}</span>
            <div className="text-slate-500 mt-0.5">{trip.operatorName} • {trip.busPlateNumber}</div>
          </div>
          <div className="text-right">
            <span className="font-bold text-emerald-700 bg-emerald-100 px-2 py-0.5 rounded-md">
              Ghế {seat.seatNumber} (Tầng {seat.floorNumber})
            </span>
            <div className="font-black text-orange-600 text-sm mt-1">
              {Number(seat.price || trip.basePrice).toLocaleString('vi-VN')} đ
            </div>
          </div>
        </div>

        {/* Booking Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-slate-400" />
              Họ và tên hành khách <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              placeholder="Ví dụ: Nguyễn Văn An"
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50/50 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1.5">
              <Phone className="w-3.5 h-3.5 text-slate-400" />
              Số điện thoại nhận vé <span className="text-rose-500">*</span>
            </label>
            <input
              type="tel"
              required
              value={customerPhone}
              onChange={(e) => setCustomerPhone(e.target.value)}
              placeholder="Ví dụ: 0912345678"
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50/50 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition font-mono"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1.5">
              <Mail className="w-3.5 h-3.5 text-slate-400" />
              Email nhận vé điện tử & hóa đơn
            </label>
            <input
              type="email"
              value={customerEmail}
              onChange={(e) => setCustomerEmail(e.target.value)}
              placeholder="Ví dụ: nguyenvanan@gmail.com"
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50/50 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
            />
          </div>

          <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex items-start gap-2">
            <Clock className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
            <span>
              Hệ thống áp dụng <strong>Optimistic Locking</strong>: Ghế sẽ được tạm khóa trong 15 phút để bạn chuyển khoản thanh toán.
            </span>
          </div>

          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition"
            >
              Hủy Bỏ
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white text-xs font-bold flex items-center gap-2 shadow-md shadow-orange-600/30 transition hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50"
            >
              <span>{loading ? 'Đang Khóa Ghế...' : 'Tiếp Tục Thanh Toán'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
