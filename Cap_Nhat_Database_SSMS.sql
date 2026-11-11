-- =========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH & ĐĂNG KÝ LỊCH HẸN TƯ VẤN (MULTI-PARTNER)
-- KỊCH BẢN CẬP NHẬT DATABASE DÀNH CHO SQL SERVER MANAGEMENT STUDIO (SSMS)
-- Database: QuanLyVeXeKhach2
-- Tương thích: SQL Server 2016, 2019, 2022+ / Azure SQL
-- Hướng dẫn: Mở file này trong SSMS -> Nhấn F5 (Execute)
-- =========================================================================================

-- 1. TẠO VÀ CHỌN DATABASE
IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = N'QuanLyVeXeKhach2')
BEGIN
    PRINT N'[1/6] Đang tạo database QuanLyVeXeKhach2...';
    CREATE DATABASE QuanLyVeXeKhach2;
END
GO

USE QuanLyVeXeKhach2;
GO

PRINT N'====================================================================';
PRINT N'BẮT ĐẦU CẬP NHẬT CẤU TRÚC VÀ DỮ LIỆU DATABASE: QuanLyVeXeKhach2';
PRINT N'====================================================================';
GO

-- -----------------------------------------------------------------------------------------
-- 2. TẠO / CẬP NHẬT BẢNG ROLES (VAI TRÒ HỆ THỐNG)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'roles')
BEGIN
    PRINT N'[2/6] Tạo bảng roles...';
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1),
        name NVARCHAR(50) NOT NULL,
        description NVARCHAR(255) NULL,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_roles PRIMARY KEY (id),
        CONSTRAINT UQ_roles_name UNIQUE (name)
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 3. TẠO / CẬP NHẬT BẢNG BUS_OPERATORS (NHÀ XE / ĐỐI TÁC VẬN TẢI)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'bus_operators')
BEGIN
    PRINT N'[2/6] Tạo bảng bus_operators...';
    CREATE TABLE bus_operators (
        id BIGINT IDENTITY(1,1),
        name NVARCHAR(150) NOT NULL,
        code NVARCHAR(50) NOT NULL,
        contact_email NVARCHAR(100) NULL,
        contact_phone NVARCHAR(20) NOT NULL,
        address NVARCHAR(255) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_bus_operators PRIMARY KEY (id),
        CONSTRAINT UQ_bus_operators_code UNIQUE (code),
        CONSTRAINT CK_bus_operators_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 4. TẠO / CẬP NHẬT BẢNG USERS (TÀI KHOẢN NGƯỜI DÙNG ĐA VAI TRÒ)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'users')
BEGIN
    PRINT N'[2/6] Tạo bảng users...';
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1),
        operator_id BIGINT NULL,
        role_id BIGINT NOT NULL,
        username NVARCHAR(50) NOT NULL,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100) NOT NULL,
        phone_number NVARCHAR(20) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_users PRIMARY KEY (id),
        CONSTRAINT UQ_users_username UNIQUE (username),
        CONSTRAINT UQ_users_email UNIQUE (email),
        CONSTRAINT CK_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
        CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT FK_users_bus_operators FOREIGN KEY (operator_id) REFERENCES bus_operators(id) ON DELETE SET NULL ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 5. TẠO / CẬP NHẬT BẢNG BUSES (PHƯƠNG TIỆN XE KHÁCH)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'buses')
BEGIN
    PRINT N'[2/6] Tạo bảng buses...';
    CREATE TABLE buses (
        id BIGINT IDENTITY(1,1),
        operator_id BIGINT NOT NULL,
        plate_number NVARCHAR(30) NOT NULL,
        bus_type NVARCHAR(50) NOT NULL,
        total_seats INT NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_buses PRIMARY KEY (id),
        CONSTRAINT UQ_buses_plate_number UNIQUE (plate_number),
        CONSTRAINT CK_buses_total_seats CHECK (total_seats > 0),
        CONSTRAINT CK_buses_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE')),
        CONSTRAINT FK_buses_bus_operators FOREIGN KEY (operator_id) REFERENCES bus_operators(id) ON DELETE NO ACTION ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 6. TẠO / CẬP NHẬT BẢNG ROUTES (TUYẾN ĐƯỜNG VẬN TẢI DU LỊCH)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'routes')
