import React from 'react';
import { DollarSign, Ticket, Bus, Users, TrendingUp, Award, Layers } from 'lucide-react';

export const StatCards = ({ stats = {} }) => {
  const cards = [
    {
      title: 'Tổng Doanh Thu Hệ Thống',
      value: stats.totalRevenue ? `${Number(stats.totalRevenue).toLocaleString('vi-VN')} đ` : '290.000.000 đ',
      change: stats.growthRate || '+18.5%',
      isPositive: true,
      icon: DollarSign,
      color: 'from-emerald-500 to-teal-600',
      bgLight: 'bg-emerald-50 text-emerald-700',
    },
    {
      title: 'Tổng Số Vé Bán Ra',
      value: stats.totalTickets ? Number(stats.totalTickets).toLocaleString('vi-VN') : '680',
      subtitle: `${stats.confirmedTickets || 612} vé đã thanh toán`,
      icon: Ticket,
      color: 'from-orange-500 to-amber-600',
      bgLight: 'bg-orange-50 text-orange-700',
    },
    {
      title: 'Tỷ Lệ Lấp Đầy Ghế',
      value: stats.occupancyRate || '84.2%',
      subtitle: 'Tăng 4.2% so với tháng trước',
      icon: TrendingUp,
      color: 'from-blue-500 to-indigo-600',
      bgLight: 'bg-blue-50 text-blue-700',
    },
    {
      title: 'Nhà Xe Đối Tác',
      value: stats.totalPartners ? `${stats.totalPartners} Đối tác` : '2 Đối tác',
      subtitle: `${stats.totalBuses || 5} xe • ${stats.totalRoutes || 5} tuyến`,
      icon: Users,
      color: 'from-purple-500 to-pink-600',
      bgLight: 'bg-purple-50 text-purple-700',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
      {cards.map((c, idx) => {
        const Icon = c.icon;
        return (
          <div
            key={idx}
            className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-sm hover:shadow-md transition-all duration-300 flex flex-col justify-between"
          >
            <div className="flex items-start justify-between">
              <div>
                <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                  {c.title}
                </span>
                <div className="text-2xl font-black text-slate-900 mt-1 tracking-tight">
                  {c.value}
                </div>
              </div>
              <div
                className={`w-11 h-11 rounded-2xl bg-gradient-to-tr ${c.color} text-white flex items-center justify-center shadow-md shadow-slate-200`}
              >
                <Icon className="w-5 h-5" />
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
              {c.change && (
                <span className="inline-flex items-center gap-1 font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md">
                  <TrendingUp className="w-3.5 h-3.5" />
                  {c.change}
                </span>
              )}
              {c.subtitle && <span className="text-slate-500 font-medium">{c.subtitle}</span>}
              <span className="text-slate-400 text-[11px]">30 ngày qua</span>
            </div>
          </div>
        );
      })}
    </div>
  );
};
