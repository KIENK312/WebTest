-- =========================================================================================
-- SEED DATA: HỆ THỐNG QUẢN LÝ BÁN VÉ XE KHÁCH
-- Microsoft SQL Server (T-SQL)
-- Chuẩn hóa tài khoản khách hàng mẫu "Nguyễn Trung Kiên" và 2 hãng xe: Phương Trang & Thành Bưởi
-- =========================================================================================

-- 1. BẢO ĐẢM CÁC VAI TRÒ (ROLES) HỆ THỐNG TỒN TẠI
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_ADMIN', N'Quản trị viên toàn hệ sinh thái', SYSDATETIME());

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_OPERATOR')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_OPERATOR', N'Nhân viên / Điều hành nhà xe đối tác', SYSDATETIME());

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO roles (name, description, created_at) VALUES ('ROLE_CUSTOMER', N'Hành khách sử dụng dịch vụ đặt vé xe', SYSDATETIME());
GO

-- 2. KHỞI TẠO 2 HÃNG XE ĐỐI TÁC CHÍNH: PHƯƠNG TRANG (FUTA) & THÀNH BƯỞI (THANHBUOI)
IF NOT EXISTS (SELECT 1 FROM bus_operators WHERE code = 'FUTA')
BEGIN
    INSERT INTO bus_operators (name, code, contact_phone, contact_email, address, status, created_at, updated_at)
    VALUES (
        N'Công ty Cổ phần Xe khách Phương Trang (FUTA Bus Lines)',
        N'FUTA',
        N'19006067',
        N'hotro@futa.vn',
        N'Số 01 Tô Hiến Thành, Phường 3, TP. Đà Lạt, Lâm Đồng',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END;

IF NOT EXISTS (SELECT 1 FROM bus_operators WHERE code = 'HOANGLONG')
BEGIN
    INSERT INTO bus_operators (name, code, contact_phone, contact_email, address, status, created_at, updated_at)
    VALUES (
        N'Công ty TNHH Vận tải Hoàng Long (Hoàng Long Bus Lines)',
        N'HOANGLONG',
        N'02253920920',
        N'info@hoanglongasia.com',
        N'Số 05 Phạm Ngũ Lão, Lương Khánh Thiện, Ngô Quyền, Hải Phòng',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END;

-- Loại bỏ dữ liệu Thành Bưởi nếu còn sót lại
IF EXISTS (SELECT 1 FROM bus_operators WHERE code = 'THANHBUOI')
BEGIN
    DELETE FROM users WHERE operator_id IN (SELECT id FROM bus_operators WHERE code = 'THANHBUOI');
    DELETE FROM bus_operators WHERE code = 'THANHBUOI';
END;
GO

-- 3. KHỞI TẠO & CHUẨN HÓA TÀI KHOẢN KHÁCH HÀNG MẪU: NGUYỄN TRUNG KIÊN
-- Password BCrypt chuẩn của 'Customer@123': $2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6
DECLARE @RoleIdCustomer BIGINT = (SELECT TOP 1 id FROM roles WHERE name = 'ROLE_CUSTOMER');

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (
        @RoleIdCustomer,
        NULL,
        N'customer',
        N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', -- Customer@123
        N'Nguyễn Trung Kiên',
        N'kientrungnguyen@gmail.com',
        N'0987654321',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END
ELSE
BEGIN
    -- Cập nhật đồng bộ thông tin nếu tài khoản 'customer' đã tồn tại với tên cũ
    UPDATE users
    SET full_name = N'Nguyễn Trung Kiên',
        email = N'kientrungnguyen@gmail.com',
        phone_number = N'0987654321',
        status = N'ACTIVE',
        updated_at = SYSDATETIME()
    WHERE username = 'customer';
END;

-- Thêm tài khoản Khách hàng mới độc lập: customer_new
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer_new')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (
        @RoleIdCustomer,
        NULL,
        N'customer_new',
        N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', -- Customer@123
        N'Nguyễn Trung Kiên (Mới)',
        N'kientrungnguyen.new@gmail.com',
        N'0989999888',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END;

-- Dọn dẹp toàn bộ dữ liệu vé cũ của Customer Nguyễn Trung Kiên để giữ tài khoản sạch
DELETE FROM payments WHERE ticket_id IN (SELECT id FROM tickets WHERE user_id IN (SELECT id FROM users WHERE username = 'customer'));
DELETE FROM tickets WHERE user_id IN (SELECT id FROM users WHERE username = 'customer');
GO

-- 4. KHỞI TẠO TÀI KHOẢN ĐIỀU HÀNH CHO PHƯƠNG TRANG VÀ HOÀNG LONG
DECLARE @RoleIdOperator BIGINT = (SELECT TOP 1 id FROM roles WHERE name = 'ROLE_OPERATOR');
DECLARE @FutaId BIGINT = (SELECT TOP 1 id FROM bus_operators WHERE code = 'FUTA');
DECLARE @HoangLongId BIGINT = (SELECT TOP 1 id FROM bus_operators WHERE code = 'HOANGLONG');

-- Operator Phương Trang
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'operator_futa')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (
        @RoleIdOperator,
        @FutaId,
        N'operator_futa',
        N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', -- Operator@123
        N'Điều Hành Xe Phương Trang',
        N'operator@futa.vn',
        N'0902223344',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END;

-- Operator Hoàng Long
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'operator_hoanglong')
BEGIN
    INSERT INTO users (role_id, operator_id, username, password, full_name, email, phone_number, status, created_at, updated_at)
    VALUES (
        @RoleIdOperator,
        @HoangLongId,
        N'operator_hoanglong',
        N'$2a$10$w85Vp28iM7b4k5L5.jCgK.J8wT4.D8G27DYiB54rV2c3p1Ym1j9u6', -- Operator@123
        N'Điều Hành Xe Hoàng Long',
        N'operator@hoanglong.vn',
        N'0903334455',
        N'ACTIVE',
        SYSDATETIME(),
        SYSDATETIME()
    );
END;
GO
