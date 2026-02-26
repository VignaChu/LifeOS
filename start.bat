@echo off
chcp 65001 >nul
echo ==========================================
echo     LifeOS Startup Script
echo ==========================================
echo.

echo [1/4] Checking environment...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Java not found, please install JDK 17+
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Maven not found, please install Maven
    pause
    exit /b 1
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Node.js not found, please install Node.js 18+
    pause
    exit /b 1
)

echo [OK] Environment check passed
echo.

echo [2/4] Checking Redis...
netstat -an | findstr "6379" >nul 2>&1
if %errorlevel% neq 0 (
    echo [*] Redis not running, attempting to start...
    
    where redis-server >nul 2>&1
    if %errorlevel% equ 0 (
        start "Redis Server" cmd /k "redis-server"
        echo [OK] Redis server starting... (port 6379)
        timeout /t 3 /nobreak >nul
    ) else (
        echo [!] Redis server not found in PATH
        echo [!] Please install Redis or add it to PATH
        echo [!] Continuing without Redis (cache will be disabled)
    )
) else (
    echo [OK] Redis is already running (port 6379)
)
echo.

echo [3/4] Starting backend service...
cd lifeos-backend
start "LifeOS Backend" cmd /k "mvn spring-boot:run"
cd ..
echo [OK] Backend service starting... (port 8080)
echo.

echo [4/4] Starting frontend services...
cd lifeos-frontend
if not exist "node_modules" (
    echo [*] First run, installing dependencies for main frontend...
    call npm install
)
start "LifeOS Frontend" cmd /k "npm run dev"
cd ..
echo [OK] Main frontend starting... (port 5173)

cd lifeos-admin
if not exist "node_modules" (
    echo [*] First run, installing dependencies for admin frontend...
    call pnpm install
)
start "LifeOS Admin" cmd /k "pnpm dev"
cd ..
echo [OK] Admin frontend starting... (port 5174)
echo.

echo ==========================================
echo     Startup Complete!
echo ==========================================
echo.
echo Main Frontend: http://localhost:5173
echo Admin Panel:   http://localhost:5174
echo Backend:       http://localhost:8080
echo API Docs:      http://localhost:8080/doc.html
echo Redis:         localhost:6379 (if started)
echo.
echo Press any key to exit...
pause >nul
