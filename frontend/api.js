/**
 * api.js – Module JavaScript dùng chung cho Hệ thống Quản lý Bán vé Xe khách
 * Tích hợp SweetAlert2, FontAwesome, Fetch API chuẩn RESTful kết nối http://localhost:8080/api/v1
 */

const API_BASE = (window.location.protocol.startsWith('http'))
  ? `${window.location.origin}/api/v1`
  : 'http://localhost:8080/api/v1';

// ============================================================
// 1. SWEETALERT2 & TOAST NOTIFICATION HELPERS
// ============================================================

/**
 * Hiển thị Toast thông báo nhanh góc trên bên phải
 */
function showToast(message, type = 'info', duration = 3500) {
  if (typeof Swal !== 'undefined') {
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: duration,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.onmouseenter = Swal.stopTimer;
        toast.onmouseleave = Swal.resumeTimer;
      }
    });
    Toast.fire({
      icon: type === 'warning' ? 'warning' : type === 'error' ? 'error' : type === 'success' ? 'success' : 'info',
      title: message
    });
  } else {
    // Fallback nếu chưa tải xong SweetAlert2
    alert(`${type.toUpperCase()}: ${message}`);
  }
}

/**
 * Popup Modal thông báo thành công
 */
function showSuccessModal(title, text, callback) {
  if (typeof Swal !== 'undefined') {
    Swal.fire({
      icon: 'success',
      title: title || 'Thành công!',
      text: text || '',
      confirmButtonColor: '#2563eb',
      confirmButtonText: 'Đồng ý'
    }).then(() => {
      if (typeof callback === 'function') callback();
    });
  } else {
    alert(`${title}\n${text}`);
    if (typeof callback === 'function') callback();
  }
}

/**
 * Tiện ích bọc nút bấm với Loading Spinner chống click spam / double submit
 */
async function withButtonLoading(btn, asyncFn, loadingText = 'Đang xử lý...') {
  if (!btn) return asyncFn();
  if (btn.disabled) return;
  
  const originalHtml = btn.innerHTML;
  btn.disabled = true;
  btn.classList.add('opacity-75', 'cursor-not-allowed');
  btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin mr-2"></i> <span>${loadingText}</span>`;
  
  try {
    return await asyncFn();
  } finally {
    btn.disabled = false;
    btn.classList.remove('opacity-75', 'cursor-not-allowed');
    btn.innerHTML = originalHtml;
  }
}

/**
 * Popup Modal thông báo lỗi sang trọng
 */
function showErrorModal(title, text) {
  if (typeof Swal !== 'undefined') {
    Swal.fire({
      icon: 'error',
      title: title || 'Đã xảy ra lỗi!',
      text: text || '',
      confirmButtonColor: '#ef4444',
      confirmButtonText: 'Đóng'
    });
  } else {
    alert(`${title}\n${text}`);
  }
}

// ============================================================
// 2. GLOBAL LOADING OVERLAY (SPINNER)
// ============================================================
let activeRequests = 0;

function showLoading(show = true) {
  let overlay = document.getElementById('global-loading-overlay');
  if (show) {
    activeRequests++;
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'global-loading-overlay';
      overlay.className = 'fixed inset-0 bg-slate-900/40 backdrop-blur-xs z-50 flex items-center justify-center transition-opacity duration-300';
      overlay.innerHTML = `
        <div class="bg-white/95 rounded-2xl p-6 shadow-2xl flex flex-col items-center gap-3 border border-blue-100 animate-bounce-short">
          <div class="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
          <span class="text-sm font-bold text-gray-700 tracking-wide">Đang xử lý dữ liệu...</span>
        </div>
      `;
      document.body.appendChild(overlay);
    }
    overlay.classList.remove('hidden');
    overlay.classList.add('flex');
  } else {
    activeRequests = Math.max(0, activeRequests - 1);
    if (activeRequests === 0 && overlay) {
      overlay.classList.add('hidden');
      overlay.classList.remove('flex');
    }
  }
}

