@echo off
chcp 65001 >nul
echo ========================================================================
echo   KHỞI CHẠY HỆ THỐNG VỚI H2 IN-MEMORY DATABASE (CHẠY TRONG RAM)
echo ========================================================================
echo.
echo Đang gọi Maven từ IntelliJ IDEA...
echo.

set "MVN_PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

if exist "%MVN_PATH%" (
    "%MVN_PATH%" spring-boot:run "-Dspring-boot.run.profiles=h2"
) else (
    mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
)

pause
