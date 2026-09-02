# 数字存证 DApp

<p align="center">
  <strong>基于 FISCO-BCOS 的区块链数字存证平台</strong><br/>
  安全 · 不可篡改 · 可追溯 · 可验证
</p>

<p align="center">
  <a href="https://github.com/FISCO-BCOS/FISCO-BCOS"><img src="https://img.shields.io/badge/FISCO--BCOS-2.9.1-blue" alt="FISCO-BCOS"></a>
  <a href="https://github.com/spring-projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F" alt="Spring Boot"></a>
  <a href="https://github.com/vuejs/vue"><img src="https://img.shields.io/badge/Vue.js-2.6-42b883" alt="Vue.js"></a>
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#api-接口">API 接口</a> •
  <a href="#智能合约">智能合约</a> •
  <a href="#数据库设计">数据库设计</a>
</p>

---

## 功能特性

### 核心业务

| 模块 | 功能 | 说明 |
|------|------|------|
| **用户系统** | 注册 / 登录 / 个人中心 | 支持个人、企业、机构三种角色；双 Token 认证（Access Token + Refresh Token） |
| **存证管理** | 新建存证 / 存证列表 / 详情查看 | 支持多文件上传、SHA256 哈希计算、分类管理（图片/文档/音频/视频/代码） |
| **区块链上链** | 提交上链 / 单条重试上链 | 通过 WeBASE-Front 中间件将存证写入 FISCO-BCOS 链，记录交易 Hash 与区块高度 |
| **存证验证** | 哈希验证 / 文件验证 | 输入哈希值或上传文件，与链上数据比对，生成验证记录 |
| **数据统计** | 存证总量 / 用户数 / 上链数 / 验证次数 | 首页仪表盘展示，定时从链上同步最新区块高度 |

### 安全设计

- **双 Token 认证体系**：短期 Access Token（5 分钟，内存存储）+ 长期 Refresh Token（7 天，HttpOnly Cookie），静默刷新无感续期
- **密码安全**：BCrypt 加密存储
- **文件完整性**：SHA256 哈希校验，确保存证文件未被篡改
- **链上存证**：数据写入 FISCO-BCOS 区块链，具备法律效力

## 技术架构

```
┌──────────────────────────────────────────────────────────────┐
│                        前端 (Frontend)                        │
│  Vue 2 + Element UI + Vue Router + Axios                     │
│  端口: 8020                                                   │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP (Proxy /api → :8080)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                       后端 (Backend)                          │
│  Spring Boot 2.7.18 + MyBatis-Plus + JWT                    │
│  端口: 8080                                                   │
│                                                               │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌───────────┐   │
│  │ Auth     │  │ Evidence  │  │ User     │  │ Statistics │   │
│  │ 认证模块  │  │ 存证模块   │  │ 用户模块  │  │ 统计模块   │   │
│  └────┬─────┘  └─────┬─────┘  └────┬─────┘  └─────┬─────┘   │
│       │              │             │              │          │
│  ┌────▼──────────────▼─────────────▼──────────────▼─────┐    │
│  │              MySQL 8.0 (evidence_dapp)               │    │
│  │   sys_user / evidence_record / work_file / verify_   │    │
│  └──────────────────────────┬──────────────────────────┘    │
└─────────────────────────────┼───────────────────────────────┘
                              │ HTTP (WeBASE-Front API)
                              ▼
┌──────────────────────────────────────────────────────────────┐
│                   FISCO-BCOS 区块链网络                        │
│  节点: <your-node-ip>:20200  |  群组: <group-id>            │
│  合约: EvidenceContract (<deployed-address>)                 │
│  中间件: WeBASE-Front (:5002)                                │
└──────────────────────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前端框架** | Vue.js | 2.6 |
| **UI 组件库** | Element UI | 2.15 |
| **HTTP 客户端** | Axios | 0.21 |
| **路由** | Vue Router | 3.5 |
| **后端框架** | Spring Boot | 2.7.18 |
| **ORM** | MyBatis-Plus | 3.5.3 |
| **数据库** | MySQL | 8.0+ |
| **区块链** | FISCO-BCOS | 2.9.1 |
| **Java SDK** | fisco-bcos-java-sdk | 2.9.1 |
| **中间件** | WeBASE-Front | - |
| **认证** | JWT (auth0/java-jwt) | 4.4.0 |
| **工具库** | Hutool | 5.8.23 |

## 快速开始

### 环境要求

- JDK 21+
- Node.js 14+
- Maven 3.6+
- MySQL 8.0+
- FISCO-BCOS 节点（2.9.1+）及 WeBASE-Front 中间件

### 1. 部署区块链环境

参考 [FISCO-BCOS 官方文档](https://fisco-bcos-doc.readthedocs.io/) 搭建链环境：

```bash
# 搭建单群组 4 节点联盟链
bash build_chain.sh -l 127.0.0.1:4 -p 30300,20200,8545

# 启动节点
bash nodes/127.0.0.1/start_all.sh