BEGIN
    PRINT N'[2/6] Tạo bảng routes...';
    CREATE TABLE routes (
        id BIGINT IDENTITY(1,1),
        operator_id BIGINT NOT NULL,
        origin NVARCHAR(100) NOT NULL,
        destination NVARCHAR(100) NOT NULL,
        distance_km DECIMAL(8,2) NULL,
        estimated_duration_hours DECIMAL(4,2) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_routes PRIMARY KEY (id),
        CONSTRAINT CK_routes_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
        CONSTRAINT FK_routes_bus_operators FOREIGN KEY (operator_id) REFERENCES bus_operators(id) ON DELETE NO ACTION ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 7. TẠO / CẬP NHẬT BẢNG TRIPS (LỊCH TRÌNH CHUYẾN ĐI)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'trips')
BEGIN
    PRINT N'[2/6] Tạo bảng trips...';
    CREATE TABLE trips (
        id BIGINT IDENTITY(1,1),
        route_id BIGINT NOT NULL,
        bus_id BIGINT NOT NULL,
        departure_time DATETIME2 NOT NULL,
        arrival_time DATETIME2 NOT NULL,
        base_price DECIMAL(18,2) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_trips PRIMARY KEY (id),
        CONSTRAINT CK_trips_price CHECK (base_price >= 0),
        CONSTRAINT CK_trips_status CHECK (status IN ('SCHEDULED', 'RUNNING', 'DEPARTED', 'COMPLETED', 'CANCELLED')),
        CONSTRAINT CK_trips_times CHECK (arrival_time > departure_time),
        CONSTRAINT FK_trips_routes FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT FK_trips_buses FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE NO ACTION ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 8. TẠO / CẬP NHẬT BẢNG SEATS (SƠ ĐỒ VỊ TRÍ GHẾ / GIƯỜNG)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'seats')
BEGIN
    PRINT N'[2/6] Tạo bảng seats...';
    CREATE TABLE seats (
        id BIGINT IDENTITY(1,1),
        bus_id BIGINT NOT NULL,
        seat_number NVARCHAR(20) NOT NULL,
        seat_type NVARCHAR(30) NOT NULL DEFAULT 'STANDARD',
        floor_number INT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_seats PRIMARY KEY (id),
        CONSTRAINT UQ_seats_bus_seat UNIQUE (bus_id, seat_number),
        CONSTRAINT CK_seats_floor CHECK (floor_number IN (1, 2)),
        CONSTRAINT FK_seats_buses FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 9. TẠO / CẬP NHẬT BẢNG TICKETS (VÉ XE & TEM SỐ ĐIỆN TỬ)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'tickets')
BEGIN
    PRINT N'[2/6] Tạo bảng tickets...';
    CREATE TABLE tickets (
        id BIGINT IDENTITY(1,1),
        trip_id BIGINT NOT NULL,
        seat_id BIGINT NOT NULL,
        user_id BIGINT NULL,
        customer_name NVARCHAR(100) NOT NULL,
        customer_phone NVARCHAR(20) NOT NULL,
        customer_email NVARCHAR(100) NULL,
        ticket_code NVARCHAR(50) NOT NULL,
        price DECIMAL(18,2) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        version BIGINT NOT NULL DEFAULT 0,
        booking_time DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_tickets PRIMARY KEY (id),
        CONSTRAINT UQ_tickets_code UNIQUE (ticket_code),
        CONSTRAINT CK_tickets_price CHECK (price >= 0),
        CONSTRAINT CK_tickets_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
        CONSTRAINT FK_tickets_trips FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT FK_tickets_seats FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
        CONSTRAINT FK_tickets_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 10. TẠO / CẬP NHẬT BẢNG MAINTENANCE_LOGS (LỊCH BẢO DƯỠNG XE)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'maintenance_logs')
BEGIN
    PRINT N'[2/6] Tạo bảng maintenance_logs...';
    CREATE TABLE maintenance_logs (
        id BIGINT IDENTITY(1,1),
        bus_id BIGINT NOT NULL,
        start_time DATETIME2 NOT NULL,
        end_time DATETIME2 NULL,
        maintenance_type NVARCHAR(50) NOT NULL,
        cost DECIMAL(18,2) NOT NULL DEFAULT 0.00,
        description NVARCHAR(500) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_maintenance_logs PRIMARY KEY (id),
        CONSTRAINT CK_maintenance_cost CHECK (cost >= 0),
        CONSTRAINT CK_maintenance_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
        CONSTRAINT FK_maintenance_logs_buses FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 11. TẠO / CẬP NHẬT BẢNG PAYMENTS (LỊCH SỬ GIAO DỊCH THANH TOÁN)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'payments')
