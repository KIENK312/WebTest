import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Bus, Lock, User, Mail, Phone, ArrowRight, ShieldCheck, UserCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [username, setUsername] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [roleName, setRoleName] = useState('ROLE_CUSTOMER'); // ROLE_CUSTOMER or ROLE_OPERATOR
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        username: username.trim(),
        fullName: fullName.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim(),
        password,
        roleName,
        operatorId: roleName === 'ROLE_OPERATOR' ? 1 : null,
      };

      await register(payload);
      navigate('/', { replace: true });
    } catch (err) {
      console.error('Đăng ký thất bại:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-6">
        <div className="text-center">
          <div className="w-14 h-14 rounded-3xl bg-gradient-to-tr from-orange-600 to-amber-500 text-white flex items-center justify-center mx-auto shadow-lg shadow-orange-500/25 mb-4">
            <Bus className="w-8 h-8 transform -rotate-3" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Đăng Ký Tài Khoản
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Gia nhập mạng lưới bán vé và trải nghiệm dịch vụ xe khách thông minh
          </p>
        </div>

        <div className="bg-white rounded-3xl border border-slate-200/90 shadow-xl p-6 sm:p-8 space-y-5">
          <form onSubmit={handleSubmit} className="space-y-3.5">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Tên đăng nhập (Username) <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="VD: nguyenvana"
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Họ và tên đầy đủ <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="VD: Nguyễn Văn An"
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Email <span className="text-rose-500">*</span>
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="an@gmail.com"
                  className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Số điện thoại <span className="text-rose-500">*</span>
                </label>
                <input
                  type="tel"
                  required
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="0912345678"
                  className="w-full h-11 px-3.5 rounded-xl border border-slate-200 bg-slate-50 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:bg-white transition font-mono"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Mật khẩu bảo mật <span className="text-rose-500">*</span>
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

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                Loại tài khoản
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setRoleName('ROLE_CUSTOMER')}
                  className={`py-2 px-3 rounded-xl text-xs font-bold border transition ${
                    roleName === 'ROLE_CUSTOMER'
                      ? 'bg-orange-600 text-white border-orange-600 shadow-xs'
                      : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
                  }`}
                >
                  👤 Khách Hàng
                </button>
                <button
                  type="button"
                  onClick={() => setRoleName('ROLE_OPERATOR')}
                  className={`py-2 px-3 rounded-xl text-xs font-bold border transition ${
                    roleName === 'ROLE_OPERATOR'
                      ? 'bg-blue-600 text-white border-blue-600 shadow-xs'
                      : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100'
                  }`}
                >
                  🚌 Đối Tác Nhà Xe
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full h-11 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-md shadow-orange-600/30 transition hover:scale-[1.01] active:scale-[0.99] disabled:opacity-50 mt-2"
            >
              <span>{loading ? 'Đang Đăng Ký...' : 'Đăng Ký Tài Khoản'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="pt-2 border-t border-slate-100 text-center text-xs text-slate-500">
            Đã có tài khoản?{' '}
            <Link to="/login" className="font-bold text-orange-600 hover:text-orange-700">
              Đăng nhập ngay
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
