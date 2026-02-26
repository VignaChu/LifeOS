# LifeOS - AI原生生活追踪器

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue.js-3.4.0-4FC08D.svg" alt="Vue.js">
  <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java">
  <img src="https://img.shields.io/badge/Node.js-18+-blue.svg" alt="Node.js">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.0-red.svg" alt="Redis">
</p>

<p align="center">
  <b>AI驱动的智能生活记录与分析系统</b><br>
  通过自然语言与AI对话，自动追踪生活轨迹、管理情绪、分析消费习惯
</p>

---

## 📖 项目简介

LifeOS 是一个**AI原生**的生活追踪应用，核心理念是让用户以最自然的方式（对话）记录生活，由AI自动完成数据解析、分类、分析和可视化展示。

### 核心功能

- 🤖 **AI智能对话** - 像聊天一样记录生活，AI自动解析时间、地点、事件、情绪、消费等信息
- 📊 **多维数据分析** - 自动统计时间分配、情绪变化、消费结构，生成可视化图表
- 🔍 **自然语言查询** - 用日常语言查询历史记录，AI自动转换为SQL并返回结果
- 🎵 **情绪感知音乐** - 根据当前情绪自动推荐背景音乐，支持自定义歌单
- 🎨 **主题切换** - 10+精美主题，支持深浅色模式
- 📱 **响应式设计** - 适配桌面和移动端
- 🔐 **JWT认证** - 安全的用户认证与权限管理
- 🛠️ **管理后台** - 独立的管理员后台，支持数据管理、系统监控、LLM配置

---

## 🛠️ 技术栈

### 后端 (lifeos-backend)

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring AI | 1.0.0-M1 | AI大模型集成 |
| Spring Security Crypto | 6.2.0 | 密码加密 |
| MyBatis | 3.0.3 | ORM框架 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.0+ | 缓存与会话 |
| JWT | 0.12.3 | 身份认证 |
| Knife4j | 4.4.0 | API文档 |
| MapStruct | 1.5.5 | 对象映射 |
| Lombok | 1.18.30 | 代码简化 |
| HikariCP | 5.0.1 | 数据库连接池 |
| Jackson | 2.15.0 | JSON处理 |
| Maven | 3.8+ | 构建工具 |

### 前端 (lifeos-frontend)

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.4.0 | 前端框架 |
| Pinia | 2.1.7 | 状态管理 |
| Tailwind CSS | 3.4.0 | 样式框架 |
| Three.js | 0.183.1 | 3D情绪背景 |
| ECharts | 5.5.0 | 数据可视化 |
| VueUse | 10.9.0 | 工具库 |
| Ky | 1.2.0 | HTTP客户端 |
| Zod | 3.23.8 | 数据验证 |
| Marked | 17.0.3 | Markdown渲染 |
| date-fns | 3.6.0 | 日期处理 |
| Vite | 5.0.0 | 构建工具 |
| pnpm | 8.0+ | 包管理器 |

### 管理后台 (lifeos-admin)

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.4.0 | 前端框架 |
| Element Plus | 2.5.6 | UI组件库 |
| Element Plus Icons | 2.3.1 | 图标库 |
| Vue Router | 4.3.0 | 路由管理 |
| Axios | 1.6.7 | HTTP客户端 |
| ECharts | 5.5.0 | 数据可视化 |
| Pinia | 2.1.7 | 状态管理 |
| Vite | 5.0.0 | 构建工具 |

---

## 🚀 快速上手

### 环境依赖

- **JDK** 17+
- **Node.js** 18+
- **MySQL** 8.0+
- **Redis** 7.0+ (可选，用于缓存)
- **Maven** 3.8+

### 1. 克隆仓库

```bash
git clone https://github.com/yourusername/LifeOS.git
cd LifeOS
```

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE lifeos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行初始化脚本
USE lifeos;
SOURCE lifeos-backend/database-init.sql;
```

### 3. 后端配置与启动

```bash
cd lifeos-backend

# 修改数据库配置
# 编辑 src/main/resources/application.yml
# 修改 spring.datasource.username 和 password

# 编译运行
mvn clean package
java -jar target/lifeos-backend-1.0.0.jar
```

后端服务默认运行在 `http://localhost:8080`

### 4. 前端配置与启动

```bash
cd lifeos-frontend

# 安装依赖
pnpm install

# 开发模式启动
pnpm run dev
```

前端服务默认运行在 `http://localhost:5173`

### 5. 管理后台启动

```bash
cd lifeos-admin

# 安装依赖
pnpm install

# 开发模式启动
pnpm run dev
```

管理后台默认运行在 `http://localhost:5174`

---

## 📁 项目结构

```
LifeOS/
├── lifeos-backend/          # 后端服务
│   ├── src/main/java/
│   │   └── com/lifeos/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # API控制器
│   │       ├── service/     # 业务逻辑
│   │       ├── mapper/      # 数据访问层
│   │       ├── entity/      # 实体类
│   │       └── dto/         # 数据传输对象
│   ├── src/main/resources/
│   │   ├── application.yml  # 主配置文件
│   │   └── static/          # 静态资源
│   └── database-init.sql    # 数据库初始化脚本
│
├── lifeos-frontend/         # 用户前端
│   ├── src/
│   │   ├── components/      # 组件
│   │   ├── views/           # 页面
│   │   ├── store/           # Pinia状态管理
│   │   ├── api/             # API封装
│   │   └── utils/           # 工具函数
│   └── package.json
│
├── lifeos-admin/            # 管理后台
│   ├── src/
│   │   ├── views/           # 管理页面
│   │   ├── api/             # API封装
│   │   └── router/          # 路由配置
│   └── package.json
│
└── README.md
```

