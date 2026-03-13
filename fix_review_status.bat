@echo off
chcp 65001 >nul
echo ============================================================================
echo FIX REVIEW STATUS CASE SENSITIVITY
echo ============================================================================
echo.
echo Buoc 1: Chay SQL fix trong MySQL...
echo.
mysql -u root -p example201 < "%~dp0src\main\resources\db\migration\V2__fix_review_status_case.sql"
echo.
echo ============================================================================
echo Kiem tra ket qua...
echo ============================================================================
echo.
mysql -u root -p -e "SELECT status, COUNT(*) as count FROM reviews GROUP BY status;" example201
echo.
echo ============================================================================
echo Neu thay PENDING, APPROVED, etc. (chua hoa) la OK!
echo.
pause

