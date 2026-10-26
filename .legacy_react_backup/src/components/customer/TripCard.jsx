import React, { useState } from 'react';
import { Clock, MapPin, Bus, ShieldCheck, ChevronDown, ChevronUp, Users, Sparkles } from 'lucide-react';
import { SeatMap } from './SeatMap';

export const TripCard = ({ trip, onProceedBooking }) => {
  const [showSeatMap, setShowSeatMap] = useState(false);

  // Format giờ phút (HH:mm)
  const formatTime = (isoString) => {
    if (!isoString) return '--:--';
    const d = new Date(isoString);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', hour12: false });
  };

  // Format ngày tháng (dd/MM/yyyy)
  const formatDate = (isoString) => {
    if (!isoString) return '';
    const d = new Date(isoString);
    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  };

  // Tính thời gian di chuyển (giờ)
  const calcDuration = (dep, arr) => {
    if (!dep || !arr) return '8h';
    const diffMs = new Date(arr) - new Date(dep);
    const diffHours = Math.round(diffMs / (1000 * 60 * 60) * 10) / 10;
    return `${diffHours} giờ`;
  };

  const isFuta = trip.operatorCode === 'FUTA';
  const isHoangLong = trip.operatorCode === 'HOANGLONG';

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 hover:border-orange-300 shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden">
      {/* Top Main Section */}
      <div className="p-5 sm:p-6 lg:p-7">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          {/* Col 1: Operator & Bus Info */}
          <div className="space-y-3 lg:max-w-xs">
            <div className="flex items-center gap-2.5">
              <span
                className={`px-3 py-1 rounded-xl text-xs font-black tracking-wider uppercase text-white shadow-xs ${
                  isFuta
                    ? 'bg-gradient-to-r from-orange-600 to-amber-500'
                    : isHoangLong
                    ? 'bg-gradient-to-r from-red-600 to-rose-700'
                    : 'bg-gradient-to-r from-blue-600 to-indigo-600'
                }`}
              >
                {trip.operatorCode || 'ĐỐI TÁC'}
              </span>
              <h3 className="font-extrabold text-base text-slate-900 truncate">
                {trip.operatorName}
              </h3>
            </div>

            <div className="flex items-center gap-3 text-xs text-slate-500 font-medium">
              <span className="flex items-center gap-1.5 bg-slate-100 px-2.5 py-1 rounded-lg">
                <Bus className="w-3.5 h-3.5 text-slate-600" />
                {trip.busType}
              </span>
              <span className="text-slate-400 font-mono">BKS: {trip.busPlateNumber}</span>
            </div>

            <div className="flex items-center gap-1.5 text-emerald-600 text-xs font-semibold">
              <ShieldCheck className="w-4 h-4" />
              <span>Chính sách hoàn 150% nếu không có chỗ</span>
            </div>
          </div>

          {/* Col 2: Journey Timeline */}
          <div className="flex-1 max-w-md">
            <div className="flex items-center justify-between gap-3 text-center sm:text-left">
              {/* Giờ khởi hành */}
              <div>
                <div className="text-2xl font-black text-slate-900">
                  {formatTime(trip.departureTime)}
                </div>
                <div className="text-xs font-bold text-slate-700 mt-0.5">{trip.origin}</div>
                <div className="text-[11px] text-slate-400">{formatDate(trip.departureTime)}</div>
              </div>

              {/* Đường kẻ thời lượng */}
              <div className="flex-1 px-4 flex flex-col items-center">
                <span className="text-[11px] font-semibold text-slate-400 mb-1 flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  {calcDuration(trip.departureTime, trip.arrivalTime)}
                </span>
                <div className="w-full relative flex items-center">
                  <div className="w-2 h-2 rounded-full bg-orange-500"></div>
                  <div className="flex-1 h-0.5 bg-dashed border-t-2 border-slate-300"></div>
                  <div className="w-2 h-2 rounded-full bg-slate-400"></div>
                </div>
                <span className="text-[10px] text-slate-400 mt-1 uppercase tracking-wider font-semibold">
                  Chạy Thẳng
                </span>
              </div>

              {/* Giờ đến dự kiến */}
              <div className="text-right">
                <div className="text-2xl font-black text-slate-900">
                  {formatTime(trip.arrivalTime)}
                </div>
                <div className="text-xs font-bold text-slate-700 mt-0.5">{trip.destination}</div>
                <div className="text-[11px] text-slate-400">{formatDate(trip.arrivalTime)}</div>
              </div>
            </div>
          </div>

          {/* Col 3: Price & Action */}
          <div className="flex flex-row lg:flex-col items-center lg:items-end justify-between border-t lg:border-t-0 pt-4 lg:pt-0 border-slate-100 gap-3">
            <div className="text-left lg:text-right">
              <div className="text-[11px] font-semibold text-slate-400 uppercase">Giá mỗi vé từ</div>
              <div className="text-2xl font-black text-orange-600">
                {Number(trip.basePrice).toLocaleString('vi-VN')} đ
              </div>
              <div className="flex items-center gap-1.5 text-xs text-slate-500 mt-0.5">
                <Users className="w-3.5 h-3.5 text-slate-400" />
                <span>Còn <strong className="text-emerald-600">{trip.availableSeats || trip.totalSeats}</strong> ghế trống</span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setShowSeatMap(!showSeatMap)}
              className={`px-5 py-2.5 rounded-xl font-bold text-xs sm:text-sm flex items-center gap-2 transition shadow-sm ${
                showSeatMap
                  ? 'bg-slate-800 text-white hover:bg-slate-900'
                  : 'bg-orange-600 hover:bg-orange-700 text-white shadow-orange-600/25'
              }`}
            >
              <span>{showSeatMap ? 'Đóng Sơ Đồ' : 'Chọn Ghế'}</span>
              {showSeatMap ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </button>
          </div>
        </div>
      </div>

      {/* Accordion Interactive Seat Map */}
      {showSeatMap && (
        <div className="border-t border-slate-100 bg-slate-50/50 p-4 sm:p-6 animate-in slide-in-from-top-4 duration-300">
          <SeatMap trip={trip} onProceedBooking={onProceedBooking} />
        </div>
      )}
    </div>
  );
};
