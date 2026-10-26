import React, { useState, useEffect } from 'react';
import { operatorApi } from '../api/operatorApi';
import { useAuth } from '../context/AuthContext';
import { BusList } from '../components/operator/BusList';
import { CreateTripModal } from '../components/operator/CreateTripModal';
import { ScheduleMaintenanceModal } from '../components/operator/ScheduleMaintenanceModal';
import { 
  Bus, Wrench, Calendar, Plus, RefreshCw, AlertTriangle, 
  CheckCircle2, Clock, Users, ArrowRight 
} from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const OperatorDashboardPage = () => {
  const { user } = useAuth();
  const toast = useToast();

  const [activeTab, setActiveTab] = useState('TRIPS'); // 'TRIPS' or 'BUSES'
  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);

  const [createTripOpen, setCreateTripOpen] = useState(false);
  const [maintenanceModalOpen, setMaintenanceModalOpen] = useState(false);

  const loadAllData = async () => {
    setLoading(true);
    try {
      const operatorId = user?.operatorId || null;

      const [busesRes, routesRes, tripsRes] = await Promise.all([
        operatorApi.getBuses(operatorId),
        operatorApi.getRoutes(operatorId),
        operatorApi.getTrips(operatorId),
      ]);

      setBuses(busesRes.data || []);
      setRoutes(routesRes.data || []);
      setTrips(tripsRes.data || []);
    } catch (err) {
      console.error('Lỗi tải dữ liệu nhà xe:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, [user?.operatorId]);

  const activeBusesCount = buses.filter((b) => b.status === 'ACTIVE').length;
  const maintenanceBusesCount = buses.filter((b) => b.status === 'MAINTENANCE').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-slate-200 gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-blue-600 mb-1">
            <Wrench className="w-4 h-4" />
            Cổng Điều Hành Vận Tải
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Quản Lý Nhà Xe & Lịch Trình Xe Khách
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Nhà xe đối tác: <strong className="text-slate-800">{user?.fullName || 'Phương Trang (FUTA)'}</strong> • Giám sát an toàn 48h
          </p>
        </div>

        <div className="flex items-center gap-2.5 self-start sm:self-auto">
          <button
            type="button"
            onClick={() => setMaintenanceModalOpen(true)}
            className="px-4 py-2.5 rounded-xl border border-blue-300 bg-blue-50 hover:bg-blue-100 text-blue-700 font-bold text-xs flex items-center gap-1.5 transition shadow-xs"
          >
            <Wrench className="w-4 h-4" />
            <span>Xếp Lịch Bảo Trì 48h</span>
          </button>

          <button
            type="button"
            onClick={() => setCreateTripOpen(true)}
            className="px-4 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-md shadow-orange-600/25 transition"
          >
            <Plus className="w-4 h-4" />
            <span>Tạo Chuyến Xe Mới</span>
          </button>
        </div>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <div className="bg-white rounded-3xl border border-slate-200/90 p-5 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Xe Sẵn Sàng Chạy
            </span>
            <div className="text-2xl font-black text-emerald-600 mt-1">
              {activeBusesCount} Phương tiện
            </div>
            <span className="text-[11px] text-slate-400 mt-1 block">Đủ điều kiện xếp chuyến</span>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white rounded-3xl border border-slate-200/90 p-5 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Xe Đang Bảo Dưỡng
            </span>
            <div className="text-2xl font-black text-rose-600 mt-1">
              {maintenanceBusesCount} Xe (Khóa)
            </div>
            <span className="text-[11px] text-slate-400 mt-1 block">Chặn xếp chuyến tự động</span>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-rose-100 text-rose-600 flex items-center justify-center">
            <Wrench className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white rounded-3xl border border-slate-200/90 p-5 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Chuyến Xe Đang Mở
            </span>
            <div className="text-2xl font-black text-blue-600 mt-1">
              {trips.length} Chuyến
            </div>
            <span className="text-[11px] text-slate-400 mt-1 block">{routes.length} Tuyến đường</span>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-blue-100 text-blue-600 flex items-center justify-center">
            <Bus className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center justify-between border-b border-slate-200">
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={() => setActiveTab('TRIPS')}
            className={`pb-3 text-sm font-extrabold border-b-2 transition flex items-center gap-2 ${
              activeTab === 'TRIPS'
                ? 'border-orange-600 text-orange-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Calendar className="w-4 h-4" />
            <span>Lịch Trình Chuyến Xe ({trips.length})</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('BUSES')}
            className={`pb-3 text-sm font-extrabold border-b-2 transition flex items-center gap-2 ${
              activeTab === 'BUSES'
                ? 'border-orange-600 text-orange-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Bus className="w-4 h-4" />
            <span>Quản Lý Đội Xe & Bảo Trì 48h ({buses.length})</span>
          </button>
        </div>

        <button
          type="button"
          onClick={loadAllData}
          className="text-xs font-bold text-slate-400 hover:text-slate-700 pb-3 flex items-center gap-1"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Làm mới</span>
        </button>
      </div>

      {/* Content by Tab */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400 bg-white rounded-3xl border border-slate-200">
          <RefreshCw className="w-10 h-10 animate-spin text-blue-500 mb-3" />
          <p className="text-sm font-semibold text-slate-700">Đang tải dữ liệu nhà xe...</p>
        </div>
      ) : activeTab === 'TRIPS' ? (
        <div className="bg-white rounded-3xl border border-slate-200/90 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="font-extrabold text-base text-slate-900">
              Danh Sách Chuyến Xe Theo Lịch Trình
            </h3>
            <span className="text-xs text-slate-500">
              Kiểm tra xung đột xe bảo trì trước khi cho phép chạy
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[10px]">
                  <th className="pb-3 pl-2">Tuyến Đường</th>
                  <th className="pb-3">Giờ Xuất Bến</th>
                  <th className="pb-3">Giờ Đến Dự Kiến</th>
                  <th className="pb-3">Phương Tiện / BKS</th>
                  <th className="pb-3">Giá Vé</th>
                  <th className="pb-3">Ghế Trống</th>
                  <th className="pb-3 pr-2 text-right">Trạng Thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
                {trips.map((trip) => (
                  <tr key={trip.tripId} className="hover:bg-slate-50/70 transition">
                    <td className="py-3.5 pl-2 font-bold text-slate-900 text-sm">
                      {trip.origin} → {trip.destination}
                    </td>
                    <td className="py-3.5 font-mono text-slate-800">
                      {new Date(trip.departureTime).toLocaleString('vi-VN')}
                    </td>
                    <td className="py-3.5 font-mono text-slate-500">
                      {new Date(trip.arrivalTime).toLocaleString('vi-VN')}
                    </td>
                    <td className="py-3.5">
                      <span className="font-mono font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded-md">
                        {trip.busPlateNumber}
                      </span>
                      <div className="text-[10px] text-slate-400 mt-0.5">{trip.busType}</div>
                    </td>
                    <td className="py-3.5 font-bold text-orange-600 font-mono">
                      {Number(trip.basePrice).toLocaleString('vi-VN')} đ
                    </td>
                    <td className="py-3.5">
                      <strong className="text-emerald-600">{trip.availableSeats || trip.totalSeats}</strong>/{trip.totalSeats}
                    </td>
                    <td className="py-3.5 pr-2 text-right">
                      <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">
                        {trip.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="space-y-6">
          <BusList buses={buses} onReload={loadAllData} />
        </div>
      )}

      {/* Modal Tạo Chuyến */}
      {createTripOpen && (
        <CreateTripModal
          routes={routes}
          buses={buses}
          onClose={() => setCreateTripOpen(false)}
          onSuccess={loadAllData}
        />
      )}

      {/* Modal Xếp Lịch Bảo Trì 48h */}
      {maintenanceModalOpen && (
        <ScheduleMaintenanceModal
          buses={buses}
          onClose={() => setMaintenanceModalOpen(false)}
          onSuccess={loadAllData}
        />
      )}
    </div>
  );
};