# 部署 WeBASE-Front（用于应用层与区块链交互）
# 配置 WeBASE-Front 连接至节点 :20200 端口
```

部署 `contracts/EvidenceContract.sol` 合约并记录合约地址。

### 2. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE evidence_dapp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 导入初始化脚本（包含 sys_user / evidence_record / work_file / verify_record 四张表）
source evidence-backend/src/main/resources/sql/init.sql;
```

### 3. 启动后端

```bash
cd evidence-backend

# 修改配置文件 application.yml 中的以下项：
#   - spring.datasource: 数据库连接信息（地址/端口/用户名/密码）
#   - webase.url: WeBASE-Front 地址
#   - fisco.bcos.node-url: 区块链节点地址
#   - fisco.bcos.group-id: 群组 ID
#   - fisco.bcos.contract.evidence-address: 已部署的合约地址
#   - jwt.secret: JWT 密钥（生产环境请务必更换为强密钥）

# 编译运行
mvn spring-boot:run -DskipTests
```

后端启动成功后访问：`http://localhost:8080`

### 4. 启动前端

```bash
cd front

# 安装依赖
npm install

# 开发模式启动（端口 8020）
npm run serve
```

前端启动成功后访问：`http://localhost:8020`

### 默认账号

首次使用需通过注册页面创建账号。支持的用户类型：
- **个人用户** (type=0)：适用于个人创作者
- **企业用户** (type=1)：适用于企业主体
- **机构用户** (type=2)：适用于公证/鉴定等权威机构

## API 接口

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/register` | 用户注册 | 否 |
| POST | `/login` | 用户登录（返回 AccessToken + Cookie） | 否 |
| GET | `/refresh` | 刷新令牌（HttpOnly Cookie） | Cookie |

### 存证模块 `/api/evidence`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/save` | 创建存证（含文件上传） | ✅ |
| GET | `/list` | 存证列表（分页/搜索/筛选） | ✅ |
| GET | `/{id}` | 存证详情 | ✅ |
| GET | `/my` | 我的存证 | ✅ |
| POST | `/{id}/chain` | 上链操作 | ✅ |
| GET | `/verify/{hash}` | 验证存证 | ✅ |

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/profile` | 获取个人信息 | ✅ |
| PUT | `/profile` | 更新个人信息 | ✅ |
| PUT | `/password` | 修改密码 | ✅ |

### 统计模块 `/api/statistics`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/summary` | 数据统计概览 | ✅ |

### 文件模块 `/api/file`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/upload` | 文件上传 | ✅ |
| GET | `/download/{fileName}` | 文件下载 | ✅ |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1700000000000
}
```

## 智能合约

### EvidenceContract

基于 FISCO-BCOS 预编译合约 `TableFactory` 的 CRUD 合约模式，在链上建立存证表 `t_evidence`。

```solidity
// 核心方法
function saveEvidence(string hash, string metadata) public returns (int256);
function getEvidence(string hash) public view returns (string metadata, string owner, string timestamp, string verifyCount);
function verifyEvidence(string hash) public returns (bool, string);
function getEvidenceCount() public view returns (uint256);

