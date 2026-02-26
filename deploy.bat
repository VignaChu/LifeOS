@echo off
chcp 65001 >nul
title LifeOS One-Click Deployment Script

:: ============================================
:: LifeOS One-Click Deployment Script
:: Supports custom ports and MySQL credentials
:: ============================================

echo.
echo ============================================
echo    LifeOS - AI Native Life Tracker
echo    One-Click Deployment Script
echo ============================================
echo.

:: Check administrator privileges
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [Error] Please run this script as administrator!
    echo Right-click the script and select "Run as administrator"
    pause
    exit /b 1
)

:: ============================================
:: Configuration input
:: ============================================
echo [1/5] Configuring database connection...
echo.

set /p MYSQL_HOST="MySQL Host (default: localhost): "
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

set /p MYSQL_PORT="MySQL Port (default: 3306): "
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

set /p MYSQL_USER="MySQL Username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="MySQL Password: "
if "%MYSQL_PASS%"=="" (
    echo [Error] Password cannot be empty!
    pause
    exit /b 1
)

echo.
echo [2/5] Configuring service ports...
echo.

set /p BACKEND_PORT="Backend service port (default: 8080): "
if "%BACKEND_PORT%"=="" set BACKEND_PORT=8080

set /p FRONTEND_PORT="Frontend service port (default: 5173): "
if "%FRONTEND_PORT%"=="" set FRONTEND_PORT=5173

set /p ADMIN_PORT="Admin panel port (default: 5174): "
if "%ADMIN_PORT%"=="" set ADMIN_PORT=5174

echo.
echo ============================================
echo    Configuration Confirmation
:: ============================================
echo    MySQL: %MYSQL_USER%@%MYSQL_HOST%:%MYSQL_PORT%
echo    Backend Port: %BACKEND_PORT%
echo    Frontend Port: %FRONTEND_PORT%
echo    Admin Panel Port: %ADMIN_PORT%
echo ============================================
echo.

set /p CONFIRM="Start deployment? (Y/N): "
if /I not "%CONFIRM%"=="Y" (
    echo Deployment cancelled
    pause
    exit /b 0
)

:: ============================================
:: Step 1: Check environment
:: ============================================
echo.
echo [3/5] Checking environment dependencies...
echo.

:: Check Java
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [Error] Java not found! Please install JDK 17+
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java is installed

:: Check Maven
mvn -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [Error] Maven not found! Please install Maven 3.8+
    echo Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [OK] Maven is installed

:: Check Node.js
node -v >nul 2>&1
if %errorLevel% neq 0 (
    echo [Error] Node.js not found! Please install Node.js 18+
    echo Download: https://nodejs.org/
    pause
    exit /b 1
)
echo [OK] Node.js is installed

:: Check MySQL
mysql -V >nul 2>&1
if %errorLevel% neq 0 (
    echo [Warning] MySQL client not found! Please ensure MySQL is installed
)
echo [OK] Environment check completed

:: ============================================
:: Step 2: Initialize database
:: ============================================
echo.
echo [4/5] Initializing database...
echo.

:: Test database connection
echo Testing database connection...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% -e "SELECT 1;" >nul 2>&1
if %errorLevel% neq 0 (
    echo [Error] Database connection failed! Please check:
    echo   - MySQL service is running
    echo   - Host address, port, username, and password are correct
    pause
    exit /b 1
)
echo [OK] Database connection successful

:: Execute initialization script
echo Creating database and tables...
cd /d "%~dp0\lifeos-backend"
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% < database-init.sql
if %errorLevel% neq 0 (
    echo [Error] Database initialization failed!
    pause
    exit /b 1
)
echo [OK] Database initialization completed

:: ============================================
:: Step 3: Update configuration files
:: ============================================
echo.
echo Updating backend configuration...
echo.

:: Backup original configuration
copy /Y "src\main\resources\application.yml" "src\main\resources\application.yml.bak" >nul 2>&1

:: Use PowerShell to modify configuration
powershell -Command "
    $content = Get-Content 'src\main\resources\application.yml' -Raw
    $content = $content -replace 'port: 8080', 'port: %BACKEND_PORT%'
    $content = $content -replace 'url: jdbc:mysql://localhost:3306/', 'url: jdbc:mysql://%MYSQL_HOST%:%MYSQL_PORT%/'
    $content = $content -replace 'username: root', 'username: %MYSQL_USER%'
    $content = $content -replace 'password: 114514', 'password: %MYSQL_PASS%'
    Set-Content 'src\main\resources\application.yml' $content -NoNewline
