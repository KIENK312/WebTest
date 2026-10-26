import React, { useState } from 'react';
import { X, Calendar, Clock, DollarSign, Bus, MapPin, AlertTriangle, UserCheck, CheckCircle2 } from 'lucide-react';
import { operatorApi } from '../../api/operatorApi';
import { useToast } from '../../context/ToastContext';

export const CreateTripModal = ({ routes = [], buses = [], onClose, onSuccess }) => {
  const toast = useToast();

  const [selectedRouteId, setSelectedRouteId] = useState(routes[0]?.id || '');
  const [selectedBusId, setSelectedBusId] = useState(buses[0]?.id || '');
  
  // Mặc định ngày mai lúc 08:00
  const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
  const tomorrowStr = tomorrow.toISOString().slice(0, 10);
  
  const [departureTime, setDepartureTime] = useState(`${tomorrowStr}T08:00`);
  const [arrivalTime, setArrivalTime] = useState(`${tomorrowStr}T16:00`);
  const [basePrice, setBasePrice] = useState(350000);
  const [driverName, setDriverName] = useState('Trần Văn Bình (Bằng E)');
  const [loading, setLoading] = useState(false);

  const selectedBus = buses.find((b) => b.id === Number(selectedBusId));
  const isBusInMaintenance = selectedBus?.status === 'MAINTENANCE';

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (new Date(arrivalTime) <= new Date(departureTime)) {
      toast.error('Giờ đến dự kiến phải sau giờ xuất bến!', 'Lỗi Thời Gian');
      return;
    }

    if (isBusInMaintenance) {
      toast.error(
        `Phương tiện ${selectedBus.plateNumber} đang ở trạng thái BẢO DƯỠNG (MAINTENANCE). Vui lòng chọn xe khác!`,
        'Xe Đang Bảo Trì'
      );
      return;
    }

    setLoading(true);
    try {
      const payload = {
        routeId: Number(selectedRouteId),
        busId: Number(selectedBusId),
        departureTime: new Date(departureTime).toISOString(),
        arrivalTime: new Date(arrivalTime).toISOString(),
        basePrice: Number(basePrice),
        driverName: driverName.trim(),
      };

      const res = await operatorApi.createTrip(payload);
      toast.success('Tạo chuyến xe mới thành công và đã mở bán vé!', 'Tạo Chuyến Thành Công');
      if (onSuccess) onSuccess(res.data);
      onClose();
    } catch (err) {
      console.error('Lỗi tạo chuyến xe:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header Modal */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-gradient-to-r from-orange-50 to-amber-50/50">
          <div>
            <h3 className="font-extrabold text-lg text-slate-900">Tạo Chuyến Xe & Điều Phối</h3>
            <p className="text-xs text-slate-500">Thiết lập lịch trình, xếp xe và phân công tài xế</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-white text-slate-400 hover:text-slate-700 flex items-center justify-center border border-slate-200 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Warning if selected bus is in maintenance */}
        {isBusInMaintenance && (
          <div className="m-6 mb-0 p-3.5 rounded-2xl bg-rose-50 border border-rose-300 text-rose-950 text-xs flex items-start gap-2.5">
            <AlertTriangle className="w-4 h-4 text-rose-600 flex-shrink-0 mt-0.5" />
            <div>
              <strong className="text-rose-700">CẢNH BÁO XE BẢO DƯỠNG:</strong> Xe{' '}
              <strong>{selectedBus?.plateNumber}</strong> đang ở trạng thái <code>MAINTENANCE</code>. Hệ thống backend sẽ từ chối tạo chuyến với xe này để bảo đảm an toàn kỹ thuật!
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-orange-600" />
              Chọn tuyến đường khai thác <span className="text-rose-500">*</span>
            </label>
            <select
              value={selectedRouteId}
              onChange={(e) => setSelectedRouteId(e.target.value)}
              required
              className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
            >
              {routes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.origin} → {r.destination} ({r.distanceKm} km, ~{r.estimatedDurationHours}h)
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
              <Bus className="w-3.5 h-3.5 text-blue-600" />
              Phương tiện vận hành <span className="text-rose-500">*</span>
            </label>
            <select
              value={selectedBusId}
              onChange={(e) => setSelectedBusId(e.target.value)}
              required
              className={`w-full h-11 px-3 rounded-xl border text-xs font-semibold focus:outline-none focus:ring-2 ${
                isBusInMaintenance
                  ? 'border-rose-400 bg-rose-50 text-rose-900 focus:ring-rose-500'
                  : 'border-slate-200 bg-slate-50 text-slate-900 focus:ring-orange-500'
              }`}
            >
              {buses.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.plateNumber} - {b.busType} [{b.status === 'MAINTENANCE' ? '⚠️ ĐANG BẢO TRÌ' : '✅ SẴN SÀNG'}]
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                Giờ xuất bến <span className="text-rose-500">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={departureTime}
                onChange={(e) => setDepartureTime(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-slate-500" />
                Giờ đến dự kiến <span className="text-rose-500">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={arrivalTime}
                onChange={(e) => setArrivalTime(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-semibold text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <DollarSign className="w-3.5 h-3.5 text-emerald-600" />
                Giá vé cơ sở (VND) <span className="text-rose-500">*</span>
              </label>
              <input
                type="number"
                step="10000"
                min="50000"
                required
                value={basePrice}
                onChange={(e) => setBasePrice(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1">
                <UserCheck className="w-3.5 h-3.5 text-blue-600" />
                Phân công tài xế
              </label>
              <input
                type="text"
                value={driverName}
                onChange={(e) => setDriverName(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
            </div>
          </div>

          <div className="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={loading || isBusInMaintenance}
              className="px-5 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white text-xs font-bold flex items-center gap-2 shadow-md shadow-orange-600/30 transition disabled:opacity-50"
            >
              <span>{loading ? 'Đang Xử Lý...' : 'Xác Nhận Tạo Chuyến'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
