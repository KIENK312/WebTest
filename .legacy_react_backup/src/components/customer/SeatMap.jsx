import React, { useState, useEffect } from 'react';
import { tripApi } from '../../api/tripApi';
import { Lock, Check, Layers, UserCheck, AlertCircle, RefreshCw } from 'lucide-react';
import { useToast } from '../../context/ToastContext';

export const SeatMap = ({ trip, onProceedBooking }) => {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFloor, setActiveFloor] = useState(1);
  const [selectedSeat, setSelectedSeat] = useState(null); // Chọn 1 ghế cho luồng giữ chỗ
  const toast = useToast();

  const fetchSeats = async () => {
    setLoading(true);
    try {
      const res = await tripApi.getTripSeats(trip.tripId);
      setSeats(res.data || []);
    } catch (err) {
      console.error('Lỗi tải sơ đồ ghế:', err);
      toast.error('Không thể tải sơ đồ ghế của chuyến xe!', 'Lỗi Sơ Đồ Ghế');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (trip?.tripId) {
      fetchSeats();
      setSelectedSeat(null);
    }
  }, [trip?.tripId]);

  const floor1Seats = seats.filter((s) => s.floorNumber === 1);
  const floor2Seats = seats.filter((s) => s.floorNumber === 2);
  const currentFloorSeats = activeFloor === 1 ? floor1Seats : floor2Seats;

  const handleSeatClick = (seat) => {
    if (seat.status !== 'AVAILABLE') {
      toast.warning(
        `Ghế ${seat.seatNumber} hiện đã có khách đặt hoặc đang được giữ chỗ!`,
        'Ghế Đã Có Người'
      );
      return;
    }

    if (selectedSeat?.seatId === seat.seatId) {
      setSelectedSeat(null); // Bỏ chọn
    } else {
      setSelectedSeat(seat);
    }
  };

  if (loading) {
    return (
      <div className="py-12 flex flex-col items-center justify-center text-slate-500">
        <RefreshCw className="w-8 h-8 text-orange-500 animate-spin mb-2" />
        <span className="text-xs font-semibold">Đang tải sơ đồ ghế thời gian thực...</span>
      </div>
    );
  }

  return (
    <div className="bg-slate-50/90 rounded-2xl border border-slate-200 p-5 sm:p-6">
      {/* Header Seat Map */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-200 gap-3">
        <div>
          <h4 className="font-extrabold text-base text-slate-900 flex items-center gap-2">
            <Layers className="w-5 h-5 text-orange-600" />
            Sơ Đồ Ghế Xe {trip.busPlateNumber} ({trip.busType})
          </h4>
          <p className="text-xs text-slate-500 mt-0.5">
            Chọn vị trí ghế ngồi mong muốn. Vị trí sẽ được giữ độc quyền trong 15 phút sau khi xác nhận.
          </p>
        </div>

        {/* Floor switcher */}
        <div className="inline-flex rounded-xl bg-white p-1 border border-slate-200 shadow-2xs self-start sm:self-auto">
          <button
            type="button"
            onClick={() => setActiveFloor(1)}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition ${
              activeFloor === 1
                ? 'bg-orange-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Tầng 1 (Dưới)
          </button>
          <button
            type="button"
            onClick={() => setActiveFloor(2)}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition ${
              activeFloor === 2
                ? 'bg-orange-600 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Tầng 2 (Trên)
          </button>
        </div>
      </div>

      {/* Legend - Chú thích màu sắc */}
      <div className="flex flex-wrap items-center justify-center gap-4 sm:gap-8 py-4 text-xs font-semibold text-slate-600">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-white border-2 border-slate-300 shadow-2xs"></div>
          <span>Ghế trống (Còn chỗ)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-emerald-600 text-white flex items-center justify-center shadow-xs">
            <Check className="w-3.5 h-3.5" />
          </div>
          <span>Ghế bạn đang chọn</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-slate-200 text-slate-400 border border-slate-300 flex items-center justify-center">
            <Lock className="w-3 h-3" />
          </div>
          <span>Đã bán / Đang giữ chỗ</span>
        </div>
      </div>

      {/* Cabin Visual Representation */}
      <div className="max-w-xl mx-auto bg-white rounded-3xl border-2 border-dashed border-slate-200 p-6 my-2 shadow-xs">
        {/* Vị trí Vô lăng / Bác tài */}
        <div className="flex items-center justify-between pb-4 mb-4 border-b border-slate-100 text-xs font-bold text-slate-400">
          <span className="flex items-center gap-1.5">
            <UserCheck className="w-4 h-4 text-slate-500" />
            Vị trí Bác Tài / Cửa lên xuống
          </span>
          <span className="uppercase tracking-wider text-[10px] px-2 py-0.5 rounded bg-slate-100 text-slate-600">
            {activeFloor === 1 ? 'TẦNG DƯỚI' : 'TẦNG TRÊN'}
          </span>
        </div>

        {/* Lưới ghế 3 cột chuẩn xe giường nằm/VIP */}
        <div className="grid grid-cols-3 sm:grid-cols-3 gap-3.5 sm:gap-4 justify-items-center">
          {currentFloorSeats.map((seat) => {
            const isAvailable = seat.status === 'AVAILABLE';
            const isSelected = selectedSeat?.seatId === seat.seatId;

            return (
              <button
                key={seat.seatId}
                type="button"
                disabled={!isAvailable}
                onClick={() => handleSeatClick(seat)}
                className={`relative w-16 sm:w-20 h-16 sm:h-20 rounded-2xl flex flex-col items-center justify-center transition-all duration-200 group ${
                  isSelected
                    ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/30 ring-4 ring-emerald-200 scale-105'
                    : isAvailable
                    ? 'bg-white hover:bg-emerald-50 text-slate-800 border-2 border-slate-300 hover:border-emerald-500 hover:scale-102 shadow-xs'
                    : 'bg-slate-100 text-slate-400 border border-slate-200 cursor-not-allowed opacity-60'
                }`}
              >
                {/* Seat headrest shape */}
                <span
                  className={`w-7 sm:w-9 h-2 rounded-t-md mb-1.5 ${
                    isSelected ? 'bg-emerald-400' : isAvailable ? 'bg-slate-300 group-hover:bg-emerald-300' : 'bg-slate-300'
                  }`}
                />

                <span className="font-extrabold text-xs sm:text-sm tracking-wide">
                  {seat.seatNumber}
                </span>

                <span className="text-[10px] mt-0.5 font-medium opacity-80">
                  {seat.seatType === 'VIP' ? 'VIP' : 'Thường'}
                </span>

                {/* Status indicator badge */}
                {!isAvailable && (
                  <div className="absolute top-1.5 right-1.5">
                    <Lock className="w-3 h-3 text-slate-400" />
                  </div>
                )}
                {isSelected && (
                  <div className="absolute top-1.5 right-1.5 w-4 h-4 rounded-full bg-white text-emerald-700 flex items-center justify-center">
                    <Check className="w-2.5 h-2.5 stroke-[3]" />
                  </div>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Bottom Summary Bar */}
      <div className="mt-6 pt-4 border-t border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div className="text-center sm:text-left">
          <div className="text-xs text-slate-500 font-medium">Ghế bạn đang chọn:</div>
          <div className="text-base font-extrabold text-slate-900 mt-0.5">
            {selectedSeat ? (
              <span className="text-emerald-700 bg-emerald-100 px-2.5 py-0.5 rounded-lg mr-2 font-black">
                {selectedSeat.seatNumber} (Tầng {selectedSeat.floorNumber})
              </span>
            ) : (
              <span className="text-slate-400 text-sm font-normal italic">
                Chưa có ghế nào được chọn. Hãy nhấp vào 1 ghế trống phía trên.
              </span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <div className="text-[11px] text-slate-500 font-semibold uppercase">Tổng giá vé</div>
            <div className="text-xl font-black text-orange-600">
              {selectedSeat ? `${Number(selectedSeat.price || trip.basePrice).toLocaleString('vi-VN')} đ` : '0 đ'}
            </div>
          </div>

          <button
            type="button"
            disabled={!selectedSeat}
            onClick={() => onProceedBooking(selectedSeat)}
            className={`px-6 py-3 rounded-xl font-bold text-sm flex items-center gap-2 transition shadow-md ${
              selectedSeat
                ? 'bg-gradient-to-r from-orange-600 to-amber-500 hover:from-orange-700 hover:to-amber-600 text-white shadow-orange-500/25 hover:scale-[1.02] active:scale-[0.98]'
                : 'bg-slate-200 text-slate-400 cursor-not-allowed shadow-none'
            }`}
          >
            <span>Giữ Chỗ & Đặt Vé</span>
          </button>
        </div>
      </div>
    </div>
  );
};
