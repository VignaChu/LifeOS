@echo off
chcp 65001 >nul
title LifeOS 一键部署脚本

:: ============================================
:: LifeOS 一键部署脚本
:: 支持自定义端口和MySQL账号密码
:: ============================================

echo.
echo ============================================
echo    LifeOS - AI原生生活追踪器
echo    一键部署脚本
echo ============================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [错误] 请以管理员身份运行此脚本!
    echo 右键点击脚本，选择"以管理员身份运行"
    pause
    exit /b 1
)

:: ============================================
:: 配置输入
:: ============================================
echo [1/5] 配置数据库连接...
echo.

set /p MYSQL_HOST="MySQL主机地址 (默认: localhost): "
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

set /p MYSQL_PORT="MySQL端口 (默认: 3306): "
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

set /p MYSQL_USER="MySQL用户名 (默认: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="MySQL密码: "
if "%MYSQL_PASS%"=="" (
    echo [错误] 密码不能为空!
    pause
    exit /b 1
)

echo.
echo [2/5] 配置服务端口...
echo.

set /p BACKEND_PORT="后端服务端口 (默认: 8080): "
if "%BACKEND_PORT%"=="" set BACKEND_PORT=8080

set /p FRONTEND_PORT="前端服务端口 (默认: 5173): "
if "%FRONTEND_PORT%"=="" set FRONTEND_PORT=5173

set /p ADMIN_PORT="管理后台端口 (默认: 5174): "
if "%ADMIN_PORT%"=="" set ADMIN_PORT=5174

echo.
echo ============================================
echo    配置确认
:: ============================================
echo    MySQL: %MYSQL_USER%@%MYSQL_HOST%:%MYSQL_PORT%
echo    后端端口: %BACKEND_PORT%
echo    前端端口: %FRONTEND_PORT%
echo    管理后台端口: %ADMIN_PORT%
echo ============================================
echo.

set /p CONFIRM="确认开始部署? (Y/N): "
if /I not "%CONFIRM%"=="Y" (
    echo 部署已取消
    pause
    exit /b 0
)

:: ============================================
:: 步骤1: 检查环境
:: ============================================
echo.
echo [3/5] 检查环境依赖...
echo.

:: 检查Java
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [错误] 未检测到Java! 请先安装JDK 17+
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java已安装

:: 检查Maven
mvn -version >nul 2>&1
if %errorLevel% neq 0 (
    echo [错误] 未检测到Maven! 请先安装Maven 3.8+
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [OK] Maven已安装

:: 检查Node.js
node -v >nul 2>&1
if %errorLevel% neq 0 (
    echo [错误] 未检测到Node.js! 请先安装Node.js 18+
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)
echo [OK] Node.js已安装

:: 检查MySQL
mysql -V >nul 2>&1
if %errorLevel% neq 0 (
    echo [警告] 未检测到MySQL客户端! 请确保MySQL已安装
)
echo [OK] 环境检查完成

:: ============================================
:: 步骤2: 初始化数据库
:: ============================================
echo.
echo [4/5] 初始化数据库...
echo.

:: 测试数据库连接
echo 测试数据库连接...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% -e "SELECT 1;" >nul 2>&1
if %errorLevel% neq 0 (
    echo [错误] 数据库连接失败! 请检查:
    echo   - MySQL服务是否启动
    echo   - 主机地址、端口、用户名、密码是否正确
    pause
    exit /b 1
)
echo [OK] 数据库连接成功

:: 执行初始化脚本
echo 创建数据库和表...
cd /d "%~dp0\lifeos-backend"
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% < database-init.sql
if %errorLevel% neq 0 (
    echo [错误] 数据库初始化失败!
    pause
    exit /b 1
)
echo [OK] 数据库初始化完成

:: ============================================
:: 步骤3: 修改配置文件
:: ============================================
echo.
echo 更新后端配置文件...
echo.

:: 备份原配置
copy /Y "src\main\resources\application.yml" "src\main\resources\application.yml.bak" >nul 2>&1

:: 使用PowerShell修改配置
powershell -Command "
    $content = Get-Content 'src\main\resources\application.yml' -Raw
    $content = $content -replace 'port: 8080', 'port: %BACKEND_PORT%'
    $content = $content -replace 'url: jdbc:mysql://localhost:3306/', 'url: jdbc:mysql://%MYSQL_HOST%:%MYSQL_PORT%/'
    $content = $content -replace 'username: root', 'username: %MYSQL_USER%'
    $content = $content -replace 'password: 114514', 'password: %MYSQL_PASS%'
    Set-Content 'src\main\resources\application.yml' $content -NoNewline
"
echo [OK] 后端配置已更新

:: ============================================
:: 步骤4: 构建后端
:: ============================================
echo.
echo 构建后端服务...
echo.

call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo [错误] 后端构建失败!
    pause
    exit /b 1
)
echo [OK] 后端构建完成

