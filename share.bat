@echo off
chcp 65001 >nul
echo ========================================================================
echo   BUS TICKET PRO - CHIA SẺ TRANG WEB CHO BẠN BÈ (100%% MIỄN PHÍ)
echo ========================================================================
echo.
echo Đang tạo đường link công khai có bảo mật HTTPS qua Cloudflare Tunnel...
echo Vui lòng đợi trong giây lát...
echo.
"C:\Program Files (x86)\cloudflared\cloudflared.exe" tunnel --url http://localhost:8080
pause
