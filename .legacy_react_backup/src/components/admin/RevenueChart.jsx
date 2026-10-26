import React, { useState } from 'react';
import { TrendingUp, BarChart3, Calendar, DollarSign } from 'lucide-react';

export const RevenueChart = ({ chartData = [] }) => {
  const [hoveredIdx, setHoveredIdx] = useState(null);

  const data = chartData.length > 0 ? chartData : [
    { month: 'T5/26', revenue: 125000000, tickets: 310 },
    { month: 'T6/26', revenue: 185000000, tickets: 450 },
    { month: 'T7/26', revenue: 240000000, tickets: 590 },
    { month: 'T8/26', revenue: 195000000, tickets: 480 },
    { month: 'T9/26', revenue: 210000000, tickets: 510 },
    { month: 'T10/26', revenue: 290000000, tickets: 680 },
  ];

  const maxRevenue = Math.max(...data.map((d) => d.revenue));

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-6 shadow-sm space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
        <div>
          <div className="flex items-center gap-2 font-black text-slate-900 text-base">
            <BarChart3 className="w-5 h-5 text-orange-600" />
            <span>Biểu Đồ Tăng Trưởng Doanh Thu & Số Vé (6 Tháng Gần Nhất)</span>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Phân tích số liệu doanh thu thực tế và lưu lượng hành khách toàn nền tảng.
          </p>
        </div>
        <div className="flex items-center gap-3 text-xs font-semibold">
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded-full bg-orange-500"></span>
            <span>Doanh thu (VND)</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded-full bg-slate-400"></span>
            <span>Số vé bán</span>
          </div>
        </div>
      </div>

      {/* SVG Bar Chart Visualization */}
      <div className="h-64 sm:h-72 w-full pt-4 flex items-end justify-between gap-2 sm:gap-6 px-2 sm:px-6 relative">
        {data.map((item, idx) => {
          const heightPercent = Math.round((item.revenue / maxRevenue) * 80) + 15;
          const isHovered = hoveredIdx === idx;

          return (
            <div
              key={idx}
              onMouseEnter={() => setHoveredIdx(idx)}
              onMouseLeave={() => setHoveredIdx(null)}
              className="flex-1 flex flex-col items-center h-full justify-end group cursor-pointer relative"
            >
              {/* Tooltip on hover */}
              {isHovered && (
                <div className="absolute -top-12 z-20 bg-slate-900 text-white text-[11px] px-3 py-1.5 rounded-xl shadow-xl pointer-events-none whitespace-nowrap animate-in fade-in zoom-in-95">
                  <div className="font-bold">{item.month}</div>
                  <div className="text-orange-400 font-mono font-bold">
                    {Number(item.revenue).toLocaleString('vi-VN')} đ
                  </div>
                  <div className="text-slate-300 font-mono">{item.tickets} vé đã bán</div>
                </div>
              )}

              {/* Bar */}
              <div
                style={{ height: `${heightPercent}%` }}
                className={`w-full max-w-[48px] rounded-t-2xl transition-all duration-300 relative overflow-hidden ${
                  isHovered
                    ? 'bg-gradient-to-t from-orange-600 to-amber-400 shadow-lg shadow-orange-500/30 scale-x-105'
                    : 'bg-gradient-to-t from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-400'
                }`}
              >
                {/* Subtle sheen highlight */}
                <div className="absolute inset-x-0 top-0 h-2 bg-white/30 rounded-t-2xl"></div>
              </div>

              {/* Month label */}
              <div className="mt-3 text-xs font-bold text-slate-600 group-hover:text-orange-600 transition">
                {item.month}
              </div>
              <div className="text-[10px] text-slate-400 font-mono">
                {item.tickets} vé
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
