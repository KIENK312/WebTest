import React from 'react';
import { Link } from 'react-router-dom';
import { Bus, Home } from 'lucide-react';

export const NotFoundPage = () => {
  return (
    <div className="min-h-[70vh] flex flex-col items-center justify-center text-center px-4">
      <div className="w-16 h-16 rounded-3xl bg-orange-100 text-orange-600 flex items-center justify-center mb-4">
        <Bus className="w-8 h-8" />
      </div>
      <h1 className="text-4xl font-black text-slate-900 tracking-tight">404</h1>
      <h2 className="text-lg font-bold text-slate-700 mt-1">Trang Không Tồn Tại</h2>
      <p className="text-xs text-slate-500 max-w-sm mt-1">
        Đường dẫn bạn yêu cầu không khả dụng hoặc đã được di chuyển sang địa chỉ khác.
      </p>
      <Link
        to="/"
        className="mt-6 px-5 py-2.5 rounded-xl bg-orange-600 text-white font-bold text-xs flex items-center gap-2 hover:bg-orange-700 transition shadow-sm"
      >
        <Home className="w-4 h-4" />
        <span>Về Trang Chủ</span>
      </Link>
    </div>
  );
};
