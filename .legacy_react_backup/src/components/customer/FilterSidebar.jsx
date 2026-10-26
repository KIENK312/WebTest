import React from 'react';
import { Filter, RotateCcw, Clock, DollarSign, Bus, Check, ArrowUpDown } from 'lucide-react';

export const FilterSidebar = ({
  filters,
  setFilters,
  onReset,
  availableOperators = [],
}) => {
  const handleTimeSlotToggle = (slotKey) => {
    setFilters((prev) => {
      const current = prev.timeSlots || [];
      const updated = current.includes(slotKey)
        ? current.filter((k) => k !== slotKey)
        : [...current, slotKey];
      return { ...prev, timeSlots: updated };
    });
  };

  const handleOperatorToggle = (operatorCode) => {
    setFilters((prev) => {
      const current = prev.operators || [];
      const updated = current.includes(operatorCode)
        ? current.filter((c) => c !== operatorCode)
        : [...current, operatorCode];
      return { ...prev, operators: updated };
    });
  };

  const handleBusTypeToggle = (type) => {
    setFilters((prev) => {
      const current = prev.busTypes || [];
      const updated = current.includes(type)
        ? current.filter((t) => t !== type)
        : [...current, type];
      return { ...prev, busTypes: updated };
    });
  };

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 p-5 sm:p-6 shadow-sm space-y-6">
      {/* Header filter */}
      <div className="flex items-center justify-between pb-4 border-b border-slate-100">
        <div className="flex items-center gap-2 font-black text-slate-900 text-sm">
          <Filter className="w-4 h-4 text-orange-600" />
          <span>Bộ Lọc Chuyến Xe</span>
        </div>
        <button
          type="button"
          onClick={onReset}
          className="flex items-center gap-1 text-xs font-semibold text-slate-400 hover:text-orange-600 transition"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Đặt lại</span>
        </button>
      </div>

      {/* Sắp xếp */}
      <div>
        <label className="block text-xs font-bold text-slate-900 mb-2 flex items-center gap-1.5">
          <ArrowUpDown className="w-3.5 h-3.5 text-slate-500" />
          Sắp xếp theo
        </label>
        <select
          value={filters.sortBy || 'time_asc'}
          onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
          className="w-full h-10 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-orange-500"
        >
          <option value="time_asc">Giờ khởi hành: Sớm nhất</option>
          <option value="time_desc">Giờ khởi hành: Muộn nhất</option>
          <option value="price_asc">Giá vé: Tăng dần (Rẻ nhất)</option>
          <option value="price_desc">Giá vé: Giảm dần</option>
        </select>
      </div>

      {/* Giờ xuất bến */}
      <div>
        <label className="block text-xs font-bold text-slate-900 mb-2.5 flex items-center gap-1.5">
          <Clock className="w-3.5 h-3.5 text-slate-500" />
          Giờ xuất bến
        </label>
        <div className="space-y-2">
          {[
            { key: 'EARLY', label: 'Sáng sớm (00:00 - 06:00)' },
            { key: 'MORNING', label: 'Buổi sáng (06:00 - 12:00)' },
            { key: 'AFTERNOON', label: 'Buổi chiều (12:00 - 18:00)' },
            { key: 'EVENING', label: 'Buổi tối (18:00 - 24:00)' },
          ].map((item) => {
            const isChecked = filters.timeSlots?.includes(item.key);
            return (
              <label
                key={item.key}
                onClick={() => handleTimeSlotToggle(item.key)}
                className={`flex items-center justify-between p-2.5 rounded-xl border text-xs font-semibold cursor-pointer transition ${
                  isChecked
                    ? 'bg-orange-50 border-orange-300 text-orange-900'
                    : 'bg-slate-50/70 border-slate-200 text-slate-700 hover:bg-slate-100'
                }`}
              >
                <span>{item.label}</span>
                <div
                  className={`w-4 h-4 rounded-md border flex items-center justify-center ${
                    isChecked
                      ? 'bg-orange-600 border-orange-600 text-white'
                      : 'border-slate-300 bg-white'
                  }`}
                >
                  {isChecked && <Check className="w-3 h-3 stroke-[3]" />}
                </div>
              </label>
            );
          })}
        </div>
      </div>

      {/* Lọc theo Giá vé tối đa */}
      <div>
        <div className="flex items-center justify-between text-xs font-bold text-slate-900 mb-2">
          <span className="flex items-center gap-1.5">
            <DollarSign className="w-3.5 h-3.5 text-slate-500" />
            Giá vé tối đa
          </span>
          <span className="text-orange-600 font-extrabold font-mono">
            {Number(filters.maxPrice || 1000000).toLocaleString('vi-VN')} đ
          </span>
        </div>
        <input
          type="range"
          min="150000"
          max="1000000"
          step="50000"
          value={filters.maxPrice || 1000000}
          onChange={(e) => setFilters({ ...filters, maxPrice: Number(e.target.value) })}
          className="w-full accent-orange-600 cursor-pointer"
        />
        <div className="flex justify-between text-[10px] text-slate-400 font-medium mt-1">
          <span>150.000 đ</span>
          <span>1.000.000 đ</span>
        </div>
      </div>

      {/* Lọc theo Loại xe */}
      <div>
        <label className="block text-xs font-bold text-slate-900 mb-2 flex items-center gap-1.5">
          <Bus className="w-3.5 h-3.5 text-slate-500" />
          Loại xe
        </label>
        <div className="space-y-1.5">
          {[
            { key: 'Giường nằm', label: 'Xe Giường Nằm' },
            { key: 'Limousine', label: 'Xe VIP Limousine' },
          ].map((type) => {
            const isChecked = filters.busTypes?.includes(type.key);
            return (
              <label
                key={type.key}
                onClick={() => handleBusTypeToggle(type.key)}
                className={`flex items-center justify-between p-2 rounded-xl border text-xs font-semibold cursor-pointer transition ${
                  isChecked
                    ? 'bg-orange-50 border-orange-300 text-orange-900'
                    : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                }`}
              >
                <span>{type.label}</span>
                <div
                  className={`w-4 h-4 rounded-md border flex items-center justify-center ${
                    isChecked
                      ? 'bg-orange-600 border-orange-600 text-white'
                      : 'border-slate-300 bg-white'
                  }`}
                >
                  {isChecked && <Check className="w-3 h-3 stroke-[3]" />}
                </div>
              </label>
            );
          })}
        </div>
      </div>

      {/* Lọc theo Nhà xe */}
      <div>
        <label className="block text-xs font-bold text-slate-900 mb-2 flex items-center gap-1.5">
          Nhà xe đối tác
        </label>
        <div className="space-y-1.5">
          {[
            { code: 'FUTA', name: 'Phương Trang (FUTA)' },
            { code: 'HOANGLONG', name: 'Hoàng Long Asia' },
          ].map((op) => {
            const isChecked = filters.operators?.includes(op.code);
            return (
              <label
                key={op.code}
                onClick={() => handleOperatorToggle(op.code)}
                className={`flex items-center justify-between p-2 rounded-xl border text-xs font-semibold cursor-pointer transition ${
                  isChecked
                    ? 'bg-orange-50 border-orange-300 text-orange-900'
                    : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                }`}
              >
                <span>{op.name}</span>
                <div
                  className={`w-4 h-4 rounded-md border flex items-center justify-center ${
                    isChecked
                      ? 'bg-orange-600 border-orange-600 text-white'
                      : 'border-slate-300 bg-white'
                  }`}
                >
                  {isChecked && <Check className="w-3 h-3 stroke-[3]" />}
                </div>
              </label>
            );
          })}
        </div>
      </div>
    </div>
  );
};
