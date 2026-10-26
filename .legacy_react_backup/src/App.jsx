import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/common/Navbar';
import { Footer } from './components/common/Footer';
import { ProtectedRoute } from './components/common/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { SearchResultsPage } from './pages/SearchResultsPage';
import { PaymentPage } from './pages/PaymentPage';
import { MyTicketsPage } from './pages/MyTicketsPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OperatorDashboardPage } from './pages/OperatorDashboardPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { NotFoundPage } from './pages/NotFoundPage';

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800 font-sans selection:bg-orange-500 selection:text-white">
            <Navbar />
            <main className="flex-1">
              <Routes>
                {/* 1. Phân hệ Khách hàng (Public & Customer UI) */}
                <Route path="/" element={<HomePage />} />
                <Route path="/search" element={<SearchResultsPage />} />
                <Route path="/payment/:ticketId" element={<PaymentPage />} />
                <Route path="/my-tickets" element={<MyTicketsPage />} />

                {/* Phân hệ Xác thực */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* 2. Phân hệ Quản lý Nhà xe (Role OPERATOR & ADMIN) */}
                <Route
                  path="/operator"
                  element={
                    <ProtectedRoute requiredRole="ROLE_OPERATOR">
                      <OperatorDashboardPage />
                    </ProtectedRoute>
                  }
                />

                {/* 3. Phân hệ Quản trị viên (Role ADMIN) */}
                <Route
                  path="/admin"
                  element={
                    <ProtectedRoute requiredRole="ROLE_ADMIN">
                      <AdminDashboardPage />
                    </ProtectedRoute>
                  }
                />

                {/* 404 Not Found */}
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </main>
            <Footer />
          </div>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  );
}

export default App;
