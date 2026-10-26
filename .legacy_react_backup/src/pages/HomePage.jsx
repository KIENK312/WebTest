import React from 'react';
import { SearchWidget } from '../components/customer/SearchWidget';
import { PartnerBanner } from '../components/customer/PartnerBanner';
import { ShieldCheck, Clock, Zap, Award, Sparkles, ArrowRight, Bus, CheckCircle2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const POPULAR_DESTINATIONS = [
  {
    from: 'Hà Nội',
    to: 'Đà Nẵng',
    price: '450.000 đ',
    duration: '14 giờ',
    operator: 'Phương Trang & Hoàng Long',
    image: 'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=600&q=80',
    tag: 'Tuyến Phổ Biến Nhất',
  },
  {
    from: 'Sài Gòn',
    to: 'Đà Lạt',
    price: '320.000 đ',
    duration: '8 giờ',
    operator: 'FUTA Bus Lines',
    image: 'https://images.unsplash.com/photo-1583417319070-4a69db38a482?auto=format&fit=crop&w=600&q=80',
    tag: 'Hot Nhất Mùa Du Lịch',
  },
  {
    from: 'Hà Nội',
    to: 'Sa Pa',
    price: '280.000 đ',
    duration: '6 giờ',
    operator: 'Hoàng Long Asia VIP',
    image: 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=600&q=80',
    tag: 'Xe Limousine Cabin VIP',
  },
  {
    from: 'Sài Gòn',
    to: 'Nha Trang',
    price: '350.000 đ',
    duration: '9 giờ',
    operator: 'Phương Trang FUTA',
    image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80',
    tag: 'Biển Đẹp Vẫy Gọi',
  },
];

export const HomePage = () => {
  const navigate = useNavigate();

  return (
    <div className="space-y-16 sm:space-y-24 pb-20">
      {/* Hero Section */}
      <section className="relative pt-10 sm:pt-16 pb-20 lg:pb-32 overflow-hidden">
        {/* Background Decorative Blobs */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-96 bg-gradient-to-b from-orange-100/60 via-amber-50/40 to-transparent rounded-b-3xl -z-10 blur-2xl"></div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          {/* Top Pill */}
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-orange-100/80 border border-orange-200 text-orange-800 text-xs font-bold mb-6 shadow-xs animate-in fade-in duration-300">
            <Sparkles className="w-4 h-4 text-orange-600" />
            <span>Nền tảng Quản lý & Đặt vé xe khách đa đối tác thế hệ mới</span>
          </div>

          {/* Heading */}
          <h1 className="text-3xl sm:text-5xl lg:text-6xl font-black text-slate-900 tracking-tight leading-[1.15] max-w-4xl mx-auto">
            Hành Trình Vạn Dặm,{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-600 to-amber-500">
              Chắc Chắn Có Chỗ
            </span>
          </h1>

          <p className="mt-4 text-sm sm:text-base text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Kết nối hơn 100+ nhà xe uy tín toàn quốc. Giữ chỗ tức thì với công nghệ Optimistic Locking, thanh toán tự động VietQR và giám sát an toàn kỹ thuật 48 giờ.
          </p>

          {/* Search Box Widget */}
          <div className="mt-10 max-w-5xl mx-auto">
            <SearchWidget />
          </div>

          {/* Key Quick Stats */}
          <div className="mt-12 grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-4xl mx-auto text-left">
            <div className="p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-orange-100 text-orange-600 flex items-center justify-center font-bold">
                <Bus className="w-5 h-5" />
              </div>
              <div>
                <div className="font-extrabold text-slate-900 text-base">100+</div>
                <div className="text-[11px] text-slate-500 font-medium">Nhà xe đối tác</div>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center font-bold">
                <Zap className="w-5 h-5" />
              </div>
              <div>
                <div className="font-extrabold text-slate-900 text-base">3 Giây</div>
                <div className="text-[11px] text-slate-500 font-medium">Đối soát VietQR</div>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <div className="font-extrabold text-slate-900 text-base">48 Giờ</div>
                <div className="text-[11px] text-slate-500 font-medium">Đệm an toàn bảo trì</div>
              </div>
            </div>

            <div className="p-4 rounded-2xl bg-white border border-slate-200/80 shadow-xs flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-600 flex items-center justify-center font-bold">
                <Award className="w-5 h-5" />
              </div>
              <div>
                <div className="font-extrabold text-slate-900 text-base">150%</div>
                <div className="text-[11px] text-slate-500 font-medium">Bồi thường uy tín</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Popular Routes Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8 gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-orange-600 mb-1">
              <Award className="w-4 h-4" />
              Tuyến đường được đặt nhiều nhất
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Tuyến Xe Khách Trọng Điểm
            </h2>
          </div>
          <button
            type="button"
            onClick={() => navigate('/search')}
            className="flex items-center gap-1.5 text-xs font-bold text-orange-600 hover:text-orange-700 transition"
          >
            <span>Xem tất cả chuyến xe</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {POPULAR_DESTINATIONS.map((dest, idx) => (
            <div
              key={idx}
              onClick={() =>
                navigate(
                  `/search?departurePoint=${encodeURIComponent(dest.from)}&arrivalPoint=${encodeURIComponent(
                    dest.to
                  )}`
                )
              }
              className="group bg-white rounded-3xl border border-slate-200/90 hover:border-orange-300 shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer flex flex-col justify-between"
            >
              <div>
                {/* Image banner */}
                <div className="h-44 w-full relative overflow-hidden bg-slate-100">
                  <img
                    src={dest.image}
                    alt={`${dest.from} đi ${dest.to}`}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent"></div>
                  <span className="absolute top-3 left-3 px-2.5 py-1 rounded-lg bg-orange-600 text-white font-bold text-[10px] uppercase tracking-wider shadow-sm">
                    {dest.tag}
                  </span>
                  <div className="absolute bottom-3 left-3 text-white">
                    <div className="text-lg font-black leading-tight">
                      {dest.from} → {dest.to}
                    </div>
                    <div className="text-[11px] text-slate-300 flex items-center gap-1 mt-0.5">
                      <Clock className="w-3 h-3" />
                      <span>{dest.duration}</span>
                    </div>
                  </div>
                </div>

                {/* Content */}
                <div className="p-4 space-y-2">
                  <div className="text-xs text-slate-500 font-medium">Nhà xe vận hành:</div>
                  <div className="text-xs font-bold text-slate-800">{dest.operator}</div>
                </div>
              </div>

              {/* Price & Action */}
              <div className="p-4 pt-0 border-t border-slate-100 mt-2 flex items-center justify-between">
                <div>
                  <div className="text-[10px] text-slate-400 font-medium uppercase">Từ</div>
                  <div className="text-base font-black text-orange-600">{dest.price}</div>
                </div>
                <span className="px-3 py-1.5 rounded-xl bg-orange-50 group-hover:bg-orange-600 group-hover:text-white text-orange-600 text-xs font-bold transition">
                  Đặt Vé
                </span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Partner Banner Component */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <PartnerBanner />
      </div>

      {/* Feature Highlights Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 rounded-3xl p-8 sm:p-12 text-white relative overflow-hidden shadow-2xl">
          <div className="relative z-10 max-w-2xl">
            <span className="text-xs font-bold text-orange-400 uppercase tracking-widest">
              NỀN TẢNG CÔNG NGHỆ VẬN TẢI
            </span>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight mt-2">
              Tại Sao Nên Đặt Vé Trên BusTicket Pro?
            </h2>
            <p className="text-sm text-slate-300 mt-3 leading-relaxed">
              Hệ thống được phát triển theo tiêu chuẩn doanh nghiệp backend Java 17 Spring Boot 3 và kiến trúc Frontend React hiện đại.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-8">
              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-sm text-white">Chống Trùng Ghế 100%</h4>
                  <p className="text-xs text-slate-400 mt-0.5">Optimistic Locking ngăn chặn xung đột giữ chỗ cùng một lúc.</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-sm text-white">Thanh Toán VietQR Tức Thì</h4>
                  <p className="text-xs text-slate-400 mt-0.5">Webhook đối soát tự động theo mã <code>VE&#123;id&#125;</code> trong 3 giây.</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-sm text-white">Kiểm Tra Đệm Bảo Trì 48h</h4>
                  <p className="text-xs text-slate-400 mt-0.5">Tuyệt đối không xếp xe đang bảo dưỡng hoặc xung đột lịch kỹ thuật.</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-sm text-white">Vé Điện Tử Mã Hóa QR</h4>
                  <p className="text-xs text-slate-400 mt-0.5">Quét mã trực tiếp khi lên xe, đồng bộ tức thì với danh sách hành khách.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
