import React, { useState, useEffect } from 'react';
import { ticketApi } from '../api/ticketApi';
import { useAuth } from '../context/AuthContext';
import { TicketCard } from '../components/customer/TicketCard';
import { Ticket, Search, RefreshCw, AlertCircle, Phone, Filter } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const MyTicketsPage = () => {
  const { user, isAuthenticated } = useAuth();
  const toast = useToast();

  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchPhone, setSearchPhone] = useState(user?.phoneNumber || '');
  const [activeTab, setActiveTab] = useState('ALL'); // ALL, CONFIRMED, PENDING, CANCELLED

  const fetchTickets = async (phoneQuery = '') => {
    setLoading(true);
    try {
      const params = {};
      if (user?.userId) {
        params.userId = user.userId;
      } else if (phoneQuery) {
        params.phone = phoneQuery.trim();
      } else if (searchPhone) {
        params.phone = searchPhone.trim();
      }

      const res = await ticketApi.getMyTickets(params);
      setTickets(res.data || []);
    } catch (err) {
      console.error('Lỗi tải danh sách vé:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [user?.userId]);

  const handlePhoneSearch = (e) => {
    e.preventDefault();
    if (!searchPhone) return;
    fetchTickets(searchPhone);
  };

  const handleTicketCancelled = (ticketId) => {
    setTickets((prev) =>
      prev.map((t) => (t.ticketId === ticketId ? { ...t, status: 'CANCELLED' } : t))
    );
  };

  const filteredTickets = tickets.filter((t) => {
    if (activeTab === 'ALL') return true;
    return t.status === activeTab;
  });

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-orange-600 mb-1">
            <Ticket className="w-4 h-4" />
            Tra cứu hành trình & Vé xe
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Vé Của Tôi (My Tickets)
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Toàn bộ vé xe điện tử kèm mã QR quét trực tiếp khi lên xe khách.
          </p>
        </div>

        {/* Tra cứu nhanh bằng SĐT nếu khách chưa đăng nhập hoặc muốn tìm vé người thân */}
        <form onSubmit={handlePhoneSearch} className="flex items-center gap-2 self-start sm:self-auto">
          <div className="relative">
            <Phone className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="tel"
              value={searchPhone}
              onChange={(e) => setSearchPhone(e.target.value)}
              placeholder="Nhập SĐT đặt vé..."
              className="h-10 pl-9 pr-3 rounded-xl border border-slate-200 bg-white text-xs font-mono focus:outline-none focus:ring-2 focus:ring-orange-500 w-44 sm:w-52"
            />
          </div>
          <button
            type="submit"
            className="h-10 px-3.5 rounded-xl bg-slate-900 text-white font-bold text-xs hover:bg-slate-800 transition shadow-xs"
          >
            Tra Cứu
          </button>
        </form>
      </div>

      {/* Tabs Filter */}
      <div className="flex items-center justify-between gap-2 overflow-x-auto pb-1">
        <div className="inline-flex rounded-2xl bg-white p-1 border border-slate-200 shadow-2xs">
          {[
            { key: 'ALL', label: 'Tất Cả Vé' },
            { key: 'CONFIRMED', label: 'Đã Thanh Toán' },
            { key: 'PENDING', label: 'Chờ Thanh Toán' },
            { key: 'CANCELLED', label: 'Đã Hủy' },
          ].map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition whitespace-nowrap ${
                activeTab === tab.key
                  ? 'bg-orange-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <button
          type="button"
          onClick={() => fetchTickets()}
          className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition"
          title="Tải lại danh sách vé"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Ticket List */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400 bg-white rounded-3xl border border-slate-200">
          <RefreshCw className="w-10 h-10 animate-spin text-orange-500 mb-3" />
          <p className="text-sm font-semibold text-slate-700">Đang tra cứu vé xe...</p>
        </div>
      ) : filteredTickets.length === 0 ? (
        <div className="py-16 text-center bg-white rounded-3xl border border-slate-200 p-8">
          <Ticket className="w-16 h-16 text-slate-300 mx-auto mb-3" />
          <h3 className="font-bold text-base text-slate-800">Chưa Có Vé Xe Nào</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
            Bạn chưa có vé xe nào ở trạng thái này. Hãy tìm kiếm và giữ chỗ một chuyến xe yêu thích!
          </p>
          <a
            href="/search"
            className="inline-block mt-4 px-5 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs shadow-sm transition"
          >
            Tìm & Đặt Vé Ngay
          </a>
        </div>
      ) : (
        <div className="space-y-5">
          {filteredTickets.map((ticket) => (
            <TicketCard
              key={ticket.ticketId}
              ticket={ticket}
              onTicketCancelled={handleTicketCancelled}
            />
          ))}
        </div>
      )}
    </div>
  );
};
