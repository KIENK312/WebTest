import React, { useState } from 'react';
import { X, Wrench, AlertTriangle, Calendar, Clock, CheckCircle2, ShieldAlert } from 'lucide-react';
import { operatorApi } from '../../api/operatorApi';
import { useToast } from '../../context/ToastContext';

export const ScheduleMaintenanceModal = ({ buses = [], onClose, onSuccess }) => {
  const toast = useToast();

  const [selectedBusId, setSelectedBusId] = useState(buses[0]?.id || '');
  const [maintenanceType, setMaintenanceType] = useState('PERIODIC'); // PERIODIC, REPAIR, INSPECTION
  
  // Mặc định: Bắt đầu từ 2 ngày sau, kéo dài 2 ngày
  const defaultStart = new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16);
  const defaultEnd = new Date(Date.now() + 4 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16);

  const [startTime, setStartTime] = useState(defaultStart);
  const [endTime, setEndTime] = useState(defaultEnd);
  const [description, setDescription] = useState('Bảo dưỡng định kỳ 50.000km & kiểm tra an toàn phanh ABS');
  
  const [loading, setLoading] = useState(false);
  const [conflictError, setConflictError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setConflictError(null);

    if (new Date(endTime) <= new Date(startTime)) {
      toast.error('Thời gian kết thúc phải sau thời gian bắt đầu bảo dưỡng!', 'Lỗi Thời Gian');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        busId: Number(selectedBusId),
        maintenanceType,
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
        description: description.trim(),
      };

      const res = await operatorApi.scheduleMaintenance(payload);
      toast.success(
        'Đã xếp lịch bảo trì thành công! Hệ thống đã kích hoạt vùng đệm an toàn 48h.',
        'Xếp Lịch Bảo Trì Thành Công'
      );
      if (onSuccess) onSuccess(res.data);
      onClose();
    } catch (err) {
      console.error('Lỗi xếp lịch bảo trì 48h:', err);
      const resp = err.response?.data;
      if (resp?.conflictingTripIds && resp.conflictingTripIds.length > 0) {
        setConflictError({
          message: resp.message || 'Xung đột lịch bảo trì với các chuyến xe hiện tại!',
          tripIds: resp.conflictingTripIds,
        });
      } else {
        setConflictError({
          message: resp?.message || 'Xe này đang có xung đột lịch trình trong vùng đệm 48h!',
          tripIds: [],
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const selectedBus = buses.find((b) => b.id === Number(selectedBusId));

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header Modal */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-gradient-to-r from-blue-50 to-indigo-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-blue-600 text-white flex items-center justify-center shadow-md shadow-blue-600/20">
              <Wrench className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-extrabold text-lg text-slate-900">Xếp Lịch Bảo Trì Xe (Đệm 48h)</h3>
              <p className="text-xs text-slate-500">Quy tắc an toàn: Kiểm tra chuyến xe ±48 giờ</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-white text-slate-400 hover:text-slate-700 flex items-center justify-center border border-slate-200 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Rule 48h Notice banner */}
        <div className="px-6 py-3.5 bg-amber-50/80 border-b border-amber-100 flex items-start gap-2.5 text-xs text-amber-900">
          <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
          <span>
            <strong>Quy tắc đệm 48 giờ:</strong> Hệ thống sẽ quét từ <strong>(Bắt đầu - 48h)</strong> đến <strong>(Kết thúc + 48h)</strong>. Nếu xe có chuyến <code>SCHEDULED</code> trong thời gian này, thao tác sẽ bị chặn tự động để bảo đảm an toàn.
          </span>
        </div>

        {/* Conflict Warning if any */}
        {conflictError && (
          <div className="m-6 mb-0 p-4 rounded-2xl bg-rose-50 border border-rose-300 text-rose-950 text-xs animate-in slide-in-from-top-2">
            <div className="flex items-center gap-2 font-bold text-rose-700 text-sm mb-1">
              <AlertTriangle className="w-4 h-4" />
              <span>Xung Đột Lịch Bảo Trì 48h!</span>
            </div>
            <p className="leading-relaxed">{conflictError.message}</p>
            {conflictError.tripIds.length > 0 && (
              <div className="mt-2 pt-2 border-t border-rose-200 flex flex-wrap items-center gap-1.5">
                <span className="font-semibold text-rose-800">Các ID chuyến bị ảnh hưởng:</span>
                {conflictError.tripIds.map((id) => (
                  <span key={id} className="px-2 py-0.5 bg-rose-200 text-rose-900 font-mono font-bold rounded-md">
                    Chuyến #{id}
                  </span>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              Chọn phương tiện bảo trì <span className="text-rose-500">*</span>
            </label>
            <select
              value={selectedBusId}
              onChange={(e) => setSelectedBusId(e.target.value)}
              required
              className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {buses.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.plateNumber} - {b.busType} ({b.operatorName || 'Hệ thống'}) - [Trạng thái: {b.status}]
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-blue-600" />
                Bắt đầu bảo trì <span className="text-rose-500">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-blue-600" />
                Kết thúc bảo trì <span className="text-rose-500">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              Hình thức bảo dưỡng
            </label>
            <div className="grid grid-cols-3 gap-2">
              {[
                { key: 'PERIODIC', label: 'Định kỳ' },
                { key: 'REPAIR', label: 'Sửa chữa' },
                { key: 'INSPECTION', label: 'Đăng kiểm' },
              ].map((item) => (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => setMaintenanceType(item.key)}
                  className={`py-2 px-3 rounded-xl text-xs font-bold border transition ${
                    maintenanceType === item.key
                      ? 'bg-blue-600 text-white border-blue-600 shadow-xs'
                      : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
                  }`}
                >
                  {item.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              Ghi chú nội dung kỹ thuật
            </label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Chi tiết các hạng mục thay dầu, lọc gió, kiểm tra lốp..."
              className="w-full p-3 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition"
            >
              Đóng
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold flex items-center gap-2 shadow-md shadow-blue-600/30 transition disabled:opacity-50"
            >
              <span>{loading ? 'Đang Kiểm Tra 48h...' : 'Xác Nhận Lên Lịch'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
