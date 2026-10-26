import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Bus, Lock, User, ArrowRight, Sparkles, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectPath = searchParams.get('redirect') || '/';
  const toast = useToast();

  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(usernameOrEmail.trim(), password);
      navigate(redirectPath, { replace: true });
    } catch (err) {
      console.error('Đăng nhập thất bại:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (u, p) => {
    setUsernameOrEmail(u);
    setPassword(p);
    setLoading(true);
    try {
      await login(u, p);
      navigate(redirectPath, { replace: true });
    } catch (err) {
      console.error('Đăng nhập nhanh thất bại:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-6">
        {/* Logo header */}
        <div className="text-center">
          <div className="w-14 h-14 rounded-3xl bg-gradient-to-tr from-orange-600 to-amber-500 text-white flex items-center justify-center mx-auto shadow-lg shadow-orange-500/25 mb-4">
            <Bus className="w-8 h-8 transform -rotate-3" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Đăng Nhập Hệ Thống
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Hệ thống Quản lý Bán vé Xe khách Đa đối tác
          </p>
        </div>

        {/* 1-Click Quick Demo Box */}
        <div className="bg-amber-50/80 border border-amber-200 rounded-3xl p-4 sm:p-5 shadow-xs">
          <div className="flex items-center gap-1.5 text-xs font-bold text-amber-900 mb-2">
            <Sparkles className="w-4 h-4 text-amber-600" />
            <span>Đăng nhập nhanh 1-Click (Demo Tài Khoản)</span>
          </div>
          <p className="text-[11px] text-amber-800 mb-3">
            Nhấp trực tiếp vào vai trò bên dưới để kiểm tra phân quyền tức thì:
          </p>

          <div className="grid grid-cols-3 gap-2">
            <button
              type="button"
              disabled={loading}
              onClick={() => handleQuickLogin('customer', 'Customer@123')}
              className="py-2 px-1 rounded-xl bg-white border border-emerald-300 hover:bg-emerald-50 text-emerald-800 text-[11px] font-bold shadow-2xs transition text-center"
            >
              👤 Khách Hàng
            </button>
            <button
              type="button"
              disabled={loading}
              onClick={() => handleQuickLogin('operator_futa', 'Operator@123')}
              className="py-2 px-1 rounded-xl bg-white border border-blue-300 hover:bg-blue-50 text-blue-800 text-[11px] font-bold shadow-2xs transition text-center"
            >
              🚌 Nhà Xe FUTA
            </button>
            <button
              type="button"
              disabled={loading}
              onClick={() => handleQuickLogin('admin', 'Admin@123')}
              className="py-2 px-1 rounded-xl bg-white border border-purple-300 hover:bg-purple-50 text-purple-800 text-[11px] font-bold shadow-2xs transition text-center"
            >
              👑 Quản Trị Viên
            </button>
          </div>
        </div>

        {/* Main Login Form */}
        <div className="bg-white rounded-3xl border border-slate-200/90 shadow-xl p-6 sm:p-8 space-y-5">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1.5">
                <User className="w-3.5 h-3.5 text-slate-400" />
                Tên đăng nhập hoặc Email
              </label>
              <input
                type="text"
                required
                value={usernameOrEmail}
                onChange={(e) => setUsernameOrEmail(e.target.value)}
                placeholder="VD: customer hoặc admin@busbooking.vn"
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1 flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5 text-slate-400" />
                Mật khẩu
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full h-11 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-md shadow-orange-600/30 transition hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50"
            >
              <span>{loading ? 'Đang Xác Thực...' : 'Đăng Nhập'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="pt-2 border-t border-slate-100 text-center text-xs text-slate-500">
            Chưa có tài khoản?{' '}
            <Link to="/register" className="font-bold text-orange-600 hover:text-orange-700">
              Đăng ký ngay
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
