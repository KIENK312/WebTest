import React, { useState } from 'react';
import { Users, Plus, CheckCircle2, ShieldAlert, Phone, Mail, MapPin, Power, X } from 'lucide-react';
import { adminApi } from '../../api/adminApi';
import { useToast } from '../../context/ToastContext';

export const PartnerManagement = ({ partners = [], onReload }) => {
  const [updatingId, setUpdatingId] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const toast = useToast();

  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [address, setAddress] = useState('');
  const [adding, setAdding] = useState(false);

  const handleToggleStatus = async (partner) => {
    const nextStatus = partner.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    setUpdatingId(partner.id);
    try {
      await adminApi.updatePartnerStatus(partner.id, nextStatus);
      toast.success(
        `Đã chuyển trạng thái nhà xe ${partner.name} sang ${nextStatus}!`,
        'Cập Nhật Đối Tác'
      );
      if (onReload) onReload();
    } catch (err) {
      console.error('Lỗi cập nhật đối tác:', err);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCreatePartner = async (e) => {
    e.preventDefault();
    setAdding(true);
    try {
      const payload = {
        name: name.trim(),
        code: code.trim().toUpperCase(),
        contactPhone: contactPhone.trim(),
        contactEmail: contactEmail.trim(),
        address: address.trim(),
        status: 'ACTIVE',
      };

      await adminApi.createPartner(payload);
      toast.success(`Đã thêm nhà xe đối tác ${name} thành công!`, 'Thêm Đối Tác');
      setShowAddModal(false);
      setName('');
      setCode('');
      setContactPhone('');
      setContactEmail('');
      setAddress('');
      if (onReload) onReload();
    } catch (err) {
      console.error('Lỗi tạo đối tác:', err);
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="bg-white rounded-3xl border border-slate-200/90 shadow-sm p-6 space-y-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-100 gap-3">
        <div>
          <h3 className="font-extrabold text-base text-slate-900 flex items-center gap-2">
            <Users className="w-5 h-5 text-purple-600" />
            Quản Lý Đối Tác Vận Tải & Phê Duyệt Nhà Xe
          </h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Duyệt nhà xe mới tham gia nền tảng, thiết lập hợp đồng và phân quyền điều hành.
          </p>
        </div>
        <button
          type="button"
          onClick={() => setShowAddModal(true)}
          className="px-4 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs flex items-center gap-2 shadow-md shadow-purple-600/20 transition self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Thêm Nhà Xe Mới</span>
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[10px]">
              <th className="pb-3 pl-2">Tên Nhà Xe / Doanh Nghiệp</th>
              <th className="pb-3">Mã Đối Tác</th>
              <th className="pb-3">Hotline</th>
              <th className="pb-3">Email</th>
              <th className="pb-3">Trạng Thái</th>
              <th className="pb-3 pr-2 text-right">Phê Duyệt / Khóa</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
            {partners.map((partner) => {
              const isActive = partner.status === 'ACTIVE';
              const isUpdating = updatingId === partner.id;

              return (
                <tr key={partner.id} className="hover:bg-slate-50/70 transition">
                  <td className="py-3.5 pl-2 font-bold text-slate-900 text-sm">
                    {partner.name}
                  </td>
                  <td className="py-3.5 font-mono font-bold text-purple-700">
                    {partner.code}
                  </td>
                  <td className="py-3.5 font-mono">{partner.contactPhone}</td>
                  <td className="py-3.5 text-slate-500">{partner.contactEmail || 'Chưa cập nhật'}</td>
                  <td className="py-3.5">
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold ${
                        isActive
                          ? 'bg-emerald-100 text-emerald-800 border border-emerald-200'
                          : 'bg-rose-100 text-rose-800 border border-rose-200'
                      }`}
                    >
                      <span className={`w-2 h-2 rounded-full ${isActive ? 'bg-emerald-500' : 'bg-rose-500'}`} />
                      <span>{isActive ? 'Đã Phê Duyệt' : 'Tạm Khóa'}</span>
                    </span>
                  </td>
                  <td className="py-3.5 pr-2 text-right">
                    <button
                      type="button"
                      disabled={isUpdating}
                      onClick={() => handleToggleStatus(partner)}
                      className={`px-3 py-1.5 rounded-xl font-bold text-xs inline-flex items-center gap-1.5 transition ${
                        isActive
                          ? 'bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200'
                          : 'bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200'
                      }`}
                    >
                      <Power className="w-3.5 h-3.5" />
                      <span>{isActive ? 'Tạm Khóa' : 'Kích Hoạt'}</span>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Modal Add Partner */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between px-6 py-5 border-b border-slate-100 bg-purple-50">
              <h3 className="font-extrabold text-lg text-slate-900">Thêm Nhà Xe Đối Tác Mới</h3>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="w-8 h-8 rounded-full bg-white text-slate-400 hover:text-slate-700 flex items-center justify-center border border-slate-200"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreatePartner} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Tên doanh nghiệp / Nhà xe <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ví dụ: Công ty TNHH Vận tải Thành Bưởi"
                  className="w-full h-11 px-3 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-purple-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Mã đối tác (Code) <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    placeholder="VD: THANHBUOI"
                    className="w-full h-11 px-3 rounded-xl border border-slate-200 text-xs font-mono font-bold uppercase focus:ring-2 focus:ring-purple-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Số điện thoại tổng đài <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="tel"
                    required
                    value={contactPhone}
                    onChange={(e) => setContactPhone(e.target.value)}
                    placeholder="VD: 19006079"
                    className="w-full h-11 px-3 rounded-xl border border-slate-200 text-xs font-mono focus:ring-2 focus:ring-purple-500 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Email liên hệ
                </label>
                <input
                  type="email"
                  value={contactEmail}
                  onChange={(e) => setContactEmail(e.target.value)}
                  placeholder="VD: lienhe@thanhbuoi.com.vn"
                  className="w-full h-11 px-3 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-purple-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Địa chỉ trụ sở chính
                </label>
                <input
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="VD: 266 Lê Hồng Phong, Phường 4, Quận 5, TP.HCM"
                  className="w-full h-11 px-3 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-purple-500 focus:outline-none"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2.5 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={adding}
                  className="px-5 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold transition disabled:opacity-50"
                >
                  {adding ? 'Đang Lưu...' : 'Thêm Nhà Xe'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
