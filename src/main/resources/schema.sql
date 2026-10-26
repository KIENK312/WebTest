-- =========================================================================================
-- HỆ THỐNG QUẢN LÝ BÁN VÉ XE KHÁCH ĐA ĐỐI TÁC (MULTI-PARTNER BUS MANAGEMENT SYSTEM)
-- Database Engine: Microsoft SQL Server (T-SQL)
-- Date Format: DATETIME2 | Currency: DECIMAL(18,2) | ID: BIGINT IDENTITY(1,1)
-- =========================================================================================

-- Tùy chọn: Chọn database làm việc
USE QuanLyVeXeKhach2;
GO

-- -----------------------------------------------------------------------------------------
-- 0. XÓA BẢNG CŨ THEO THỨ TỰ PHỤ THUỘC (NẾU ĐÃ TỒN TẠI)
-- -----------------------------------------------------------------------------------------
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS maintenance_logs;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS buses;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS bus_operators;
DROP TABLE IF EXISTS roles;
GO

-- -----------------------------------------------------------------------------------------
-- 1. BẢNG: roles (Vai trò người dùng trong hệ thống)
-- Phục vụ phân quyền RBAC: Quản trị viên hệ thống (ADMIN), Nhà xe (OPERATOR), Khách (CUSTOMER)
-- -----------------------------------------------------------------------------------------
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1),
    name NVARCHAR(50) NOT NULL,
    description NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_roles PRIMARY KEY (id),
    CONSTRAINT UQ_roles_name UNIQUE (name)
);
GO

-- -----------------------------------------------------------------------------------------
-- 2. BẢNG: bus_operators (Nhà xe / Đối tác vận tải)
-- Đại diện cho các đối tác tham gia nền tảng (mô hình Multi-partner).
-- Mỗi nhà xe quản lý đội xe, tài khoản nhân viên và các chuyến xe của riêng mình.
-- -----------------------------------------------------------------------------------------
CREATE TABLE bus_operators (
    id BIGINT IDENTITY(1,1),
    name NVARCHAR(150) NOT NULL,
    code NVARCHAR(50) NOT NULL,                  -- Mã định danh nhà xe (VD: FUTA, HOANGLONG)
    contact_email NVARCHAR(100) NULL,
    contact_phone NVARCHAR(20) NOT NULL,
    address NVARCHAR(255) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_bus_operators PRIMARY KEY (id),
    CONSTRAINT UQ_bus_operators_code UNIQUE (code),
    CONSTRAINT CK_bus_operators_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);
GO

-- -----------------------------------------------------------------------------------------
-- 3. BẢNG: users (Tài khoản người dùng)
-- Quan hệ: 
--  - N-1 với roles: Mỗi người dùng có một vai trò chính.
--  - N-1 với bus_operators: Nếu role = 'OPERATOR', user thuộc về một nhà xe cụ thể. 
--                           Nếu là ADMIN hoặc CUSTOMER thì operator_id là NULL.
-- Lưu ý SQL Server: Sử dụng NO ACTION cho khóa ngoại để tránh xung đột chu kỳ (cycles / multiple cascade paths)
-- -----------------------------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT IDENTITY(1,1),
    operator_id BIGINT NULL,                      -- Khóa ngoại liên kết nhà xe (chỉ dành cho nhân viên nhà xe)
    role_id BIGINT NOT NULL,                      -- Khóa ngoại vai trò
    username NVARCHAR(50) NOT NULL,
    password NVARCHAR(255) NOT NULL,              -- Mật khẩu mã hóa BCrypt
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, LOCKED
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_users PRIMARY KEY (id),
    CONSTRAINT UQ_users_username UNIQUE (username),
    CONSTRAINT UQ_users_email UNIQUE (email),
    CONSTRAINT CK_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_users_bus_operators FOREIGN KEY (operator_id) 
        REFERENCES bus_operators(id) ON DELETE SET NULL ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 4. BẢNG: buses (Xe khách)
