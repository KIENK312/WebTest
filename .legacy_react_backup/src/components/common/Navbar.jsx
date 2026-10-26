import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Bus, User, LogOut, Ticket, Shield, Wrench, Menu, X, ChevronDown, Sparkles } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Navbar = () => {
  const { user, isAuthenticated, isAdmin, isOperator, logout, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const [demoLoginOpen, setDemoLoginOpen] = useState(false);

  const isActive = (path) => location.pathname === path;

  const handleQuickLogin = async (username, password) => {
    try {
      await login(username, password);
      setDemoLoginOpen(false);
    } catch (err) {
      // toast will handle
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200/80 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 sm:h-20">
          {/* Logo Brand */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-orange-600 to-amber-500 flex items-center justify-center text-white shadow-md shadow-orange-500/25 group-hover:scale-105 transition-transform duration-200">
              <Bus className="w-6 h-6 transform -rotate-3" />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-extrabold text-xl tracking-tight text-slate-900">BusTicket</span>
                <span className="text-xs px-2 py-0.5 rounded-full bg-orange-100 text-orange-700 font-bold uppercase tracking-wider">
                  Pro
                </span>
              </div>
              <p className="text-[11px] text-slate-500 font-medium">Hệ Thống Đặt Vé Đa Đối Tác</p>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-1">
            <Link
              to="/"
              className={`px-4 py-2 rounded-xl text-sm font-semibold transition ${
                isActive('/')
                  ? 'text-orange-600 bg-orange-50'
                  : 'text-slate-700 hover:text-orange-600 hover:bg-slate-100/70'
              }`}
            >
              Trang Chủ
            </Link>

            <Link
              to="/search"
              className={`px-4 py-2 rounded-xl text-sm font-semibold transition ${
                isActive('/search')
                  ? 'text-orange-600 bg-orange-50'
                  : 'text-slate-700 hover:text-orange-600 hover:bg-slate-100/70'
              }`}
            >
              Tìm Chuyến Xe
            </Link>

            <Link
              to="/my-tickets"
              className={`px-4 py-2 rounded-xl text-sm font-semibold transition flex items-center gap-1.5 ${
                isActive('/my-tickets')
                  ? 'text-orange-600 bg-orange-50'
                  : 'text-slate-700 hover:text-orange-600 hover:bg-slate-100/70'
              }`}
            >
              <Ticket className="w-4 h-4" />
              Lịch Sử Vé
            </Link>

            {isOperator && (
              <Link
                to="/operator"
                className={`px-4 py-2 rounded-xl text-sm font-semibold transition flex items-center gap-1.5 ${
                  isActive('/operator')
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-slate-700 hover:text-blue-600 hover:bg-slate-100/70'
                }`}
              >
                <Wrench className="w-4 h-4 text-blue-500" />
                Quản Lý Nhà Xe
              </Link>
            )}

            {isAdmin && (
              <Link
                to="/admin"
                className={`px-4 py-2 rounded-xl text-sm font-semibold transition flex items-center gap-1.5 ${
                  isActive('/admin')
                    ? 'text-purple-600 bg-purple-50'
                    : 'text-slate-700 hover:text-purple-600 hover:bg-slate-100/70'
                }`}
              >
                <Shield className="w-4 h-4 text-purple-500" />
                Quản Trị Admin
              </Link>
            )}
          </nav>

          {/* Right Action / Auth Area */}
          <div className="hidden md:flex items-center gap-3">
            {/* Quick Demo Switcher Button */}
            <div className="relative">
              <button
                onClick={() => setDemoLoginOpen(!demoLoginOpen)}
                className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg bg-slate-100 text-slate-700 hover:bg-slate-200 transition border border-slate-200"
                title="Chọn nhanh tài khoản Demo để kiểm tra phân quyền"
              >
                <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                <span>Demo 1-Click</span>
                <ChevronDown className="w-3 h-3 text-slate-500" />
              </button>

              {demoLoginOpen && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-slate-200 p-2 z-50">
                  <div className="px-3 py-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                    Đăng nhập mẫu nhanh
                  </div>
                  <button
                    onClick={() => handleQuickLogin('customer', 'Customer@123')}
                    className="w-full text-left px-3 py-2 text-xs rounded-xl hover:bg-emerald-50 text-slate-700 hover:text-emerald-700 flex items-center justify-between"
                  >
                    <div>
                      <div className="font-semibold">Khách Hàng</div>
                      <div className="text-[10px] text-slate-400">customer / Customer@123</div>
                    </div>
                    <span className="px-1.5 py-0.5 rounded text-[10px] bg-emerald-100 text-emerald-700 font-bold">
                      Customer
                    </span>
                  </button>
                  <button
                    onClick={() => handleQuickLogin('operator_futa', 'Operator@123')}
                    className="w-full text-left px-3 py-2 text-xs rounded-xl hover:bg-blue-50 text-slate-700 hover:text-blue-700 flex items-center justify-between mt-1"
                  >
                    <div>
                      <div className="font-semibold">Nhà Xe Phương Trang</div>
                      <div className="text-[10px] text-slate-400">operator_futa / Operator@123</div>
                    </div>
                    <span className="px-1.5 py-0.5 rounded text-[10px] bg-blue-100 text-blue-700 font-bold">
                      FUTA
                    </span>
                  </button>
                  <button
                    onClick={() => handleQuickLogin('admin', 'Admin@123')}
                    className="w-full text-left px-3 py-2 text-xs rounded-xl hover:bg-purple-50 text-slate-700 hover:text-purple-700 flex items-center justify-between mt-1"
                  >
                    <div>
                      <div className="font-semibold">Quản Trị Viên</div>
                      <div className="text-[10px] text-slate-400">admin / Admin@123</div>
                    </div>
                    <span className="px-1.5 py-0.5 rounded text-[10px] bg-purple-100 text-purple-700 font-bold">
                      Admin
                    </span>
                  </button>
                </div>
              )}
            </div>

            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                  className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-slate-50 border border-slate-200/90 hover:bg-slate-100 transition"
                >
                  <div className="w-8 h-8 rounded-lg bg-orange-600 text-white font-bold flex items-center justify-center text-xs shadow-xs">
                    {user?.fullName?.charAt(0) || 'U'}
                  </div>
                  <div className="text-left hidden lg:block">
                    <div className="text-xs font-bold text-slate-900 leading-tight">
                      {user?.fullName}
                    </div>
                    <div className="text-[10px] font-semibold text-slate-500">
                      {isAdmin ? 'Quản trị viên' : isOperator ? 'Nhà xe' : 'Hành khách'}
                    </div>
                  </div>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {userDropdownOpen && (
                  <div className="absolute right-0 mt-2 w-52 bg-white rounded-2xl shadow-xl border border-slate-200 p-2 z-50">
                    <div className="px-3 py-2 border-b border-slate-100 mb-1">
                      <p className="text-xs font-bold text-slate-900">{user?.fullName}</p>
                      <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
                    </div>

                    <Link
                      to="/my-tickets"
                      onClick={() => setUserDropdownOpen(false)}
                      className="flex items-center gap-2 px-3 py-2 text-xs rounded-xl text-slate-700 hover:bg-slate-50 font-medium"
                    >
                      <Ticket className="w-4 h-4 text-slate-400" />
                      Vé Của Tôi
                    </Link>

                    {isOperator && (
                      <Link
                        to="/operator"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-xs rounded-xl text-slate-700 hover:bg-slate-50 font-medium"
                      >
                        <Wrench className="w-4 h-4 text-blue-500" />
                        Quản Lý Nhà Xe
                      </Link>
                    )}

                    {isAdmin && (
                      <Link
                        to="/admin"
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-xs rounded-xl text-slate-700 hover:bg-slate-50 font-medium"
                      >
                        <Shield className="w-4 h-4 text-purple-500" />
                        Quản Trị Hệ Thống
                      </Link>
                    )}

                    <button
                      onClick={() => {
                        logout();
                        setUserDropdownOpen(false);
                      }}
                      className="w-full flex items-center gap-2 px-3 py-2 text-xs rounded-xl text-rose-600 hover:bg-rose-50 font-medium mt-1"
                    >
                      <LogOut className="w-4 h-4" />
                      Đăng Xuất
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-4 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
                >
                  Đăng Nhập
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 rounded-xl text-xs font-bold bg-orange-600 text-white hover:bg-orange-700 transition shadow-sm shadow-orange-600/30"
                >
                  Đăng Ký
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Hamburger Button */}
          <div className="flex md:hidden items-center gap-2">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 bg-white px-4 pt-3 pb-6 space-y-2">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-xl text-sm font-semibold text-slate-800 hover:bg-slate-100"
          >
            Trang Chủ
          </Link>
          <Link
            to="/search"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-xl text-sm font-semibold text-slate-800 hover:bg-slate-100"
          >
            Tìm Chuyến Xe
          </Link>
          <Link
            to="/my-tickets"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-xl text-sm font-semibold text-slate-800 hover:bg-slate-100"
          >
            Lịch Sử Vé
          </Link>

          {isOperator && (
            <Link
              to="/operator"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-xl text-sm font-semibold text-blue-600 hover:bg-blue-50"
            >
              Quản Lý Nhà Xe
            </Link>
          )}

          {isAdmin && (
            <Link
              to="/admin"
              onClick={() => setMobileMenuOpen(false)}
              className="block px-3 py-2 rounded-xl text-sm font-semibold text-purple-600 hover:bg-purple-50"
            >
              Quản Trị Admin
            </Link>
          )}

          <div className="border-t border-slate-100 pt-3">
            {isAuthenticated ? (
              <div className="space-y-2">
                <div className="px-3 py-1 text-xs text-slate-500 font-medium">
                  Đang đăng nhập: <strong className="text-slate-800">{user?.fullName}</strong>
                </div>
                <button
                  onClick={() => {
                    logout();
                    setMobileMenuOpen(false);
                  }}
                  className="w-full text-left px-3 py-2 rounded-xl text-sm font-semibold text-rose-600 hover:bg-rose-50"
                >
                  Đăng Xuất
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-2 pt-1">
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-center py-2 text-xs font-bold rounded-xl border border-slate-300 text-slate-700"
                >
                  Đăng Nhập
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMobileMenuOpen(false)}
                  className="text-center py-2 text-xs font-bold rounded-xl bg-orange-600 text-white"
                >
                  Đăng Ký
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
};