// ============================================================
// 3. FETCH API TRUNG TÂM
// ============================================================
async function fetchAPI(endpoint, method = 'GET', body = null, params = null) {
  let url = `${API_BASE}${endpoint}`;
  if (params) {
    const qs = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''))
    ).toString();
    if (qs) url += '?' + qs;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  const token = localStorage.getItem('token');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const options = { method, headers };
  if (body && method !== 'GET') {
    options.body = JSON.stringify(body);
  }

  // Tự động disable nút submit & hiện spinner
  showLoading(true);

  try {
    const response = await fetch(url, options);
    let data = null;
    try {
      data = await response.json();
    } catch {
      data = null;
    }

    if (!response.ok) {
      let errMessage = data?.message || data?.error || `Lỗi máy chủ (${response.status})`;

      if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        showToast('Phiên làm việc hết hạn. Vui lòng đăng nhập lại!', 'error');
        setTimeout(() => { window.location.href = 'login.html'; }, 1500);
      } else if (response.status === 409) {
        // Xung đột ghế hoặc xung đột bảo trì
        showErrorModal('Rất tiếc! Xung đột chỗ ngồi', errMessage || 'Ghế này vừa bị người khác đặt hoặc xe đang bảo trì. Vui lòng chọn ghế khác!');
      } else if (response.status === 403) {
        showErrorModal('Không có quyền truy cập', 'Bạn không có quyền thực hiện thao tác này!');
      } else if (response.status === 400 && data?.details) {
        const detailMsg = Object.values(data.details).join(', ');
        showErrorModal('Dữ liệu không hợp lệ', detailMsg || errMessage);
      } else if (response.status >= 500) {
        showErrorModal('Lỗi máy chủ nội bộ', errMessage || 'Máy chủ đang bận, vui lòng thử lại sau giây lát!');
      } else {
        showToast(errMessage, 'error');
      }

      const err = new Error(errMessage);
      err.status = response.status;
      err.apiData = data;
      throw err;
    }

    return data?.data !== undefined ? data.data : data;

  } catch (networkError) {
    if (!networkError.status) {
      showErrorModal(
        'Mất kết nối máy chủ',
        'Không thể kết nối đến REST API tại http://localhost:8080. Hãy đảm bảo máy chủ Spring Boot đang chạy!'
      );
    }
    throw networkError;
  } finally {
    showLoading(false);
  }
}

