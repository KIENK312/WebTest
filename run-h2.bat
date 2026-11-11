@echo off
chcp 65001 >nul
echo ========================================================================
echo   KHỞI CHẠY HỆ THỐNG VỚI H2 IN-MEMORY DATABASE (CHẠY TRONG RAM)
echo ========================================================================
echo.

if exist "%~dp0mvnw.cmd" (
    echo Đang khởi chạy bằng Maven Wrapper tích hợp...
    call "%~dp0mvnw.cmd" spring-boot:run "-Dspring-boot.run.profiles=h2"
) else (
    echo Đang khởi chạy bằng lệnh mvn hệ thống...
    call mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
)

pause
