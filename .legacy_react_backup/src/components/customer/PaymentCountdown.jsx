import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  QrCode, Clock, CheckCircle2, Copy, AlertCircle, 
  ArrowRight, ShieldCheck, Zap, RefreshCw, Ticket, Check 
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { paymentApi } from '../../api/paymentApi';
import { ticketApi } from '../../api/ticketApi';
import { useToast } from '../../context/ToastContext';

export const PaymentCountdown = ({ ticket, onClose }) => {
  const navigate = useNavigate();
  const toast = useToast();

  const [qrData, setQrData] = useState(null);
  const [loadingQr, setLoadingQr] = useState(true);
  const [ticketStatus, setTicketStatus] = useState(ticket?.status || 'PENDING');
  const [isSuccess, setIsSuccess] = useState(ticket?.status === 'CONFIRMED');

  // Đếm ngược 10 phút (600 giây)
  const [timeLeft, setTimeLeft] = useState(600);
  const [copiedAccount, setCopiedAccount] = useState(false);
  const [copiedContent, setCopiedContent] = useState(false);
  const [simulatingWebhook, setSimulatingWebhook] = useState(false);

  const pollingRef = useRef(null);

  // 1. Tải thông tin VietQR từ Backend
  useEffect(() => {
    if (!ticket?.ticketId) return;

    const fetchQr = async () => {
      setLoadingQr(true);
      try {
        const res = await paymentApi.createPaymentQr(ticket.ticketId);
        setQrData(res.data);
      } catch (err) {
        console.error('Lỗi sinh VietQR:', err);
        toast.error('Không thể sinh mã VietQR từ hệ thống!', 'Lỗi Thanh Toán');
      } finally {
        setLoadingQr(false);
      }
    };

    fetchQr();
  }, [ticket?.ticketId]);

  // 2. Đồng hồ đếm ngược 10 phút
  useEffect(() => {
    if (isSuccess || timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          toast.error('Vé của bạn đã hết hạn thời gian giữ chỗ!', 'Hết Thời Gian');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isSuccess, timeLeft]);

  // 3. Polling tự động kiểm tra trạng thái vé sau mỗi 3 giây
  useEffect(() => {
    if (isSuccess || !ticket?.ticketId) return;

    pollingRef.current = setInterval(async () => {
      try {
        const res = await ticketApi.getTicketById(ticket.ticketId);
        const currentTicket = res.data;

        if (currentTicket?.status === 'CONFIRMED') {
          setIsSuccess(true);
          setTicketStatus('CONFIRMED');
          clearInterval(pollingRef.current);

          // Pháo hoa ăn mừng
          try {
            confetti({
              particleCount: 150,
              spread: 80,
              origin: { y: 0.6 },
              colors: ['#ea580c', '#10b981', '#f59e0b', '#3b82f6'],
            });
          } catch (e) {
            // ignore
          }

          toast.success(
            `Thanh toán vé ${currentTicket.ticketCode} thành công! Vé điện tử đã gửi về email.`,
            'Thanh Toán Thành Công!'
          );
        } else if (currentTicket?.status === 'CANCELLED' || currentTicket?.status === 'EXPIRED') {
          setTicketStatus(currentTicket.status);
          clearInterval(pollingRef.current);
        }
      } catch (err) {
        // Polling silent retry
      }
    }, 3000);

    return () => {
      if (pollingRef.current) clearInterval(pollingRef.current);
    };
  }, [isSuccess, ticket?.ticketId]);

  // Format mm:ss
  const formatCountdown = (secs) => {
    const mins = Math.floor(secs / 60);
    const rem = secs % 60;
    return `${mins.toString().padStart(2, '0')}:${rem.toString().padStart(2, '0')}`;
  };

  const copyToClipboard = (text, type) => {
    navigator.clipboard.writeText(text);
    if (type === 'account') {
      setCopiedAccount(true);
      setTimeout(() => setCopiedAccount(false), 2000);
    } else {
      setCopiedContent(true);
      setTimeout(() => setCopiedContent(false), 2000);
    }
    toast.info(`Đã sao chép: ${text}`, 'Đã Sao Chép');
  };

  // Nút test giả lập gọi Webhook từ ngân hàng ngay trên UI
  const handleSimulateWebhook = async () => {
    setSimulatingWebhook(true);
    try {
      const webhookPayload = {
        gateway: 'VIETQR',
        transactionCode: `TX_${Date.now()}`,
        transferContent: qrData?.transferContent || `VE${ticket.ticketId}`,
        transferAmount: ticket.price || 480000,
        bankAccount: qrData?.accountNumber || '10101010',
        transactionDate: new Date().toISOString(),
      };

      await paymentApi.simulateWebhook(webhookPayload);
      toast.success(
        'Đã gửi webhook giả lập thành công! Hệ thống đang tự động đối soát...',
        'Gửi Webhook Test'
      );
    } catch (err) {
      console.error('Lỗi giả lập webhook:', err);
    } finally {
      setSimulatingWebhook(false);
    }
  };

  // Màn hình Thành Công
  if (isSuccess) {
    return (
      <div className="bg-white rounded-3xl border border-emerald-200 p-6 sm:p-8 text-center max-w-lg mx-auto shadow-2xl animate-in zoom-in-95 duration-300">
        <div className="w-20 h-20 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto mb-4 ring-8 ring-emerald-50">
          <CheckCircle2 className="w-10 h-10" />
        </div>

        <span className="px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold uppercase tracking-wider">
          Xác nhận thành công
        </span>

        <h3 className="text-2xl font-black text-slate-900 mt-2">Thanh Toán Hoàn Tất!</h3>
        <p className="text-xs text-slate-500 mt-1">
          Vé điện tử của bạn đã được xuất thành công trên hệ thống. Mã vé tra cứu:
        </p>

        <div className="my-5 p-4 rounded-2xl bg-slate-50 border border-slate-200 font-mono text-center">
          <div className="text-[11px] text-slate-400 uppercase font-sans font-bold">Mã Vé Điện Tử</div>
          <div className="text-xl font-extrabold text-orange-600 tracking-wider mt-1">
            {ticket.ticketCode || `TK-VE${ticket.ticketId}`}
          </div>
          <div className="text-xs text-slate-600 mt-2">
            Ghế: <strong>{ticket.seatNumber}</strong> • Số tiền: <strong>{Number(ticket.price).toLocaleString('vi-VN')} đ</strong>
          </div>
        </div>

        <div className="space-y-2">
          <button
            type="button"
            onClick={() => navigate('/my-tickets')}
            className="w-full py-3 rounded-xl bg-gradient-to-r from-orange-600 to-amber-500 hover:from-orange-700 hover:to-amber-600 text-white font-bold text-sm shadow-md shadow-orange-500/25 flex items-center justify-center gap-2 transition"
          >
            <Ticket className="w-4 h-4" />
            <span>Xem Danh Sách Vé Đã Đặt</span>
          </button>
          {onClose && (
            <button
              type="button"
              onClick={onClose}
              className="w-full py-2.5 rounded-xl text-slate-600 hover:bg-slate-100 font-semibold text-xs transition"
            >
              Đóng Cửa Sổ
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 max-w-xl mx-auto shadow-2xl">
      {/* Header Countdown */}
      <div className="text-center pb-4 border-b border-slate-100">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-orange-50 border border-orange-200 text-orange-700 font-bold text-xs mb-3 animate-pulse">
          <Clock className="w-4 h-4" />
          <span>Thời gian giữ chỗ còn lại:</span>
          <span className="font-mono font-black text-sm text-orange-600">
            {formatCountdown(timeLeft)}
          </span>
        </div>

        <h3 className="font-extrabold text-xl text-slate-900">Quét Mã VietQR Chuyển Khoản</h3>
        <p className="text-xs text-slate-500 mt-1">
          Hệ thống ngân hàng đối soát tự động theo mã nội dung <strong>VE{ticket.ticketId}</strong> trong 3 giây.
        </p>
      </div>

      {/* Main QR Card */}
      <div className="my-6 grid grid-cols-1 sm:grid-cols-2 gap-6 items-center">
        {/* VietQR Code Display */}
        <div className="flex flex-col items-center justify-center p-4 bg-slate-50 rounded-2xl border border-slate-200">
          {loadingQr ? (
            <div className="w-48 h-48 flex flex-col items-center justify-center text-slate-400">
              <RefreshCw className="w-8 h-8 animate-spin text-orange-500 mb-2" />
              <span className="text-xs font-semibold">Đang sinh VietQR...</span>
            </div>
          ) : qrData?.qrCodeUrl ? (
            <img
              src={qrData.qrCodeUrl}
              alt="Mã VietQR"
              className="w-48 h-48 rounded-xl object-contain border border-slate-200 shadow-xs"
            />
          ) : (
            <div className="w-48 h-48 bg-white rounded-xl border border-slate-200 flex flex-col items-center justify-center p-3 text-center">
              <QrCode className="w-16 h-16 text-slate-700 mb-2" />
              <span className="text-[11px] font-bold text-slate-800">VietQR 247</span>
              <span className="text-[10px] text-slate-500 font-mono mt-1">
                VE{ticket.ticketId}
              </span>
            </div>
          )}

          <div className="flex items-center gap-1.5 text-[11px] font-bold text-emerald-600 mt-3">
            <RefreshCw className="w-3 h-3 animate-spin" />
            <span>Tự động kiểm tra giao dịch...</span>
          </div>
        </div>

        {/* Bank Account Details */}
        <div className="space-y-3 text-xs">
          <div>
            <span className="text-slate-400 font-medium">Ngân hàng thụ hưởng:</span>
            <div className="font-bold text-slate-800 text-sm">{qrData?.bankName || 'MBBank (Quân Đội)'}</div>
          </div>

          <div>
            <span className="text-slate-400 font-medium">Số tài khoản:</span>
            <div className="flex items-center justify-between font-mono font-bold text-slate-900 bg-slate-100 p-2 rounded-xl mt-0.5">
              <span>{qrData?.accountNumber || '0388889999'}</span>
              <button
                type="button"
                onClick={() => copyToClipboard(qrData?.accountNumber || '0388889999', 'account')}
                className="text-orange-600 hover:text-orange-700 p-1"
                title="Sao chép số tài khoản"
              >
                {copiedAccount ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>

          <div>
            <span className="text-slate-400 font-medium">Chủ tài khoản:</span>
            <div className="font-bold text-slate-800">{qrData?.accountName || 'CONG TY CP VE XE KHACH VIET NAM'}</div>
          </div>

          <div>
            <span className="text-slate-400 font-medium">Số tiền cần chuyển:</span>
            <div className="font-black text-orange-600 text-lg">
              {Number(ticket.price || 480000).toLocaleString('vi-VN')} đ
            </div>
          </div>

          <div>
            <span className="text-slate-400 font-medium">
              Nội dung chuyển khoản <span className="text-rose-500 font-bold">(BẮT BUỘC)</span>:
            </span>
            <div className="flex items-center justify-between font-mono font-black text-emerald-800 bg-emerald-50 border border-emerald-300 p-2 rounded-xl mt-0.5">
              <span>{qrData?.transferContent || `VE${ticket.ticketId}`}</span>
              <button
                type="button"
                onClick={() => copyToClipboard(qrData?.transferContent || `VE${ticket.ticketId}`, 'content')}
                className="text-emerald-700 hover:text-emerald-800 p-1"
                title="Sao chép nội dung chuyển khoản"
              >
                {copiedContent ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Test Helper Button for User & Examiners */}
      <div className="mt-4 pt-4 border-t border-slate-100 bg-amber-50/70 -mx-6 -mb-6 p-6 rounded-b-3xl">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="text-xs text-amber-900">
            <span className="font-bold flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 text-amber-600" />
              Công cụ thử nghiệm Webhook (Demo Mode)
            </span>
            <p className="text-[11px] text-amber-800 mt-0.5">
              Nhấp nút bên cạnh để gửi webhook xác nhận tiền về ngay lập tức mà không cần quét mã thật.
            </p>
          </div>

          <button
            type="button"
            disabled={simulatingWebhook}
            onClick={handleSimulateWebhook}
            className="w-full sm:w-auto px-4 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-sm transition whitespace-nowrap disabled:opacity-50"
          >
            <Zap className="w-4 h-4" />
            <span>{simulatingWebhook ? 'Đang gửi...' : '⚡ Giả lập Quét Mã'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
