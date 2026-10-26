import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { Bus, Calendar, Clock, MapPin, User, Phone, CheckCircle2, AlertCircle, XCircle, Trash2 } from 'lucide-react';
import { ticketApi } from '../../api/ticketApi';
import { useToast } from '../../context/ToastContext';

export const TicketCard = ({ ticket, onTicketCancelled }) => {
  const [cancelling, setCancelling] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const toast = useToast();

  const handleCancelTicket = async () => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy vé ${ticket.ticketCode || ticket.ticketId}? Ghế sẽ được hoàn trả lại cho hệ thống.`)) {
      return;
    }

    setCancelling(true);
    try {
      await ticketApi.cancelTicket(ticket.ticketId);
      toast.success(
        `Đã hủy vé ${ticket.ticketCode} thành công. Ghế ${ticket.seatNumber} đã được giải phóng!`,
        'Hủy Vé Thành Công'
      );
      if (onTicketCancelled) {
        onTicketCancelled(ticket.ticketId);
      }
    } catch (err) {
      console.error('Lỗi hủy vé:', err);
    } finally {
      setCancelling(false);
    }
  };

  const isConfirmed = ticket.status === 'CONFIRMED';
  const isPending = ticket.status === 'PENDING';
  const isCancelled = ticket.status === 'CANCELLED';

  // Dữ liệu mã hóa vào QR điện tử để soát vé tại bến
  const qrVerificationPayload = JSON.stringify({
    ticketId: ticket.ticketId,
    ticketCode: ticket.ticketCode,
    seatNumber: ticket.seatNumber,
    customerName: ticket.customerName,
    price: ticket.price,
    status: ticket.status,
    verifiedBy: 'BusTicket Pro Security Platform',
  });

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden flex flex-col md:flex-row justify-between">
      {/* Left Journey Info */}
      <div className="p-6 flex-1 space-y-4">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <span className="font-mono text-xs font-black text-orange-600 bg-orange-50 px-2.5 py-1 rounded-lg border border-orange-200">
              {ticket.ticketCode || `VE-${ticket.ticketId}`}
            </span>
            <span className="text-xs text-slate-400">• Ghế {ticket.seatNumber}</span>
          </div>

          {/* Status Badge */}
          <span
            className={`px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 ${
              isConfirmed
                ? 'bg-emerald-100 text-emerald-800'
                : isPending
                ? 'bg-amber-100 text-amber-800'
                : 'bg-rose-100 text-rose-800'
            }`}
          >
            {isConfirmed && <CheckCircle2 className="w-3.5 h-3.5" />}
            {isPending && <Clock className="w-3.5 h-3.5" />}
            {isCancelled && <XCircle className="w-3.5 h-3.5" />}
            <span>
              {isConfirmed
                ? 'Hợp Lệ (Đã Thanh Toán)'
                : isPending
                ? 'Chờ Thanh Toán'
                : 'Đã Hủy'}
            </span>
          </span>
        </div>

        {/* Passenger & Ticket Info */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs pt-1">
          <div className="flex items-center gap-2 text-slate-600">
            <User className="w-4 h-4 text-slate-400" />
            <span>Hành khách: <strong className="text-slate-900">{ticket.customerName}</strong></span>
          </div>
          <div className="flex items-center gap-2 text-slate-600">
            <Phone className="w-4 h-4 text-slate-400" />
            <span>SĐT: <strong className="text-slate-900">{ticket.customerPhone}</strong></span>
          </div>
          <div className="flex items-center gap-2 text-slate-600">
            <Calendar className="w-4 h-4 text-slate-400" />
            <span>
              Thời gian đặt:{' '}
              <strong className="text-slate-900">
                {ticket.bookingTime ? new Date(ticket.bookingTime).toLocaleString('vi-VN') : 'Mới đặt'}
              </strong>
            </span>
          </div>
          <div className="flex items-center gap-2 text-slate-600">
            <Bus className="w-4 h-4 text-slate-400" />
            <span>Vị trí ghế: <strong className="text-emerald-700 font-extrabold">{ticket.seatNumber}</strong></span>
          </div>
        </div>

        {/* Actions & Price */}
        <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
          <div>
            <span className="text-[11px] text-slate-400 uppercase font-semibold">Giá vé</span>
            <div className="text-lg font-black text-orange-600">
              {Number(ticket.price).toLocaleString('vi-VN')} đ
            </div>
          </div>

          {!isCancelled && (
            <button
              type="button"
              disabled={cancelling}
              onClick={handleCancelTicket}
              className="px-3.5 py-1.5 rounded-xl border border-rose-200 text-rose-600 hover:bg-rose-50 text-xs font-bold flex items-center gap-1.5 transition disabled:opacity-50"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>{cancelling ? 'Đang hủy...' : 'Hủy Vé'}</span>
            </button>
          )}
        </div>
      </div>

      {/* Right QR Code Section (Quét mã khi lên xe) */}
      <div className="bg-slate-50 border-t md:border-t-0 md:border-l border-slate-100 p-6 flex flex-col items-center justify-center text-center w-full md:w-56">
        <div className="p-2.5 bg-white rounded-2xl border border-slate-200 shadow-xs mb-2">
          <QRCodeSVG
            value={qrVerificationPayload}
            size={110}
            level="M"
            includeMargin={false}
          />
        </div>
        <span className="text-[11px] font-bold text-slate-800">Mã QR Vé Điện Tử</span>
        <p className="text-[10px] text-slate-400 mt-0.5">
          Xuất trình mã này cho nhân viên nhà xe khi lên xe
        </p>
      </div>
    </div>
  );
};