:: ============================================
:: 步骤5: 安装前端依赖
:: ============================================
echo.
echo [5/5] 安装前端依赖...
echo.

:: 用户前端
cd /d "%~dp0\lifeos-frontend"
echo 安装用户前端依赖...
call npm install
if %errorLevel% neq 0 (
    echo [错误] 用户前端依赖安装失败!
    pause
    exit /b 1
)
echo [OK] 用户前端依赖安装完成

:: 管理后台
cd /d "%~dp0\lifeos-admin"
echo 安装管理后台依赖...
call npm install
if %errorLevel% neq 0 (
    echo [错误] 管理后台依赖安装失败!
    pause
    exit /b 1
)
echo [OK] 管理后台依赖安装完成

:: ============================================
:: 创建启动脚本
:: ============================================
echo.
echo 创建启动脚本...
echo.

cd /d "%~dp0"

:: 创建启动后端脚本
echo @echo off > start-backend.bat
echo chcp 65001 ^>nul >> start-backend.bat
echo title LifeOS 后端服务 >> start-backend.bat
echo cd /d "%%~dp0\lifeos-backend" >> start-backend.bat
echo java -jar target\lifeos-backend-1.0.0.jar >> start-backend.bat

:: 创建启动前端脚本
echo @echo off > start-frontend.bat
echo chcp 65001 ^>nul >> start-frontend.bat
echo title LifeOS 前端服务 >> start-frontend.bat
echo cd /d "%%~dp0\lifeos-frontend" >> start-frontend.bat
echo npm run dev -- --port %FRONTEND_PORT% >> start-frontend.bat

:: 创建启动管理后台脚本
echo @echo off > start-admin.bat
echo chcp 65001 ^>nul >> start-admin.bat
echo title LifeOS 管理后台 >> start-admin.bat
echo cd /d "%%~dp0\lifeos-admin" >> start-admin.bat
echo npm run dev -- --port %ADMIN_PORT% >> start-admin.bat

:: 创建一键启动所有服务脚本
echo @echo off > start-all.bat
echo chcp 65001 ^>nul >> start-all.bat
echo title LifeOS 全部服务 >> start-all.bat
echo echo 正在启动 LifeOS 服务... >> start-all.bat
echo echo. >> start-all.bat
echo echo 后端服务: http://localhost:%BACKEND_PORT% >> start-all.bat
echo echo 用户前端: http://localhost:%FRONTEND_PORT% >> start-all.bat
echo echo 管理后台: http://localhost:%ADMIN_PORT% >> start-all.bat
echo echo. >> start-all.bat
echo start "LifeOS Backend" "%%~dp0\start-backend.bat" >> start-all.bat
echo timeout /t 5 ^>nul >> start-all.bat
echo start "LifeOS Frontend" "%%~dp0\start-frontend.bat" >> start-all.bat
echo timeout /t 2 ^>nul >> start-all.bat
echo start "LifeOS Admin" "%%~dp0\start-admin.bat" >> start-all.bat
echo echo. >> start-all.bat
echo echo 所有服务已启动! >> start-all.bat
echo pause >> start-all.bat

echo [OK] 启动脚本创建完成

:: ============================================
:: 部署完成
:: ============================================
echo.
echo ============================================
echo    部署完成!
echo ============================================
echo.
echo 访问地址:
echo   后端API:  http://localhost:%BACKEND_PORT%
echo   用户前端: http://localhost:%FRONTEND_PORT%
echo   管理后台: http://localhost:%ADMIN_PORT%
echo.
echo 启动方式:
echo   1. 双击 start-all.bat    - 启动所有服务
echo   2. 双击 start-backend.bat - 仅启动后端
echo   3. 双击 start-frontend.bat - 仅启动用户前端
echo   4. 双击 start-admin.bat   - 仅启动管理后台
echo.
echo 默认管理员账号:
echo   用户名: admin
echo   密码: admin123
echo.
echo ============================================
echo.

pause