// ============================================================
// 4. USER & AUTH HELPERS
// ============================================================
function getCurrentUser() {
  try {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function isLoggedIn() {
  return !!localStorage.getItem('token');
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  showToast('Đã đăng xuất tài khoản!', 'info');
  setTimeout(() => {
    window.location.href = 'login.html';
  }, 800);
}

// ============================================================
// 5. NAVBAR RENDERER (Phong cách Traveloka/Vexere)
// ============================================================
let currentActiveNavPage = '';

function renderNavbar(activePage = '') {
  if (activePage) {
    currentActiveNavPage = activePage;
  } else {
    activePage = currentActiveNavPage;
  }
  const user = getCurrentUser();
  const role = user?.role || '';
  const isOp = role.includes('OPERATOR') || role.includes('ADMIN');
  const isAdmin = role.includes('ADMIN');

  const navHtml = `
    <header class="bg-white/95 backdrop-blur-md border-b border-slate-100 sticky top-0 z-40 shadow-xs transition-all">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16 sm:h-20">
          
          <!-- Logo Brand -->
          <a href="index.html" class="flex items-center gap-3 group">
            <div class="w-10 h-10 sm:w-11 sm:h-11 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-700 flex items-center justify-center text-white shadow-md shadow-blue-500/25 group-hover:scale-105 transition-transform duration-300">
              <i class="fa-solid fa-bus text-lg"></i>
            </div>
            <div>
              <span class="text-xl sm:text-2xl font-black bg-gradient-to-r from-blue-700 via-indigo-600 to-sky-600 bg-clip-text text-transparent">BusTicket</span>
              <span class="hidden sm:inline-block ml-1 px-1.5 py-0.5 text-[10px] font-extrabold uppercase tracking-wider bg-blue-100 text-blue-700 rounded-md">Pro</span>
              <p class="text-[10px] text-slate-400 font-medium -mt-1 hidden sm:block">Vé xe khách liên tỉnh</p>
            </div>
          </a>

          <!-- Main Nav Links -->
          <nav class="hidden md:flex items-center gap-1">
            <a href="index.html" class="px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${activePage === 'home' ? 'text-blue-600 bg-blue-50/80 font-bold' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'}">
              <i class="fa-solid fa-house-chimney text-xs mr-1.5 opacity-70"></i>Trang chủ
            </a>
            <a href="trips.html" class="px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${activePage === 'trips' ? 'text-blue-600 bg-blue-50/80 font-bold' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'}">
              <i class="fa-solid fa-route text-xs mr-1.5 opacity-70"></i>Tìm chuyến
            </a>
            <a href="my-tickets.html" class="px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${activePage === 'tickets' ? 'text-blue-600 bg-blue-50/80 font-bold' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-50'}">
              <i class="fa-solid fa-ticket text-xs mr-1.5 opacity-70"></i>Vé của tôi
            </a>
            ${isOp ? `
              <a href="operator.html" class="px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${activePage === 'operator' ? 'text-indigo-600 bg-indigo-50 font-bold' : 'text-slate-600 hover:text-indigo-600 hover:bg-slate-50'}">
                <i class="fa-solid fa-van-shuttle text-xs mr-1.5 opacity-70"></i>Nhà xe
              </a>
            ` : ''}
            ${isAdmin ? `
              <a href="admin.html" class="px-3.5 py-2 rounded-xl text-sm font-semibold transition-all ${activePage === 'admin' ? 'text-purple-600 bg-purple-50 font-bold' : 'text-slate-600 hover:text-purple-600 hover:bg-slate-50'}">
                <i class="fa-solid fa-chart-pie text-xs mr-1.5 opacity-70"></i>Quản trị
              </a>
            ` : ''}
          </nav>

          <!-- Auth Actions -->
          <div class="flex items-center gap-3">
            ${user ? `
              <div class="flex items-center gap-2.5 bg-slate-50 border border-slate-200/80 rounded-2xl px-3 py-1.5 sm:px-3.5 sm:py-2">
                <div class="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white text-xs font-bold shadow-xs">
                  ${(user.fullName || user.username || 'U').charAt(0).toUpperCase()}
                </div>
                <div class="hidden sm:block text-left">
                  <div class="text-xs font-bold text-slate-800 leading-tight">${user.fullName || user.username}</div>
                  <div class="text-[10px] font-semibold text-blue-600">${user.role === 'ROLE_ADMIN' ? '👑 Quản Trị Viên' : user.role === 'ROLE_OPERATOR' ? '🚌 Quản Lý Nhà Xe' : '👤 Khách Hàng'}</div>
                </div>
                <button onclick="logout()" title="Đăng xuất" class="ml-1 text-slate-400 hover:text-rose-600 p-1 rounded-lg hover:bg-rose-50 transition">
                  <i class="fa-solid fa-arrow-right-from-bracket text-xs"></i>
                </button>
              </div>
            ` : `
              <a href="login.html" class="inline-flex items-center gap-2 px-4 py-2 sm:px-5 sm:py-2.5 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 shadow-md shadow-blue-500/20 hover:shadow-lg transition-all transform hover:-translate-y-0.5 active:translate-y-0">
                <i class="fa-regular fa-user text-xs"></i>
                <span>Đăng nhập</span>
              </a>
            `}
          </div>

        </div>
      </div>
    </header>
  `;

  const target = document.getElementById('navbar');
  if (target) target.innerHTML = navHtml;
}

// ============================================================
// 6. FORMATTERS & BADGES
// ============================================================
function formatVND(amount) {
  if (amount === undefined || amount === null) return '0 đ';
  return Number(amount).toLocaleString('vi-VN') + ' đ';
}

function formatDateTime(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  return d.toLocaleString('vi-VN', {
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric'
  });
}

function formatTime(isoString) {
  if (!isoString) return '--:--';
  const d = new Date(isoString);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function formatDate(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function statusBadge(status) {
  const map = {
    ACTIVE:      'bg-emerald-50 text-emerald-700 border-emerald-200 fa-circle-check',
    SCHEDULED:   'bg-blue-50 text-blue-700 border-blue-200 fa-calendar-check',
    CONFIRMED:   'bg-emerald-50 text-emerald-700 border-emerald-200 fa-badge-check',
    PENDING:     'bg-amber-50 text-amber-700 border-amber-200 fa-hourglass-half',
    MAINTENANCE: 'bg-orange-50 text-orange-700 border-orange-200 fa-wrench',
    CANCELLED:   'bg-rose-50 text-rose-700 border-rose-200 fa-circle-xmark',
    EXPIRED:     'bg-slate-100 text-slate-500 border-slate-200 fa-clock-rotate-left',
  };
  const cls = map[status] || 'bg-slate-50 text-slate-600 border-slate-200 fa-circle-info';
  const icon = cls.split(' ').pop();
  const label = {
    ACTIVE: 'Hoạt động',
    SCHEDULED: 'Đã lên lịch',
    CONFIRMED: 'Đã xác nhận',
    PENDING: 'Chờ thanh toán',
    MAINTENANCE: 'Bảo trì',
    CANCELLED: 'Đã hủy',
    EXPIRED: 'Hết hạn'
  }[status] || status;

  return `
    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold border ${cls}">
      <i class="fa-solid ${icon} text-[10px]"></i>
      ${label}
    </span>
  `;
}

// ============================================================
// 7. MULTI-TAB SESSION SYNCHRONIZATION (ĐỒNG BỘ TRẠNG THÁI ĐĂNG NHẬP ĐA TAB)
// ============================================================
/**
 * Lắng nghe sự kiện thay đổi localStorage giữa các Tab (Cross-Tab Synchronization).
 * Khi người dùng Đăng nhập, Đăng xuất hoặc Chuyển đổi tài khoản ở Tab khác:
 * - Tự động cập nhật lại Header / Navbar mà không cần F5.
 * - Kiểm tra phân quyền: Nếu tài khoản mới không có quyền truy cập trang quản trị hiện tại,
 *   lập tức thông báo và chuyển hướng an toàn để bảo mật dữ liệu.
 */
window.addEventListener('storage', function(e) {
  // Chỉ phản hồi khi khóa 'token' hoặc 'user' bị thay đổi trên trình duyệt
  if (e.key !== 'token' && e.key !== 'user') return;

  const currentPath = window.location.pathname.toLowerCase();
  const isLoginPage = currentPath.endsWith('login.html');
  const isOperatorPage = currentPath.endsWith('operator.html');
  const isAdminPage = currentPath.endsWith('admin.html');
  const isTicketsPage = currentPath.endsWith('my-tickets.html');
  const isProtectedPage = isOperatorPage || isAdminPage || isTicketsPage;

  const currentToken = localStorage.getItem('token');
  const currentUser = getCurrentUser();

  // TRƯỜNG HỢP 1: Người dùng đã bấm "Đăng xuất" từ một Tab khác
  if (!currentToken || !currentUser) {
    if (typeof showToast === 'function') {
      showToast('Tài khoản đã đăng xuất từ một tab khác!', 'warning', 2500);
    }

    if (isProtectedPage) {
      if (typeof Swal !== 'undefined') {
        Swal.fire({
          icon: 'info',
          title: 'Phiên làm việc đã kết thúc',
          text: 'Bạn đã đăng xuất ở một tab khác. Hệ thống đang chuyển hướng về trang đăng nhập...',
          timer: 2000,
          showConfirmButton: false,
          allowOutsideClick: false
        }).then(() => {
          window.location.href = 'login.html';
        });
      } else {
        setTimeout(() => { window.location.href = 'login.html'; }, 1000);
      }
    } else {
      // Trang công khai (index.html, trips.html): Cập nhật ngay Navbar về trạng thái Chưa đăng nhập
      if (typeof renderNavbar === 'function') {
        renderNavbar();
      }
    }
    return;
  }

  // TRƯỜNG HỢP 2: Người dùng vừa Đăng nhập tài khoản mới từ Tab khác
  if (isLoginPage) {
    // Nếu đang ở trang login mà tab khác đã đăng nhập xong -> chuyển về trang chủ ngay
    window.location.href = 'index.html';
    return;
  }

  const role = currentUser.role || '';
  const isAdmin = role === 'ROLE_ADMIN';
  const isOperator = role === 'ROLE_OPERATOR' || isAdmin;

  // Kiểm tra vi phạm phân quyền khi đổi tài khoản:
  // Ví dụ: Đang mở operator.html nhưng tab kia đăng nhập tài khoản Khách hàng
  if ((isAdminPage && !isAdmin) || (isOperatorPage && !isOperator)) {
    if (typeof Swal !== 'undefined') {
      Swal.fire({
        icon: 'warning',
        title: 'Quyền hạn đã thay đổi!',
        text: `Bạn vừa chuyển sang tài khoản "${currentUser.fullName || currentUser.username}". Tài khoản này không có quyền truy cập trang quản trị này.`,
        confirmButtonColor: '#2563eb',
        confirmButtonText: 'Về trang chủ',
        allowOutsideClick: false
      }).then(() => {
        window.location.href = 'index.html';
      });
    } else {
      alert(`Tài khoản đổi sang ${currentUser.fullName || currentUser.username}. Đang chuyển hướng...`);
      window.location.href = 'index.html';
    }
    return;
  }

  // Trường hợp tài khoản hợp lệ: Cập nhật giao diện Navbar ngay lập tức
  if (typeof renderNavbar === 'function') {
    renderNavbar();
  }

  if (typeof showToast === 'function') {
    showToast(`Đồng bộ tài khoản thành công: ${currentUser.fullName || currentUser.username}`, 'info', 2500);
  }

  // Nếu đang ở trang Lịch sử vé hoặc trang Nhà xe, tải lại dữ liệu khớp với user mới
  if (isTicketsPage && typeof loadMyTickets === 'function') {
    loadMyTickets();
  } else if (isOperatorPage && typeof initDashboard === 'function') {
    initDashboard();
  }
});

