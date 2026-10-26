import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ticketApi } from '../api/ticketApi';
import { PaymentCountdown } from '../components/customer/PaymentCountdown';
import { RefreshCw, ArrowLeft, Ticket } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const PaymentPage = () => {
  const { ticketId } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!ticketId) return;

    const fetchTicket = async () => {
      setLoading(true);
      try {
        const res = await ticketApi.getTicketById(ticketId);
        setTicket(res.data);
      } catch (err) {
        console.error('Lỗi tải thông tin vé thanh toán:', err);
        toast.error('Không tìm thấy thông tin vé thanh toán!', 'Lỗi');
      } finally {
        setLoading(false);
      }
    };

    fetchTicket();
  }, [ticketId]);

  if (loading) {
    return (
      <div className="min-h-[70vh] flex flex-col items-center justify-center text-slate-500">
        <RefreshCw className="w-10 h-10 text-orange-500 animate-spin mb-3" />
        <span className="text-sm font-semibold">Đang chuẩn bị thông tin thanh toán VietQR...</span>
      </div>
    );
  }

  if (!ticket) {
    return (
      <div className="max-w-md mx-auto py-20 text-center px-4">
        <Ticket className="w-16 h-16 text-slate-300 mx-auto mb-3" />
        <h3 className="text-lg font-bold text-slate-800">Không Tìm Thấy Vé</h3>
        <p className="text-xs text-slate-500 mt-1">Vé ID #{ticketId} không tồn tại hoặc đã bị hủy.</p>
        <button
          onClick={() => navigate('/search')}
          className="mt-4 px-4 py-2 rounded-xl bg-orange-600 text-white font-bold text-xs"
        >
          Tìm Chuyến Xe Khác
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 space-y-6">
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-slate-900 transition"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Quay Lại</span>
      </button>

      <PaymentCountdown ticket={ticket} />
    </div>
  );
};
