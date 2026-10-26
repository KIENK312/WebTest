import React from 'react';
import { Bus, Phone, Mail, MapPin, ShieldCheck, Clock, Award } from 'lucide-react';

export const Footer = () => {
  return (
    <footer className="bg-slate-900 text-slate-300 pt-16 pb-10 border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-10 pb-12 border-b border-slate-800">
          {/* Brand Col */}
          <div className="lg:col-span-2 space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-orange-600 to-amber-500 flex items-center justify-center text-white shadow-lg shadow-orange-500/20">
                <Bus className="w-5 h-5 transform -rotate-3" />
              </div>
              <div>
                <span className="font-extrabold text-xl tracking-tight text-white">BusTicket Pro</span>
                <span className="ml-2 text-[10px] px-2 py-0.5 rounded-full bg-orange-950 text-orange-400 font-bold border border-orange-800">
                  PLATFORM
                </span>
              </div>
            </div>
            <p className="text-sm text-slate-400 leading-relaxed max-w-sm">
              Nền tảng công nghệ quản lý và bán vé xe khách đa đối tác hàng đầu Việt Nam. Kết nối trực tiếp hệ thống nhà xe uy tín với hành khách, minh bạch giá vé và tự động giữ chỗ theo thời gian thực.
            </p>
            <div className="flex items-center gap-4 text-xs text-slate-400 pt-2">
              <div className="flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Bảo mật 100%</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Clock className="w-4 h-4 text-amber-400" />
                <span>Hỗ trợ 24/7</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Award className="w-4 h-4 text-orange-400" />
                <span>Đối tác chính hãng</span>
              </div>
            </div>
          </div>

          {/* Quick Links */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Tuyến đường Hot</h4>
            <ul className="space-y-2 text-xs">
              <li><a href="/search?departure=Sài Gòn&arrival=Đà Lạt" className="hover:text-orange-400 transition">Sài Gòn → Đà Lạt</a></li>
              <li><a href="/search?departure=Hà Nội&arrival=Đà Nẵng" className="hover:text-orange-400 transition">Hà Nội → Đà Nẵng</a></li>
              <li><a href="/search?departure=Hà Nội&arrival=Sa Pa" className="hover:text-orange-400 transition">Hà Nội → Sa Pa</a></li>
              <li><a href="/search?departure=Sài Gòn&arrival=Nha Trang" className="hover:text-orange-400 transition">Sài Gòn → Nha Trang</a></li>
              <li><a href="/search?departure=Sài Gòn&arrival=Cần Thơ" className="hover:text-orange-400 transition">Sài Gòn → Cần Thơ</a></li>
            </ul>
          </div>

          {/* Đối tác */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Nhà xe đối tác</h4>
            <ul className="space-y-2 text-xs">
              <li className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-orange-500"></span>
                <span>FUTA Bus Lines (Phương Trang)</span>
              </li>
              <li className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-red-500"></span>
                <span>Hoàng Long Asia</span>
              </li>
              <li className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-amber-500"></span>
                <span>Thành Bưởi Limousine</span>
              </li>
              <li className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-blue-500"></span>
                <span>VIP Luxury Dcar</span>
              </li>
            </ul>
          </div>

          {/* Contact */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Tổng đài hỗ trợ</h4>
            <div className="space-y-2.5 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-orange-400" />
                <span className="text-white font-bold text-sm">1900 6067</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-slate-400" />
                <span>hotro@busticketpro.vn</span>
              </div>
              <div className="flex items-start gap-2">
                <MapPin className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
                <span>Tòa nhà Innovation, Khu Công nghệ cao, TP. Hồ Chí Minh</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
          <p>© 2026 BusTicket Pro - Multi-partner Bus Reservation System. All rights reserved.</p>
          <div className="flex items-center gap-3">
            <span className="px-2 py-1 rounded bg-slate-800 text-[11px] font-semibold text-slate-300">VietQR</span>
            <span className="px-2 py-1 rounded bg-slate-800 text-[11px] font-semibold text-slate-300">Napas 247</span>
            <span className="px-2 py-1 rounded bg-slate-800 text-[11px] font-semibold text-slate-300">VNPay</span>
            <span className="px-2 py-1 rounded bg-slate-800 text-[11px] font-semibold text-slate-300">MoMo</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