"
echo [OK] Backend configuration updated

:: ============================================
:: Step 4: Build backend
:: ============================================
echo.
echo Building backend service...
echo.

call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo [Error] Backend build failed!
    pause
    exit /b 1
)
echo [OK] Backend build completed

:: ============================================
:: Step 5: Install frontend dependencies
:: ============================================
echo.
echo [5/5] Installing frontend dependencies...
echo.

:: Main frontend
cd /d "%~dp0\lifeos-frontend"
echo Installing main frontend dependencies...
call npm install
if %errorLevel% neq 0 (
    echo [Error] Main frontend dependency installation failed!
    pause
    exit /b 1
)
echo [OK] Main frontend dependencies installed

:: Admin panel
cd /d "%~dp0\lifeos-admin"
echo Installing admin panel dependencies...
call pnpm install
if %errorLevel% neq 0 (
    echo [Error] Admin panel dependency installation failed!
    pause
    exit /b 1
)
echo [OK] Admin panel dependencies installed

:: ============================================
:: Create startup scripts
:: ============================================
echo.
echo Creating startup scripts...
echo.

cd /d "%~dp0"

:: Create backend startup script
echo @echo off > start-backend.bat
echo chcp 65001 ^>nul >> start-backend.bat
echo title LifeOS Backend Service >> start-backend.bat
echo cd /d "%%~dp0\lifeos-backend" >> start-backend.bat
echo java -jar target\lifeos-backend-1.0.0.jar >> start-backend.bat

:: Create frontend startup script
echo @echo off > start-frontend.bat
echo chcp 65001 ^>nul >> start-frontend.bat
echo title LifeOS Frontend Service >> start-frontend.bat
echo cd /d "%%~dp0\lifeos-frontend" >> start-frontend.bat
echo npm run dev -- --port %FRONTEND_PORT% >> start-frontend.bat

:: Create admin panel startup script
echo @echo off > start-admin.bat
echo chcp 65001 ^>nul >> start-admin.bat
echo title LifeOS Admin Panel >> start-admin.bat
echo cd /d "%%~dp0\lifeos-admin" >> start-admin.bat
echo pnpm dev --port %ADMIN_PORT% >> start-admin.bat

:: Create one-click startup script for all services
echo @echo off > start-all.bat
echo chcp 65001 ^>nul >> start-all.bat
echo title LifeOS All Services >> start-all.bat
echo echo Starting LifeOS services... >> start-all.bat
echo echo. >> start-all.bat
echo echo Backend API: http://localhost:%BACKEND_PORT% >> start-all.bat
echo echo Main Frontend: http://localhost:%FRONTEND_PORT% >> start-all.bat
echo echo Admin Panel: http://localhost:%ADMIN_PORT% >> start-all.bat
echo echo. >> start-all.bat
echo start "LifeOS Backend" "%%~dp0\start-backend.bat" >> start-all.bat
echo timeout /t 5 ^>nul >> start-all.bat
echo start "LifeOS Frontend" "%%~dp0\start-frontend.bat" >> start-all.bat
echo timeout /t 2 ^>nul >> start-all.bat
echo start "LifeOS Admin" "%%~dp0\start-admin.bat" >> start-all.bat
echo echo. >> start-all.bat
echo echo All services started! >> start-all.bat
echo pause >> start-all.bat

echo [OK] Startup scripts created

:: ============================================
:: Deployment completed
:: ============================================
echo.
echo ============================================
echo    Deployment Completed!
echo ============================================
echo.
echo Access URLs:
echo   Backend API:  http://localhost:%BACKEND_PORT%
echo   Main Frontend: http://localhost:%FRONTEND_PORT%
echo   Admin Panel:  http://localhost:%ADMIN_PORT%
echo.
echo Startup Methods:
echo   1. Double-click start-all.bat    - Start all services
echo   2. Double-click start-backend.bat - Start backend only
echo   3. Double-click start-frontend.bat - Start main frontend only
echo   4. Double-click start-admin.bat   - Start admin panel only
echo.
echo Default Admin Account:
echo   Username: admin
echo   Password: admin123
echo.
echo ============================================
echo.

pause