---

## 🔌 API 文档

项目集成了 Knife4j 自动生成 API 文档：

- **本地环境**: http://localhost:8080/doc.html
- **开发文档**: 包含所有接口的详细说明、参数、响应示例

### 核心接口概览

| 接口 | 说明 |
|------|------|
| `POST /api/track` | 追踪生活记录（AI解析） |
| `POST /api/query` | 自然语言查询 |
| `POST /api/statistics` | 获取统计数据 |
| `GET /api/music/playlists` | 获取音乐歌单 |
| `POST /api/auth/login` | 用户登录 |
| `POST /api/auth/register` | 用户注册 |

---

## 🎨 功能展示

### AI对话记录

用户通过自然语言输入，AI自动解析并存储结构化数据：

```
用户: 今天下午3点去星巴克喝了杯拿铁，花了35块，心情还不错
AI: 已记录您的消费和心情 ✓
   📍 地点: 星巴克
   🕐 时间: 2026-02-26 15:00
   💰 消费: ¥35 (餐饮)
   😊 情绪: 积极 (7/10)
```

### 数据分析看板

- 📈 情绪趋势图 - 追踪情绪变化轨迹
- 💰 消费分析 - 按类别统计支出
- ⏰ 时间分布 - 活动类型占比
- 🏷️ 标签云 - 高频活动关键词

### 情绪感知音乐

- 根据情绪分数自动匹配背景音乐歌单
- 支持自定义歌单和强制播放模式
- 精美的播放器UI，支持专辑封面旋转效果

---

## ⚙️ 配置说明

### 后端配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lifeos?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password

# Redis配置（可选）
  data:
    redis:
      host: localhost
      port: 6379

# LLM配置（AI功能必需）
llm:
  provider: openai  # 或 azure, claude, gemini
  api-key: your_api_key
  api-url: https://api.openai.com/v1/chat/completions
  model: gpt-3.5-turbo
```

### 前端配置 (.env)

```bash
# API基础地址
VITE_API_BASE_URL=http://localhost:8080

# 开发模式
VITE_DEV_MODE=true
```

---

## ❓ 常见问题

### Q: 前端无法连接到后端？

**A**: 检查以下几点：
1. 后端服务是否已启动 (`http://localhost:8080`)
2. 前端 `.env` 文件中的 `VITE_API_BASE_URL` 配置是否正确
3. 浏览器控制台是否有 CORS 跨域错误（后端已配置CORS，一般不会有问题）

### Q: AI功能无法使用？

**A**: 
1. 确认已在 `application.yml` 中配置了有效的 LLM API Key
2. 检查网络是否能访问对应的AI服务
3. 查看后端日志中的错误信息

### Q: 数据库连接失败？

**A**:
1. 确认MySQL服务已启动
2. 检查用户名密码是否正确
3. 确认 `lifeos` 数据库已创建
4. 检查MySQL版本是否为 8.0+

### Q: 如何修改默认端口？

**A**:
- 后端: 修改 `application.yml` 中的 `server.port`
- 前端: 修改 `vite.config.js` 中的 `server.port`

---

## 📝 开发计划

- [x] 核心AI对话功能
- [x] 数据追踪与存储
- [x] 统计分析与可视化
- [x] 用户认证系统
- [x] 管理后台
- [x] 情绪感知音乐
- [x] 主题切换
- [ ] 移动端App
- [ ] 数据导出PDF
- [ ] 社交分享功能

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 开源协议

本项目基于 [MIT](LICENSE) 协议开源。

---

## 🙏 致谢

### 核心框架
- [Spring AI](https://spring.io/projects/spring-ai) - AI集成框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [MyBatis](https://mybatis.org/mybatis-3/) - ORM框架

### UI与样式
- [Tailwind CSS](https://tailwindcss.com/) - 样式框架
- [Element Plus](https://element-plus.org/) - Vue组件库
- [Three.js](https://threejs.org/) - 3D图形库
- [ECharts](https://echarts.apache.org/) - 数据可视化

### 工具与库
- [Pinia](https://pinia.vuejs.org/) - 状态管理
- [Vite](https://vitejs.dev/) - 构建工具
- [pnpm](https://pnpm.io/) - 包管理器
- [Ky](https://github.com/sindresorhus/ky) - HTTP客户端
- [Zod](https://zod.dev/) - 数据验证
- [MapStruct](https://mapstruct.org/) - 对象映射
- [Lombok](https://projectlombok.org/) - 代码简化

### 第三方服务
- [网易云音乐](https://music.163.com/) - 音乐数据源（仅供学习交流使用）
- [不蒜子 (Busuanzi)](https://busuanzi.ibruce.info/) - 网站访问统计
- [Meting API](https://github.com/metowolf/Meting) - 网易云音乐API解析服务

---

<p align="center">
  Made with ❤️ by LifeOS Team
</p>
