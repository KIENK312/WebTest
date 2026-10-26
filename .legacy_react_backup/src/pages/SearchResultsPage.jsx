import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { tripApi } from '../api/tripApi';
import { SearchWidget } from '../components/customer/SearchWidget';
import { FilterSidebar } from '../components/customer/FilterSidebar';
import { TripCard } from '../components/customer/TripCard';
import { CheckoutModal } from '../components/customer/CheckoutModal';
import { PaymentCountdown } from '../components/customer/PaymentCountdown';
import { Bus, RefreshCw, AlertCircle, ArrowUpDown, Filter, Sparkles } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const SearchResultsPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();

  const departurePoint = searchParams.get('departurePoint') || 'Hà Nội';
  const arrivalPoint = searchParams.get('arrivalPoint') || 'Đà Nẵng';
  const departureDate = searchParams.get('departureDate') || new Date().toISOString().split('T')[0];

  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);

  // Bộ lọc bên trái
  const [filters, setFilters] = useState({
    timeSlots: [], // 'EARLY', 'MORNING', 'AFTERNOON', 'EVENING'
    maxPrice: 1000000,
    busTypes: [],
    operators: [],
    sortBy: 'time_asc',
  });

  // Modal Giữ chỗ & Thanh toán
  const [selectedTrip, setSelectedTrip] = useState(null);
  const [selectedSeat, setSelectedSeat] = useState(null);
  const [checkoutModalOpen, setCheckoutModalOpen] = useState(false);
  const [activeHeldTicket, setActiveHeldTicket] = useState(null); // Khi giữ chỗ thành công -> mở PaymentCountdown

  const fetchTrips = async () => {
    setLoading(true);
    try {
      const res = await tripApi.searchTrips({
        departurePoint,
        arrivalPoint,
        departureDate,
      });
      setTrips(res.data || []);
    } catch (err) {
      console.error('Lỗi tìm kiếm chuyến xe:', err);
      // Toast được axiosInstance kích hoạt
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrips();
  }, [departurePoint, arrivalPoint, departureDate]);

  // Bộ lọc logic phía client
  const filteredTrips = useMemo(() => {
    return trips
      .filter((t) => {
        // Lọc giá vé tối đa
        if (filters.maxPrice && t.basePrice > filters.maxPrice) {
          return false;
        }

        // Lọc nhà xe
        if (filters.operators?.length > 0 && !filters.operators.includes(t.operatorCode)) {
          return false;
        }

        // Lọc loại xe
        if (filters.busTypes?.length > 0) {
          const matchType = filters.busTypes.some((bt) =>
            t.busType?.toLowerCase().includes(bt.toLowerCase())
          );
          if (!matchType) return false;
        }

        // Lọc khung giờ xuất bến
        if (filters.timeSlots?.length > 0) {
          const hour = new Date(t.departureTime).getHours();
          const inSlot = filters.timeSlots.some((slot) => {
            if (slot === 'EARLY') return hour >= 0 && hour < 6;
            if (slot === 'MORNING') return hour >= 6 && hour < 12;
            if (slot === 'AFTERNOON') return hour >= 12 && hour < 18;
            if (slot === 'EVENING') return hour >= 18 && hour < 24;
            return false;
          });
          if (!inSlot) return false;
        }

        return true;
      })
      .sort((a, b) => {
        if (filters.sortBy === 'price_asc') return a.basePrice - b.basePrice;
        if (filters.sortBy === 'price_desc') return b.basePrice - a.basePrice;
        if (filters.sortBy === 'time_desc')
          return new Date(b.departureTime) - new Date(a.departureTime);
        return new Date(a.departureTime) - new Date(b.departureTime); // default: time_asc
      });
  }, [trips, filters]);

  const handleProceedBooking = (trip, seat) => {
    setSelectedTrip(trip);
    setSelectedSeat(seat);
    setCheckoutModalOpen(true);
  };

  const handleBookingSuccess = (holdSeatResponse) => {
    setCheckoutModalOpen(false);
    setActiveHeldTicket(holdSeatResponse);
  };

  const resetFilters = () => {
    setFilters({
      timeSlots: [],
      maxPrice: 1000000,
      busTypes: [],
      operators: [],
      sortBy: 'time_asc',
    });
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Search Header Widget */}
      <SearchWidget
        initialDeparture={departurePoint}
        initialArrival={arrivalPoint}
        initialDate={departureDate}
      />

      {/* Main Grid: Filters on Left, Trips on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Filter Sidebar */}
        <div className="lg:col-span-4 sticky top-24">
          <FilterSidebar
            filters={filters}
            setFilters={setFilters}
            onReset={resetFilters}
          />
        </div>

        {/* Right Trips List */}
        <div className="lg:col-span-8 space-y-5">
          {/* Top Result Meta */}
          <div className="bg-white rounded-2xl border border-slate-200/90 p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-2xs">
            <div>
              <h2 className="font-extrabold text-base text-slate-900">
                {departurePoint} → {arrivalPoint}
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Ngày {new Date(departureDate).toLocaleDateString('vi-VN')} • Tìm thấy{' '}
                <strong className="text-orange-600">{filteredTrips.length}</strong> chuyến xe khả dụng
              </p>
            </div>

            <button
              type="button"
              onClick={fetchTrips}
              className="text-xs font-bold text-slate-600 hover:text-orange-600 flex items-center gap-1.5 self-start sm:self-auto"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              <span>Làm mới</span>
            </button>
          </div>

          {/* Loading State */}
          {loading ? (
            <div className="py-20 flex flex-col items-center justify-center text-slate-400 bg-white rounded-3xl border border-slate-200">
              <RefreshCw className="w-10 h-10 animate-spin text-orange-500 mb-3" />
              <p className="text-sm font-semibold text-slate-700">Đang tìm kiếm chuyến xe khả dụng...</p>
              <span className="text-xs text-slate-400 mt-1">Đang tự động loại bỏ xe dính lịch bảo trì 48h</span>
            </div>
          ) : filteredTrips.length === 0 ? (
            <div className="py-16 text-center bg-white rounded-3xl border border-slate-200 p-8">
              <Bus className="w-16 h-16 text-slate-300 mx-auto mb-3" />
              <h3 className="font-bold text-lg text-slate-800">Không Tìm Thấy Chuyến Xe Phù Hợp</h3>
              <p className="text-xs text-slate-500 max-w-md mx-auto mt-1">
                Không có chuyến xe nào từ <strong>{departurePoint}</strong> đến <strong>{arrivalPoint}</strong> trong ngày đã chọn hoặc bộ lọc của bạn quá chặt chẽ.
              </p>
              <button
                type="button"
                onClick={resetFilters}
                className="mt-4 px-4 py-2 rounded-xl bg-orange-600 text-white text-xs font-bold shadow-xs hover:bg-orange-700 transition"
              >
                Đặt Lại Bộ Lọc
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredTrips.map((trip) => (
                <TripCard
                  key={trip.tripId}
                  trip={trip}
                  onProceedBooking={(seat) => handleProceedBooking(trip, seat)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Modal Nhập Thông Tin Hành Khách (Bước 1) */}
      {checkoutModalOpen && selectedTrip && selectedSeat && (
        <CheckoutModal
          trip={selectedTrip}
          seat={selectedSeat}
          onClose={() => setCheckoutModalOpen(false)}
          onBookingSuccess={handleBookingSuccess}
        />
      )}

      {/* Modal Đếm Ngược Giữ Chỗ 10 Phút & VietQR (Bước 2) */}
      {activeHeldTicket && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200 overflow-y-auto">
          <div className="my-8 w-full max-w-xl">
            <PaymentCountdown
              ticket={activeHeldTicket}
              onClose={() => {
                setActiveHeldTicket(null);
                fetchTrips(); // reload seat status
              }}
            />
          </div>
        </div>
      )}
    </div>
  );
};
