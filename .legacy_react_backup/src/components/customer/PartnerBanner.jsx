import React from 'react';
import { Star, ShieldCheck, Bus, Award, Sparkles, CheckCircle } from 'lucide-react';

const PARTNERS = [
  {
    id: 1,
    name: 'Phương Trang (FUTA Bus Lines)',
    code: 'FUTA',
    logoBg: 'from-orange-500 to-red-600',
    tripsCount: '150+ chuyến/ngày',
    rating: 4.8,
    reviews: '28,450',
    type: 'Giường nằm 36 chỗ, Limousine VIP',
    perks: ['Đúng giờ 99.8%', 'Nước uống & Khăn lạnh', 'Đổi vé linh hoạt'],
    badge: 'Đối tác chiến lược',
  },
  {
    id: 2,
    name: 'Hoàng Long Asia',
    code: 'HOANGLONG',
    logoBg: 'from-red-600 to-rose-700',
    tripsCount: '80+ chuyến/ngày',
    rating: 4.7,
    reviews: '19,200',
    type: 'Xe Bắc Nam cao cấp, Limousine',
    perks: ['Bảo hiểm hành khách', 'Wifi tốc độ cao', 'Cổng sạc Type-C'],
    badge: 'Chất lượng cao',
  },
  {
    id: 3,
    name: 'Thành Bưởi Limousine',
    code: 'THANHBUOI',
    logoBg: 'from-amber-500 to-orange-600',
    tripsCount: '60+ chuyến/ngày',
    rating: 4.9,
    reviews: '34,100',
    type: 'Phòng VIP Cung Điện',
    perks: ['Cabin riêng tư', 'Massage thư giãn', 'Bác tài kinh nghiệm'],
    badge: 'VIP Service',
  },
  {
    id: 4,
    name: 'VIP Luxury Dcar',
    code: 'VIPDCAR',
    logoBg: 'from-blue-600 to-indigo-700',
    tripsCount: '40+ chuyến/ngày',
    rating: 4.9,
    reviews: '12,800',
    type: 'Limousine 9 - 18 chỗ VIP',
    perks: ['Đón tận nơi', 'Ghế da nguyên khối', 'Màn hình riêng'],
    badge: 'Sang trọng 5 sao',
  },
];

export const PartnerBanner = () => {
  return (
    <section className="py-12 sm:py-16">
      <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8 gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-orange-600 mb-1">
            <Sparkles className="w-4 h-4" />
            Mạng lưới vận tải liên kết
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Nhà Xe Đối Tác Uy Tín Hàng Đầu
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Hơn 100+ nhà xe danh tiếng, cam kết khởi hành đúng giờ và giữ nguyên vị trí ghế ngồi đã đặt.
          </p>
        </div>
        <div className="flex items-center gap-2 text-xs font-semibold text-slate-600 bg-slate-100 px-3.5 py-2 rounded-xl self-start sm:self-auto">
          <ShieldCheck className="w-4 h-4 text-emerald-600" />
          <span>Cam kết hoàn tiền 150% nếu không có chỗ</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        {PARTNERS.map((partner) => (
          <div
            key={partner.id}
            className="group bg-white rounded-2xl border border-slate-200/90 hover:border-orange-300 p-5 shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between"
          >
            <div>
              {/* Header Card */}
              <div className="flex items-start justify-between gap-3 mb-4">
                <div
                  className={`w-12 h-12 rounded-xl bg-gradient-to-tr ${partner.logoBg} flex items-center justify-center text-white font-black text-sm shadow-md group-hover:scale-105 transition-transform`}
                >
                  <Bus className="w-6 h-6" />
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-orange-50 text-orange-700 border border-orange-200">
                  {partner.badge}
                </span>
              </div>

              {/* Title & Stats */}
              <h3 className="font-bold text-slate-900 text-base group-hover:text-orange-600 transition">
                {partner.name}
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">{partner.type}</p>

              {/* Ratings */}
              <div className="flex items-center gap-2 mt-2.5">
                <div className="flex items-center gap-1 text-amber-500 font-bold text-xs bg-amber-50 px-2 py-0.5 rounded-md">
                  <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                  <span>{partner.rating}</span>
                </div>
                <span className="text-[11px] text-slate-400">({partner.reviews} đánh giá)</span>
              </div>

              {/* Perks list */}
              <div className="mt-4 pt-3 border-t border-slate-100 space-y-1.5">
                {partner.perks.map((perk, idx) => (
                  <div key={idx} className="flex items-center gap-2 text-xs text-slate-600">
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-500 flex-shrink-0" />
                    <span>{perk}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Bottom info */}
            <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-slate-400 font-medium">{partner.tripsCount}</span>
              <a
                href={`/search?departurePoint=Hà Nội&arrivalPoint=Đà Nẵng`}
                className="font-bold text-orange-600 hover:text-orange-700"
              >
                Xem chuyến →
              </a>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};