BEGIN
    PRINT N'[2/6] Tạo bảng payments...';
    CREATE TABLE payments (
        id BIGINT IDENTITY(1,1),
        ticket_id BIGINT NOT NULL,
        amount DECIMAL(18,2) NOT NULL,
        payment_method NVARCHAR(50) NOT NULL,
        transaction_code NVARCHAR(100) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        payment_time DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_payments PRIMARY KEY (id),
        CONSTRAINT CK_payments_amount CHECK (amount >= 0),
        CONSTRAINT CK_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
        CONSTRAINT FK_payments_tickets FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE NO ACTION ON UPDATE NO ACTION
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 12. TẠO / CẬP NHẬT BẢNG APPOINTMENTS (LỊCH HẸN TƯ VẤN TRỰC TIẾP - WORKFLOW 4)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'appointments')
BEGIN
    PRINT N'[2/6] Tạo bảng appointments (Workflow 4: Lịch hẹn tư vấn)...';
    CREATE TABLE appointments (
        id BIGINT IDENTITY(1,1),
        appointment_code NVARCHAR(50) NOT NULL,
        customer_name NVARCHAR(100) NOT NULL,
        customer_phone NVARCHAR(20) NOT NULL,
        customer_email NVARCHAR(100) NULL,
        appointment_date DATE NOT NULL,
        time_slot NVARCHAR(50) NOT NULL,
        consultation_type NVARCHAR(100) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        notes NVARCHAR(500) NULL,
        assigned_staff NVARCHAR(100) NULL,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT PK_appointments PRIMARY KEY (id),
        CONSTRAINT UQ_appointments_code UNIQUE (appointment_code),
        CONSTRAINT CK_appointments_status CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'))
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 13. TỐI ƯU HÓA HIỆU NĂNG: ĐÁNH CHỈ MỤC (INDEXES)
-- -----------------------------------------------------------------------------------------
PRINT N'[3/6] Kiểm tra và tạo 5 Non-Clustered Indexes tối ưu truy vấn...';

-- Index 1: Tìm kiếm chuyến xe theo thời gian xuất bến và tuyến đường
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_trips_departure_route' AND object_id = OBJECT_ID('trips'))
BEGIN
    CREATE NONCLUSTERED INDEX idx_trips_departure_route 
    ON trips(departure_time, route_id)
    INCLUDE (bus_id, base_price, status);
    PRINT N'  -> Đã tạo idx_trips_departure_route';
END

-- Index 2: Kiểm tra tình trạng ghế trong chuyến
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_tickets_trip_seat' AND object_id = OBJECT_ID('tickets'))
BEGIN
    CREATE NONCLUSTERED INDEX idx_tickets_trip_seat 
    ON tickets(trip_id, seat_id)
    INCLUDE (status, ticket_code, price);
    PRINT N'  -> Đã tạo idx_tickets_trip_seat';
END

-- Index 3: Kiểm tra xung đột lịch bảo dưỡng phương tiện
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_maintenance_bus_time' AND object_id = OBJECT_ID('maintenance_logs'))
BEGIN
    CREATE NONCLUSTERED INDEX idx_maintenance_bus_time 
    ON maintenance_logs(bus_id, start_time, end_time)
    INCLUDE (status, maintenance_type);
    PRINT N'  -> Đã tạo idx_maintenance_bus_time';
END

-- Index 4: Tra cứu lịch hẹn theo số điện thoại
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_appointments_phone' AND object_id = OBJECT_ID('appointments'))
BEGIN
    CREATE NONCLUSTERED INDEX idx_appointments_phone 
    ON appointments(customer_phone);
    PRINT N'  -> Đã tạo idx_appointments_phone';
END

-- Index 5: Tra cứu lịch hẹn theo ngày và khung giờ (giới hạn 5 khách/slot)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_appointments_date_slot' AND object_id = OBJECT_ID('appointments'))
BEGIN
    CREATE NONCLUSTERED INDEX idx_appointments_date_slot 
    ON appointments(appointment_date, time_slot);
    PRINT N'  -> Đã tạo idx_appointments_date_slot';
END
GO

-- -----------------------------------------------------------------------------------------
-- 14. NẠP DỮ LIỆU NỀN TẢNG (ROLES & BUS_OPERATORS)
-- -----------------------------------------------------------------------------------------
PRINT N'[4/6] Đồng bộ Roles và Đối tác nhà xe...';

-- Roles
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_ADMIN', N'Quản trị viên toàn hệ sinh thái', SYSDATETIME());
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_OPERATOR')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_OPERATOR', N'Nhân viên / Điều hành nhà xe đối tác', SYSDATETIME());
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_CUSTOMER', N'Hành khách sử dụng dịch vụ đặt vé', SYSDATETIME());