// 事件
event EvidenceSaved(string hash, string metadata, string owner, uint256 timestamp);
event EvidenceVerified(string hash, uint256 verifyCount, uint256 timestamp);
```

### 链上数据结构

| 字段 | 类型 | 说明 |
|------|------|------|
| hash | string (主键) | SHA256 哈希值 |
| metadata | string | 存证元数据（JSON） |
| owner | string | 存证人地址 |
| timestamp | string | 上链时间戳 |
| verify_count | string | 验证次数 |
| status | string | 状态（1=有效） |

## 项目结构

```
evidence-dapp/
├── contracts/                  # Solidity 智能合约
│   ├── EvidenceContract.sol    #   存证合约
│   └── TableFactory.sol        #   预编译合约接口
│
├── evidence-backend/           # Spring Boot 后端
│   ├── src/main/java/com/evidence/
│   │   ├── EvidenceApplication.java       # 启动类
│   │   ├── common/                       # 公共模块
│   │   │   ├── R.java                   #   统一响应封装
│   │   │   ├── ResultCode.java           #   错误码枚举
│   │   │   ├── PageResult.java           #   分页结果
│   │   │   └── exception/               #   全局异常处理
│   │   ├── config/                       # 配置类
│   │   │   ├── JwtAuthInterceptor.java   #   JWT 认证拦截器
│   │   │   ├── JwtInterceptorConfig.java #   拦截器配置
│   │   │   ├── CorsConfig.java           #   跨域配置
│   │   │   ├── MultipartConfig.java      #   文件上传配置
│   │   │   └── MyBatisPlusConfig.java    #   MP 配置
│   │   ├── controller/                   # 控制器层
│   │   │   ├── AuthController.java       #   认证接口
│   │   │   ├── EvidenceController.java   #   存证接口
│   │   │   ├── UserController.java       #   用户接口
│   │   │   ├── FileController.java       #   文件接口
│   │   │   └── StatisticsController.java #   统计接口
│   │   ├── service/                      # 业务逻辑层
│   │   │   ├── AuthService.java          #   认证服务
│   │   │   ├── EvidenceService.java      #   存证服务
│   │   │   ├── FileUploadService.java    #   文件服务
│   │   │   └── impl/                    #     服务实现
│   │   ├── model/                        # 数据模型
│   │   │   ├── entity/                  #   数据库实体
│   │   │   ├── vo/                      #   视图对象
│   │   │   ├── bo/                      #   业务对象
│   │   │   └── enums/                   #   枚举类
│   │   ├── mapper/                       # MyBatis Mapper
│   │   ├── util/                         # 工具类
│   │   │   └── JwtUtil.java             #   JWT 工具（双 Token）
│   │   └── blockchain/                   # 区块链模块
│   │       ├── FiscoBcosClient.java     #   SDK 客户端
│   │       ├── FiscoBcosConfig.java     #   SDK 配置
│   │       ├── BlockchainEvidenceService.java # 链上存证服务
│   │       ├── ReceiptHandler.java      #   回执处理
│   │       ├── WeBASEUtils.java         #   WeBASE 工具
│   │       └── BlockchainHealthCheckTask.java # 健康检查定时任务
│   ├── src/main/resources/
│   │   ├── application.yml               # 应用配置
│   │   ├── sql/init.sql                 #   数据库初始化（4张表）
│   │   ├── mapper/                      #   XML 映射
│   │   ├── abi/                         #   合约 ABI
│   │   └── conf/                        #   SDK 证书
│   └── pom.xml
│
├── front/                      # Vue.js 前端
│   ├── src/
│   │   ├── main.js            # 入口文件（Axios 拦截器 / 双 Token）
│   │   ├── App.vue            # 根组件（Shell 布局）
│   │   ├── router.js          # 路由配置
│   │   ├── assets/styles/
│   │   │   └── global.css     # 全局样式 / CSS 变量 / 设计系统
│   │   ├── components/
│   │   │   ├── Header.vue     # 顶部导航栏
│   │   │   └── Navigator.vue  # 左侧菜单栏 + 用户卡片
│   │   └── views/
│   │       ├── Login.vue      # 登录页
│   │       ├── Register.vue   # 注册页
│   │       ├── Home.vue       # 首页仪表盘
│   │       ├── EvidenceCreate.vue  # 新建存证
│   │       ├── EvidenceList.vue    # 存证列表
│   │       ├── Individual.vue      # 个人中心
│   │       └── VerifyPage.vue      # 存证验证
│   ├── package.json
│   └── vue.config.js          # 开发服务器配置（端口 8020）
│
├── LICENSE                    # MIT 开源协议
├── .gitignore
└── README.md
```

## 数据库设计

### ER 关系

```
sys_user (用户表)
  ├── 1:N → evidence_record (存证记录表)
  │            ├── 1:N → work_file (作品文件表)
  │            └── 1:N → verify_record (验证记录表)
  └── 1:N → verify_record (作为验证人)
```

### 表清单（共 4 张）

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| **sys_user** | 用户表 | id, username, password(BCrypt), real_name, phone, email, user_type(0/1/2), status |
| **evidence_record** | 存证记录表 | id, user_id, evidence_no, work_title, work_hash(SHA256), tx_hash, block_number, status(0待上链/1已上链/2失败) |
| **work_file** | 作品文件表 | id, evidence_id, file_name, file_path, file_size, file_type, file_hash |
| **verify_record** | 验证记录表 | id, evidence_id, verifier_id, verifier_name, hash, is_exist, match_result, verify_time |

## 双 Token 认证机制

本系统采用 **Access Token + Refresh Token** 双令牌方案，兼顾安全性与用户体验：

```
登录流程：
  POST /api/auth/login
  → Response Body:  { accessToken, expiresIn: 300 }
  → Response Cookie: refresh_token (HttpOnly, 7天)

请求流程：
  Axios 拦截器自动附加: Authorization: Bearer <accessToken>

过期刷新流程（静默，用户无感知）：
  收到 401 → GET /api/auth/refresh (携带 Cookie)
  → 返回新 accessToken → 重试原请求
  → refresh_token 也失效 → 跳转登录页
```

| Token | 存储位置 | 有效期 | 用途 |
|-------|---------|--------|------|
| Access Token | JS 内存变量 | 5 分钟 | API 请求认证 |
| Refresh Token | HttpOnly Cookie | 7 天 | 刷新 Access Token |

## License

本项目基于 [MIT License](LICENSE) 开源。

## 致谢

- [FISCO-BCOS](https://github.com/FISCO-BCOS/FISCO-BCOS) — 金融级区块链底层平台
- [WeBASE](https://github.com/WeBankFinTech/WeBASE) — 区块链中间件平台
- [Spring Boot](https://spring.io/projects/spring-boot) — Java 企业级开发框架
- [Vue.js](https://vuejs.org/) — 渐进式 JavaScript 框架
- [Element UI](https://element.eleme.io/) — Vue 2.0 桌面端组件库
