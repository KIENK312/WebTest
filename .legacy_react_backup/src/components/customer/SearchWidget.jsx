import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, ArrowRightLeft, Search } from 'lucide-react';

const POPULAR_LOCATIONS = [
  'Hà Nội',
  'Sài Gòn',
  'Đà Nẵng',
  'Đà Lạt',
  'Sa Pa',
  'Nha Trang',
  'Cần Thơ',
];

export const SearchWidget = ({ initialDeparture = '', initialArrival = '', initialDate = '' }) => {
  const navigate = useNavigate();
  const [departurePoint, setDeparturePoint] = useState(initialDeparture || 'Hà Nội');
  const [arrivalPoint, setArrivalPoint] = useState(initialArrival || 'Đà Nẵng');

  // Mặc định ngày hôm nay (YYYY-MM-DD)
  const todayStr = new Date().toISOString().split('T')[0];
  const [departureDate, setDepartureDate] = useState(initialDate || todayStr);

  const handleSwap = () => {
    const temp = departurePoint;
    setDeparturePoint(arrivalPoint);
    setArrivalPoint(temp);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const query = new URLSearchParams({
      departurePoint: departurePoint.trim(),
      arrivalPoint: arrivalPoint.trim(),
      departureDate: departureDate,
    }).toString();

    navigate(`/search?${query}`);
  };

  return (
    <div className="bg-white rounded-3xl shadow-xl shadow-slate-900/10 border border-slate-200/90 p-4 sm:p-6 lg:p-7 relative z-20">
      <form onSubmit={handleSearch} className="grid grid-cols-1 lg:grid-cols-12 gap-3 lg:gap-4 items-center">
        {/* Điểm đi */}
        <div className="lg:col-span-4 relative group">
          <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1 flex items-center gap-1">
            <MapPin className="w-3.5 h-3.5 text-orange-600" />
            Điểm khởi hành
          </label>
          <div className="relative">
            <input
              type="text"
              list="popular-origins"
              value={departurePoint}
              onChange={(e) => setDeparturePoint(e.target.value)}
              placeholder="Nhập thành phố hoặc bến xe..."
              required
              className="w-full h-12 px-3.5 rounded-xl border border-slate-200 bg-slate-50/70 text-slate-900 font-semibold text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
            />
            <datalist id="popular-origins">
              {POPULAR_LOCATIONS.map((loc) => (
                <option key={loc} value={loc} />
              ))}
            </datalist>
          </div>
        </div>

        {/* Nút Hoán đổi 2 chiều */}
        <div className="lg:col-span-1 flex justify-center -my-2 lg:my-0">
          <button
            type="button"
            onClick={handleSwap}
            className="w-10 h-10 rounded-full bg-slate-100 hover:bg-orange-100 text-slate-600 hover:text-orange-600 border border-slate-200 hover:border-orange-200 flex items-center justify-center transition shadow-xs group"
            title="Đảo chiều điểm đi - điểm đến"
          >
            <ArrowRightLeft className="w-4 h-4 transition-transform group-hover:rotate-180 duration-300" />
          </button>
        </div>

        {/* Điểm đến */}
        <div className="lg:col-span-4 relative group">
          <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1 flex items-center gap-1">
            <MapPin className="w-3.5 h-3.5 text-amber-600" />
            Điểm đến
          </label>
          <div className="relative">
            <input
              type="text"
              list="popular-destinations"
              value={arrivalPoint}
              onChange={(e) => setArrivalPoint(e.target.value)}
              placeholder="Nhập thành phố hoặc điểm đến..."
              required
              className="w-full h-12 px-3.5 rounded-xl border border-slate-200 bg-slate-50/70 text-slate-900 font-semibold text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
            />
            <datalist id="popular-destinations">
              {POPULAR_LOCATIONS.map((loc) => (
                <option key={loc} value={loc} />
              ))}
            </datalist>
          </div>
        </div>

        {/* Ngày đi */}
        <div className="lg:col-span-2 relative group">
          <label className="block text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1 flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5 text-orange-600" />
            Ngày đi
          </label>
          <input
            type="date"
            value={departureDate}
            min={todayStr}
            onChange={(e) => setDepartureDate(e.target.value)}
            required
            className="w-full h-12 px-3.5 rounded-xl border border-slate-200 bg-slate-50/70 text-slate-900 font-semibold text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
          />
        </div>

        {/* Nút Tìm kiếm */}
        <div className="lg:col-span-1 pt-2 lg:pt-5">
          <button
            type="submit"
            className="w-full h-12 rounded-xl bg-gradient-to-r from-orange-600 to-amber-500 hover:from-orange-700 hover:to-amber-600 text-white font-bold flex items-center justify-center gap-2 shadow-md shadow-orange-500/30 hover:scale-[1.02] active:scale-[0.98] transition-all"
            title="Tìm kiếm chuyến xe"
          >
            <Search className="w-5 h-5" />
            <span className="lg:hidden text-sm">Tìm Chuyến</span>
          </button>
        </div>
      </form>

      {/* Gợi ý tuyến phổ biến nhanh */}
      <div className="mt-3.5 pt-3 border-t border-slate-100 flex flex-wrap items-center gap-2 text-xs text-slate-500">
        <span className="font-semibold text-slate-400">Gợi ý nhanh:</span>
        <button
          type="button"
          onClick={() => {
            setDeparturePoint('Hà Nội');
            setArrivalPoint('Đà Nẵng');
          }}
          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-orange-50 hover:text-orange-600 transition"
        >
          Hà Nội → Đà Nẵng
        </button>
        <button
          type="button"
          onClick={() => {
            setDeparturePoint('Sài Gòn');
            setArrivalPoint('Đà Lạt');
          }}
          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-orange-50 hover:text-orange-600 transition"
        >
          Sài Gòn → Đà Lạt
        </button>
        <button
          type="button"
          onClick={() => {
            setDeparturePoint('Hà Nội');
            setArrivalPoint('Sa Pa');
          }}
          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-orange-50 hover:text-orange-600 transition"
        >
          Hà Nội → Sa Pa
        </button>
        <button
          type="button"
          onClick={() => {
            setDeparturePoint('Sài Gòn');
            setArrivalPoint('Nha Trang');
          }}
          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-orange-50 hover:text-orange-600 transition"
        >
          Sài Gòn → Nha Trang
        </button>
      </div>
    </div>
  );
};