-- Đối tác nhà xe: Phương Trang & Hoàng Long
IF NOT EXISTS (SELECT 1 FROM bus_operators WHERE code = 'FUTA')
BEGIN
    INSERT INTO bus_operators (name, code, contact_phone, contact_email, address, status, created_at, updated_at)
    VALUES (
        N'Công ty Cổ phần Xe khách Phương Trang (FUTA Bus Lines)',
        N'FUTA', N'19006067', N'hotro@futa.vn',
        N'Số 01 Tô Hiến Thành, Phường 3, TP. Đà Lạt, Lâm Đồng',
        'ACTIVE', SYSDATETIME(), SYSDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM bus_operators WHERE code = 'HOANGLONG')
BEGIN
    INSERT INTO bus_operators (name, code, contact_phone, contact_email, address, status, created_at, updated_at)
    VALUES (
        N'Công ty TNHH Vận tải Hoàng Long (Hoàng Long Bus Lines)',
        N'HOANGLONG', N'02253920920', N'info@hoanglongasia.com',
        N'Số 05 Phạm Ngũ Lão, Lương Khánh Thiện, Ngô Quyền, Hải Phòng',
        'ACTIVE', SYSDATETIME(), SYSDATETIME()
    );
END

-- Dọn dẹp dữ liệu Thành Bưởi (nếu còn)
IF EXISTS (SELECT 1 FROM bus_operators WHERE code = 'THANHBUOI')
BEGIN
    DELETE FROM users WHERE operator_id IN (SELECT id FROM bus_operators WHERE code = 'THANHBUOI');
    DELETE FROM bus_operators WHERE code = 'THANHBUOI';
END
GO

-- -----------------------------------------------------------------------------------------
-- 15. NẠP DỮ LIỆU TÀI KHOẢN MẪU (USERS)
-- Mật khẩu mặc định: 'Admin@123', 'Operator@123', 'Customer@123'
-- Hash BCrypt chuẩn: $2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6
-- -----------------------------------------------------------------------------------------
PRINT N'[5/6] Đồng bộ tài khoản người dùng mẫu...';

DECLARE @RoleIdAdmin BIGINT = (SELECT TOP 1 id FROM roles WHERE name = 'ROLE_ADMIN');
DECLARE @RoleIdOperator BIGINT = (SELECT TOP 1 id FROM roles WHERE name = 'ROLE_OPERATOR');
DECLARE @RoleIdCustomer BIGINT = (SELECT TOP 1 id FROM roles WHERE name = 'ROLE_CUSTOMER');
DECLARE @FutaId BIGINT = (SELECT TOP 1 id FROM bus_operators WHERE code = 'FUTA');
DECLARE @HoangLongId BIGINT = (SELECT TOP 1 id FROM bus_operators WHERE code = 'HOANGLONG');

-- 1. Admin
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (@RoleIdAdmin, NULL, 'admin', N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', N'Quản Trị Viên Hệ Thống', 'admin@busbooking.vn', '0901112233', 'ACTIVE', SYSDATETIME(), SYSDATETIME());
END

-- 2. Customer: Nguyễn Trung Kiên
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (@RoleIdCustomer, NULL, 'customer', N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', N'Nguyễn Trung Kiên', 'kientrungnguyen@gmail.com', '0987654321', 'ACTIVE', SYSDATETIME(), SYSDATETIME());
END
ELSE
BEGIN
    UPDATE users 
    SET full_name = N'Nguyễn Trung Kiên', email = 'kientrungnguyen@gmail.com', phone_number = '0987654321'
    WHERE username = 'customer';
END

-- 3. Customer mới
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer_new')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (@RoleIdCustomer, NULL, 'customer_new', N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', N'Nguyễn Trung Kiên (Mới)', 'kientrungnguyen.new@gmail.com', '0989999888', 'ACTIVE', SYSDATETIME(), SYSDATETIME());
END

-- 4. Operators
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'operator_futa')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (@RoleIdOperator, @FutaId, 'operator_futa', N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', N'Điều Hành Xe Phương Trang', 'operator@futa.vn', '0902223344', 'ACTIVE', SYSDATETIME(), SYSDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'operator_hoanglong')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (@RoleIdOperator, @HoangLongId, 'operator_hoanglong', N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', N'Điều Hành Xe Hoàng Long', 'operator@hoanglong.vn', '0903334455', 'ACTIVE', SYSDATETIME(), SYSDATETIME());
END
GO

