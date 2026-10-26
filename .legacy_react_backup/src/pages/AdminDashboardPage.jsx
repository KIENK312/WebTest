import React, { useState, useEffect } from 'react';
import { adminApi } from '../api/adminApi';
import { StatCards } from '../components/admin/StatCards';
import { RevenueChart } from '../components/admin/RevenueChart';
import { PartnerManagement } from '../components/admin/PartnerManagement';
import { Shield, RefreshCw, Sparkles, TrendingUp } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const AdminDashboardPage = () => {
  const toast = useToast();

  const [stats, setStats] = useState({});
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadAdminData = async () => {
    setLoading(true);
    try {
      const [overviewRes, partnersRes] = await Promise.all([
        adminApi.getOverview(),
        adminApi.getPartners(),
      ]);

      setStats(overviewRes.data || {});
      setPartners(partnersRes.data || []);
    } catch (err) {
      console.error('Lỗi tải dữ liệu quản trị:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-slate-200 gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-purple-600 mb-1">
            <Shield className="w-4 h-4" />
            Bảng Điều Khiển Quản Trị Hệ Thống
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Tổng Quan Doanh Thu & Đối Tác Vận Tải
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Giám sát thời gian thực toàn bộ doanh số bán vé, tỷ lệ lấp đầy xe và phê duyệt nhà xe mới.
          </p>
        </div>

        <button
          type="button"
          onClick={loadAdminData}
          className="px-4 py-2 rounded-xl bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 font-bold text-xs flex items-center gap-2 transition shadow-xs self-start sm:self-auto"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Cập nhật số liệu</span>
        </button>
      </div>

      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400 bg-white rounded-3xl border border-slate-200">
          <RefreshCw className="w-10 h-10 animate-spin text-purple-600 mb-3" />
          <p className="text-sm font-semibold text-slate-700">Đang tổng hợp dữ liệu báo cáo...</p>
        </div>
      ) : (
        <div className="space-y-8">
          {/* Key Metric Cards */}
          <StatCards stats={stats} />

          {/* Revenue & Growth Chart */}
          <RevenueChart chartData={stats.chartData} />

          {/* Partner Bus Operators Management */}
          <PartnerManagement partners={partners} onReload={loadAdminData} />
        </div>
      )}
    </div>
  );
};
