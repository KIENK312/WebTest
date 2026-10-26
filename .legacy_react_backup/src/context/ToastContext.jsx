import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((type = 'info', message, title) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 9);
    const newToast = { id, type, message, title };

    setToasts((prev) => [...prev, newToast]);

    // Tự động đóng sau 4.5 giây
    setTimeout(() => {
      removeToast(id);
    }, 4500);

    return id;
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  // Lắng nghe sự kiện custom từ Axios Interceptor hoặc bất kỳ đâu
  useEffect(() => {
    const handleCustomToast = (event) => {
      const { type, message, title } = event.detail || {};
      if (message) {
        addToast(type || 'info', message, title);
      }
    };

    window.addEventListener('app:toast', handleCustomToast);
    return () => window.removeEventListener('app:toast', handleCustomToast);
  }, [addToast]);

  const success = (message, title = 'Thành Công') => addToast('success', message, title);
  const error = (message, title = 'Có Lỗi Xảy Ra') => addToast('error', message, title);
  const warning = (message, title = 'Cảnh Báo') => addToast('warning', message, title);
  const info = (message, title = 'Thông Báo') => addToast('info', message, title);

  return (
    <ToastContext.Provider value={{ addToast, removeToast, success, error, warning, info }}>
      {children}
      <div className="fixed top-5 right-5 z-50 flex flex-col gap-2.5 max-w-md w-full pointer-events-none px-4 sm:px-0">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`pointer-events-auto flex items-start gap-3 p-4 rounded-xl shadow-xl border backdrop-blur-md transition-all duration-300 transform translate-y-0 animate-in slide-in-from-top-4 ${
              toast.type === 'success'
                ? 'bg-emerald-50/95 border-emerald-300 text-emerald-950'
                : toast.type === 'error'
                ? 'bg-rose-50/95 border-rose-300 text-rose-950'
                : toast.type === 'warning'
                ? 'bg-amber-50/95 border-amber-300 text-amber-950'
                : 'bg-sky-50/95 border-sky-300 text-sky-950'
            }`}
          >
            <div className="flex-shrink-0 mt-0.5">
              {toast.type === 'success' && <CheckCircle2 className="w-5 h-5 text-emerald-600" />}
              {toast.type === 'error' && <AlertCircle className="w-5 h-5 text-rose-600" />}
              {toast.type === 'warning' && <AlertTriangle className="w-5 h-5 text-amber-600" />}
              {toast.type === 'info' && <Info className="w-5 h-5 text-sky-600" />}
            </div>

            <div className="flex-1 min-w-0">
              {toast.title && (
                <h4 className="font-semibold text-sm leading-tight mb-1">{toast.title}</h4>
              )}
              <p className="text-xs leading-relaxed opacity-90 break-words">{toast.message}</p>
            </div>

            <button
              onClick={() => removeToast(toast.id)}
              className="flex-shrink-0 text-slate-400 hover:text-slate-700 transition p-1 rounded-md"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