-- Quan hệ: 
--  - N-1 với bus_operators: Mỗi chiếc xe thuộc sở hữu của một đối tác nhà xe nhất định.
-- -----------------------------------------------------------------------------------------
CREATE TABLE buses (
    id BIGINT IDENTITY(1,1),
    operator_id BIGINT NOT NULL,                  -- Khóa ngoại thuộc nhà xe nào
    plate_number NVARCHAR(30) NOT NULL,           -- Biển kiểm soát (VD: 29B-888.88)
    bus_type NVARCHAR(50) NOT NULL,               -- SLEEPER (Giường nằm), LIMOUSINE, SEATER (Ghế ngồi)
    total_seats INT NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, INACTIVE
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_buses PRIMARY KEY (id),
    CONSTRAINT UQ_buses_plate_number UNIQUE (plate_number),
    CONSTRAINT CK_buses_total_seats CHECK (total_seats > 0),
    CONSTRAINT CK_buses_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE')),
    CONSTRAINT FK_buses_bus_operators FOREIGN KEY (operator_id) 
        REFERENCES bus_operators(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 5. BẢNG: routes (Tuyến đường)
-- Quan hệ: 
--  - N-1 với bus_operators: Mỗi đối tác nhà xe đăng ký khai thác tuyến đường riêng biệt.
-- -----------------------------------------------------------------------------------------
CREATE TABLE routes (
    id BIGINT IDENTITY(1,1),
    operator_id BIGINT NOT NULL,                  -- Nhà xe đăng ký tuyến
    origin NVARCHAR(100) NOT NULL,                -- Điểm khởi hành (VD: Hà Nội, Sài Gòn)
    destination NVARCHAR(100) NOT NULL,           -- Điểm đến (VD: Đà Nẵng, Đà Lạt)
    distance_km DECIMAL(8,2) NULL,                -- Chiều dài tuyến đường (km)
    estimated_duration_hours DECIMAL(4,2) NULL,   -- Thời gian di chuyển dự kiến (giờ)
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_routes PRIMARY KEY (id),
    CONSTRAINT CK_routes_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT FK_routes_bus_operators FOREIGN KEY (operator_id) 
        REFERENCES bus_operators(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 6. BẢNG: trips (Chuyến xe theo lịch trình)
-- Quan hệ:
--  - N-1 với routes: Một tuyến đường có nhiều chuyến xe trong ngày/tháng.
--  - N-1 với buses: Mỗi chuyến xe được vận hành bởi 1 phương tiện cụ thể.
-- Ràng buộc: arrival_time phải lớn hơn departure_time.
-- Sử dụng ON DELETE NO ACTION ON UPDATE NO ACTION để triệt tiêu lỗi Msg 1785 multiple cascade paths trong SQL Server
-- -----------------------------------------------------------------------------------------
CREATE TABLE trips (
    id BIGINT IDENTITY(1,1),
    route_id BIGINT NOT NULL,                     -- Tuyến đường
    bus_id BIGINT NOT NULL,                       -- Xe được điều phối
    departure_time DATETIME2 NOT NULL,            -- Giờ xuất bến
    arrival_time DATETIME2 NOT NULL,              -- Giờ đến dự kiến
    base_price DECIMAL(18,2) NOT NULL,            -- Giá vé cơ sở
    status NVARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, DEPARTED, COMPLETED, CANCELLED
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_trips PRIMARY KEY (id),
    CONSTRAINT CK_trips_price CHECK (base_price >= 0),
    CONSTRAINT CK_trips_status CHECK (status IN ('SCHEDULED', 'RUNNING', 'DEPARTED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_trips_times CHECK (arrival_time > departure_time),
    CONSTRAINT FK_trips_routes FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_trips_buses FOREIGN KEY (bus_id) 
        REFERENCES buses(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 7. BẢNG: seats (Sơ đồ ghế / giường trên xe)
-- Quan hệ: 
--  - N-1 với buses: Ghế gắn liền cố định với một xe. Một xe có nhiều ghế.
-- -----------------------------------------------------------------------------------------
CREATE TABLE seats (
    id BIGINT IDENTITY(1,1),
    bus_id BIGINT NOT NULL,                       -- Thuộc xe nào
    seat_number NVARCHAR(20) NOT NULL,            -- Số ghế/mã vị trí (VD: A01, A02, B01, B02)
    seat_type NVARCHAR(30) NOT NULL DEFAULT 'STANDARD', -- STANDARD, VIP, EXTRA
    floor_number INT NOT NULL DEFAULT 1,          -- Tầng 1 hoặc Tầng 2 (với xe 2 tầng)
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_seats PRIMARY KEY (id),
    CONSTRAINT UQ_seats_bus_seat UNIQUE (bus_id, seat_number),
    CONSTRAINT CK_seats_floor CHECK (floor_number IN (1, 2)),
    CONSTRAINT FK_seats_buses FOREIGN KEY (bus_id) 
        REFERENCES buses(id) ON DELETE CASCADE ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 8. BẢNG: tickets (Vé xe)
-- Quan hệ:
--  - N-1 với trips: Vé thuộc về chuyến xe cụ thể.
--  - N-1 với seats: Vé giữ chỗ cho một ghế cụ thể trong chuyến.
--  - N-1 với users: Khách hàng đặt vé (có thể NULL nếu khách vãng lai mua tại quầy).
-- Cột 'version': Sử dụng cho Optimistic Locking (@Version trong JPA) tránh bán trùng ghế khi 2 người cùng đặt.
-- -----------------------------------------------------------------------------------------
CREATE TABLE tickets (
    id BIGINT IDENTITY(1,1),
    trip_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    user_id BIGINT NULL,                          -- Có thể null nếu khách không đăng ký tài khoản
    customer_name NVARCHAR(100) NOT NULL,
    customer_phone NVARCHAR(20) NOT NULL,
    customer_email NVARCHAR(100) NULL,
    ticket_code NVARCHAR(50) NOT NULL,            -- Mã vé tra cứu (Unique)
    price DECIMAL(18,2) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, EXPIRED
    version BIGINT NOT NULL DEFAULT 0,            -- Phục vụ Optimistic Locking chống Race Condition
    booking_time DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_tickets PRIMARY KEY (id),
    CONSTRAINT UQ_tickets_code UNIQUE (ticket_code),
    CONSTRAINT CK_tickets_price CHECK (price >= 0),
    CONSTRAINT CK_tickets_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT FK_tickets_trips FOREIGN KEY (trip_id) 
        REFERENCES trips(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_tickets_seats FOREIGN KEY (seat_id) 
        REFERENCES seats(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_tickets_users FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 9. BẢNG: maintenance_logs (Nhật ký sửa chữa, bảo dưỡng xe)
-- Quan hệ:
--  - N-1 với buses: Theo dõi lịch sử bảo trì kỹ thuật của từng xe.
-- -----------------------------------------------------------------------------------------
CREATE TABLE maintenance_logs (
    id BIGINT IDENTITY(1,1),
    bus_id BIGINT NOT NULL,                       -- Xe cần bảo dưỡng
    start_time DATETIME2 NOT NULL,                -- Thời gian bắt đầu bảo dưỡng
    end_time DATETIME2 NULL,                      -- Thời gian hoàn thành (NULL nếu đang sửa)
    maintenance_type NVARCHAR(50) NOT NULL,       -- PERIODIC (Định kỳ), REPAIR (Sửa chữa), INSPECTION (Kiểm định)
    cost DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    description NVARCHAR(500) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, CANCELLED
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_maintenance_logs PRIMARY KEY (id),
    CONSTRAINT CK_maintenance_cost CHECK (cost >= 0),
    CONSTRAINT CK_maintenance_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT FK_maintenance_logs_buses FOREIGN KEY (bus_id) 
        REFERENCES buses(id) ON DELETE CASCADE ON UPDATE NO ACTION
);
GO

-- -----------------------------------------------------------------------------------------
-- 10. BẢNG: payments (Thanh toán giao dịch vé)
-- Quan hệ:
--  - N-1 với tickets: Mỗi giao dịch thanh toán gắn với vé xe được đặt.
-- -----------------------------------------------------------------------------------------
CREATE TABLE payments (
    id BIGINT IDENTITY(1,1),
    ticket_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    payment_method NVARCHAR(50) NOT NULL,         -- VNPAY, MOMO, BANK_TRANSFER, CASH
    transaction_code NVARCHAR(100) NULL,          -- Mã phản hồi từ cổng thanh toán
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, REFUNDED
    payment_time DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_payments PRIMARY KEY (id),
    CONSTRAINT CK_payments_amount CHECK (amount >= 0),
    CONSTRAINT CK_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT FK_payments_tickets FOREIGN KEY (ticket_id) 
        REFERENCES tickets(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
GO

-- =========================================================================================
-- ĐÁNH CHỈ MỤC (INDEXES) TỐI ƯU HÓA HIỆU NĂNG THEO YÊU CẦU
-- =========================================================================================

-- 1. Index cho bảng trips: Tối ưu tra cứu chuyến xe theo tuyến đường và thời gian xuất phát
CREATE NONCLUSTERED INDEX idx_trips_departure_route 
ON trips(departure_time, route_id)
INCLUDE (bus_id, base_price, status);
GO

-- 2. Index cho bảng tickets: Tối ưu kiểm tra tình trạng chỗ ngồi (ghế đã được đặt trong chuyến hay chưa)
CREATE NONCLUSTERED INDEX idx_tickets_trip_seat 
ON tickets(trip_id, seat_id)
INCLUDE (status, ticket_code, price);
GO

-- 3. Index cho bảng maintenance_logs: Tối ưu tra cứu kiểm tra xe có đang bảo dưỡng trong khoảng thời gian nào
CREATE NONCLUSTERED INDEX idx_maintenance_bus_time 
ON maintenance_logs(bus_id, start_time, end_time)
INCLUDE (status, maintenance_type);
GO

-- =========================================================================================
-- DỮ LIỆU KHỞI TẠO MẪU (SEED DATA)
-- =========================================================================================

-- 1. Khởi tạo roles
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (id, name, description, created_at)
VALUES 
    (1, N'ROLE_ADMIN', N'Quản trị viên toàn hệ sinh thái', SYSDATETIME()),
    (2, N'ROLE_OPERATOR', N'Nhân viên / Quản lý đối tác nhà xe', SYSDATETIME()),
    (3, N'ROLE_CUSTOMER', N'Hành khách sử dụng dịch vụ đặt vé', SYSDATETIME());
SET IDENTITY_INSERT roles OFF;
GO

-- 2. Khởi tạo 2 nhà xe mẫu (bus_operators)
SET IDENTITY_INSERT bus_operators ON;
INSERT INTO bus_operators (id, name, code, contact_email, contact_phone, address, status, created_at, updated_at)
VALUES 
    (1, N'Công ty Cổ phần Xe khách Phương Trang (FUTA Bus Lines)', N'FUTA', N'hotro@futa.vn', N'19006067', N'Số 01 Tô Hiến Thành, Phường 3, TP. Đà Lạt, Lâm Đồng', N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
    (2, N'Công ty TNHH Vận tải Hoàng Long (Hoàng Long Asia)', N'HOANGLONG', N'lienhe@hoanglongasia.com', N'02253920920', N'Số 05 Phạm Ngũ Lão, Lương Khánh Thiện, Ngô Quyền, Hải Phòng', N'ACTIVE', SYSDATETIME(), SYSDATETIME());
SET IDENTITY_INSERT bus_operators OFF;
GO