-- -----------------------------------------------------------------------------------------
-- 16. NẠP LỊCH HẸN TƯ VẤN MẪU (WORKFLOW 4)
-- -----------------------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM appointments WHERE appointment_code = 'HEN-20261112-DEMO')
BEGIN
    INSERT INTO appointments (
        appointment_code, customer_name, customer_phone, customer_email,
        appointment_date, time_slot, consultation_type, status, notes,
        assigned_staff, created_at, updated_at
    )
    VALUES (
        'HEN-20261112-DEMO',
        N'Nguyễn Trung Kiên',
        '0901234567',
        'trungkien@gmail.com',
        DATEADD(day, 1, CAST(SYSDATETIME() AS DATE)),
        N'09:15 - 10:15',
        N'Tư vấn lộ trình & đặt vé đoàn',
        'CONFIRMED',
        N'Đoàn công tác 25 người tuyến Hà Nội - Đà Nẵng',
        N'Trần Minh Tuấn - Trưởng phòng Điều hành',
        SYSDATETIME(),
        SYSDATETIME()
    );
END
GO

-- -----------------------------------------------------------------------------------------
-- 17. BÁO CÁO TỔNG HỢP SAU KHI CẬP NHẬT DATABASE
-- -----------------------------------------------------------------------------------------
PRINT N'';
PRINT N'====================================================================';
PRINT N'CẬP NHẬT DATABASE VÀO SQL SERVER THÀNH CÔNG!';
PRINT N'====================================================================';
PRINT N'BẢNG THỐNG KÊ SỐ LƯỢNG BẢN GHI HIỆN CÓ:';

SELECT 
    t.name AS [Tên Bảng (Table)],
    SUM(p.rows) AS [Số Bản Ghi (Row Count)],
    CASE 
        WHEN t.name = 'appointments' THEN N'Workflow 4: Lịch hẹn tư vấn trực tiếp'
        WHEN t.name = 'tickets' THEN N'Workflow 1: Quản lý vé & Tem số điện tử'
        WHEN t.name = 'trips' THEN N'Lịch trình chuyến xe đa đối tác'
        WHEN t.name = 'seats' THEN N'Sơ đồ ghế ngồi theo xe'
        WHEN t.name = 'buses' THEN N'Đội xe vận tải'
        WHEN t.name = 'routes' THEN N'Tuyến đường du lịch liên tỉnh'
        WHEN t.name = 'users' THEN N'Người dùng phân quyền RBAC'
        WHEN t.name = 'bus_operators' THEN N'Đối tác vận tải (Phương Trang, Hoàng Long)'
        WHEN t.name = 'roles' THEN N'Danh mục vai trò bảo mật'
        WHEN t.name = 'payments' THEN N'Lịch sử giao dịch VietQR'
        WHEN t.name = 'maintenance_logs' THEN N'Nhật ký bảo dưỡng phương tiện'
        ELSE N'Bảng hệ thống'
    END AS [Chức Năng Kiến Trúc]
FROM sys.tables t
JOIN sys.partitions p ON t.object_id = p.object_id
WHERE p.index_id IN (0, 1) AND t.is_ms_shipped = 0
GROUP BY t.name
ORDER BY [Số Bản Ghi (Row Count)] DESC;

SELECT 
    name AS [Tên Index Hiệu Năng],
    type_desc AS [Loại Index],
    OBJECT_NAME(object_id) AS [Thuộc Bảng]
FROM sys.indexes 
WHERE name LIKE 'idx_%'
ORDER BY [Thuộc Bảng];
GO
