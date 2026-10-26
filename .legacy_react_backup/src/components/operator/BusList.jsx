import React, { useState } from 'react';
import { Bus, Wrench, CheckCircle2, AlertTriangle, RefreshCw, Power } from 'lucide-react';
import { operatorApi } from '../../api/operatorApi';
import { useToast } from '../../context/ToastContext';

export const BusList = ({ buses = [], onReload }) => {
  const [updatingId, setUpdatingId] = useState(null);
  const toast = useToast();

  const handleToggleStatus = async (bus) => {
    const nextStatus = bus.status === 'ACTIVE' ? 'MAINTENANCE' : 'ACTIVE';
    setUpdatingId(bus.id);
    try {
      await operatorApi.updateBusStatus(bus.id, nextStatus);
      toast.success(
        `Đã chuyển trạng thái xe ${bus.plateNumber} sang ${nextStatus}!`,
        'Cập Nhật Đội Xe'
      );
      if (onReload) onReload();
    } catch (err) {
      console.error('Lỗi cập nhật trạng thái xe:', err);
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 shadow-sm p-5 sm:p-6 space-y-4">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div>
          <h3 className="font-extrabold text-base text-slate-900 flex items-center gap-2">
            <Bus className="w-5 h-5 text-blue-600" />
            Danh Sách Phương Tiện & Đội Xe
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Quản lý trạng thái xe khách và giám sát lịch bảo dưỡng kỹ thuật định kỳ.
          </p>
        </div>
        <button
          type="button"
          onClick={onReload}
          className="p-2 text-slate-400 hover:text-slate-700 rounded-xl hover:bg-slate-100 transition"
          title="Tải lại danh sách"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[10px]">
              <th className="pb-3 pl-2">Biển Số Xe</th>
              <th className="pb-3">Loại Xe</th>
              <th className="pb-3">Sức Chứa</th>
              <th className="pb-3">Nhà Xe Quản Lý</th>
              <th className="pb-3">Trạng Thái</th>
              <th className="pb-3 pr-2 text-right">Thao Tác Bảo Dưỡng</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
            {buses.map((bus) => {
              const isMaintenance = bus.status === 'MAINTENANCE';
              const isUpdating = updatingId === bus.id;

              return (
                <tr key={bus.id} className="hover:bg-slate-50/70 transition">
                  <td className="py-3.5 pl-2 font-mono font-bold text-slate-900 text-sm">
                    {bus.plateNumber}
                  </td>
                  <td className="py-3.5 text-slate-600">{bus.busType}</td>
                  <td className="py-3.5">
                    <span className="font-bold text-slate-800">{bus.totalSeats}</span> chỗ ngồi/giường
                  </td>
                  <td className="py-3.5 text-slate-600">{bus.operatorName || 'Hệ thống'}</td>
                  <td className="py-3.5">
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold ${
                        isMaintenance
                          ? 'bg-rose-100 text-rose-800 border border-rose-200'
                          : 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                      }`}
                    >
                      <span
                        className={`w-2 h-2 rounded-full ${
                          isMaintenance ? 'bg-rose-500 animate-pulse' : 'bg-emerald-500'
                        }`}
                      />
                      <span>{isMaintenance ? 'Đang Bảo Trì' : 'Đang Hoạt Động'}</span>
                    </span>
                  </td>
                  <td className="py-3.5 pr-2 text-right">
                    <button
                      type="button"
                      disabled={isUpdating}
                      onClick={() => handleToggleStatus(bus)}
                      className={`px-3 py-1.5 rounded-xl font-bold text-xs inline-flex items-center gap-1.5 transition ${
                        isMaintenance
                          ? 'bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-300'
                          : 'bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-300'
                      }`}
                      title={
                        isMaintenance
                          ? 'Mở lại xe về trạng thái hoạt động'
                          : 'Đưa xe vào diện bảo dưỡng kỹ thuật'
                      }
                    >
                      <Power className="w-3.5 h-3.5" />
                      <span>{isMaintenance ? 'Kích Hoạt Xe' : 'Đưa Đi Bảo Trì'}</span>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
