@echo off
chcp 65001 >nul
echo ========================================================================
echo   KHỞI CHẠY HỆ THỐNG VỚI MICROSOFT SQL SERVER
echo ========================================================================
echo.

if exist "%~dp0mvnw.cmd" (
    echo Đang khởi chạy bằng Maven Wrapper tích hợp...
    call "%~dp0mvnw.cmd" spring-boot:run
) else (
    echo Đang khởi chạy bằng lệnh mvn hệ thống...
    call mvn spring-boot:run
)

pause
