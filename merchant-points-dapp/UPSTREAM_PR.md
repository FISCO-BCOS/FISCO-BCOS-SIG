# [夏季开源成长营 3-2] 商户积分通兑 DApp 完整项目

## 简介

基于 **FISCO-BCOS 3.0+** 区块链的商户积分通兑平台（DApp），实现积分的**发行、转账、消费、查询**全流程上链。采用前后端分离架构，后端通过 WeBASE-Front 与区块链节点交互，前端使用 Vue 3 + Element Plus 构建管理界面。

项目涵盖完整的业务闭环：用户注册 → 商户审核 → 积分发行 → 商户间转账 → 积分消费 → 流水追溯。

## 功能清单

| 模块 | 文件/目录 | 核心内容 |
|------|-----------|----------|
| 用户认证 | `AuthController` + `AuthService` | 登录（JWT）、注册（商户+审核流） |
| 积分发行 | `IssueController` + `PointsIssueService` | 管理员向商户发行积分，调用合约 `issuePoints()` 上链 |
| 积分转账 | `TransferController` + `PointsTransferService` | 商户间互转，调用合约 `transfer()` 上链 |
| 积分消费 | `ConsumeController` + `PointsConsumeService` | 商户消费积分，调用合约 `consume()` 上链 |
| 余额查询 | `BalanceController` + `BalanceService` | 查询链上 + MySQL 双写缓存余额 |
| 商户管理 | `MerchantController` + `MerchantService` | CRUD、审核（通过/拒绝）、状态管理 |
| 交易流水 | `TransactionController` + `TransactionService` | 全量流水查询（含 txHash、区块高度、链状态） |
| 数据看板 | `DashboardController` + `DashboardService` | 商户数、总发行额、总消费额、交易量统计 |
| 智能合约 | `contracts/PointsContract.sol` | Solidity ^0.4.25，含 issue/transfer/consume/getBalance |
| 前端页面 | `views/` (7个页面) | 登录、注册、仪表盘、发行、转账、消费、流水、商户、个人中心 |

## 技术栈

```
后端:  Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Knife4j(Swagger) + JJWT
前端:  Vue 3.5 + TypeScript 6.0 + Element Plus 2.14 + Pinia 3.0 + Vite 8.0
数据库: MySQL 8.0 (4张表: sys_user / merchant / points_transaction / points_issue)
区块链: FISCO-BCOS 3.0+ (WeBASE-Front RESTful 交互)
智能合约: PointsContract (Solidity ^0.4.25)
```

## 项目结构

```
merchant-points-dapp/
├── points-backend/                    # Spring Boot 后端
│   ├── src/main/java/com/points/
│   │   ├── controller/               # 9 个 REST Controller
│   │   ├── service/                  # 接口 + impl 实现
│   │   ├── mapper/                   # MyBatis-Plus Mapper
│   │   ├── entity/                   # 4 个数据库实体
│   │   ├── dto/                      # 请求/响应 DTO
│   │   ├── config/                   # 区块链/CORS/Swagger/安全配置
│   │   ├── interceptor/              # JWT 认证拦截器
│   │   ├── annotation/               # @RequiresAdmin / @RequiresMerchant
│   │   ├── enums/                    # 枚举(角色/交易类型/审核状态/错误码)
│   │   ├── exception/                # 全局异常处理(业务/链操作/余额不足)
│   │   ├── scheduled/                # 定时任务(余额同步/健康检查)
│   │   └── utils/                    # JWT工具/序号生成/手机脱敏/WeBASE工具
│   ├── src/main/resources/
│   │   ├── contracts/PointsContract.sol    # 智能合约源码
│   │   ├── abi/PointsContract.abi          # 合约 ABI
│   │   └── application.yml                 # 配置文件(已脱敏)
│   └── sql/init.sql                     # 数据库建表+初始化数据
│
├── points-frontend/                   # Vue 3 前端
│   ├── src/views/                       # 7 个业务页面
│   ├── src/api/                          # 5 个 API 模块
│   ├── src/components/layout/            # Header + Sidebar + Layout
│   ├── src/stores/useUserStore.ts        # Pinia 用户状态
│   ├── src/utils/request.ts              # Axios 封装(拦截器/Token)
│   └── src/router/index.ts               # 路由(守卫鉴权)
│
├── README.md                           # 项目说明
└── PULL_REQUEST.md                     # 详细 PR 文档
```

## API 接口一览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录获取 JWT |
| 认证 | POST | `/api/auth/register` | 商户注册 |
| 积分 | POST | `/api/points/issue` | 发行积分(Admin) |
| 积分 | POST | `/api/points/transfer` | 转账(Merchant) |
| 积分 | POST | `/api/points/consume` | 消费(Merchant) |
| 余额 | GET | `/api/balance/{id}` | 查询余额 |
| 商户 | GET/PUT | `/api/merchants` | 列表/详情/审核 |
| 交易 | GET | `/api/transactions` | 流水查询 |
| 看板 | GET | `/api/dashboard/stats` | 统计数据 |

> 完整 Swagger 文档：启动后端访问 `http://localhost:8081/doc.html`

## 智能合约 — PointsContract

```solidity
pragma solidity ^0.4.25;

contract PointsContract {
    function issuePoints(address to, uint256 amount, string metadata) public onlyOwner;
    function transfer(address to, uint256 amount) public;
    function consume(uint256 amount, string metadata) public;
    function getBalance(address account) public view returns (uint256);

    event PointsIssued(address indexed to, uint256 amount, string metadata, uint256 timestamp);
    event PointsTransferred(address indexed from, address indexed to, uint256 amount, uint256 timestamp);
    event PointsConsumed(address indexed from, uint256 amount, string metadata, uint256 timestamp);
}
```

## 数据库设计 (4张表)

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| `sys_user` | 系统用户 | role(Admin/Merchant), status, blockchain_address |
| `merchant` | 商户信息 | chain_address, points_balance, total_issued, audit_status |
| `points_transaction` | 交易流水 | tx_type(1发/2转/3收/4消), tx_hash, block_number, chain_status |
| `points_issue` | 发行记录 | issue_no, amount, reason, status(待审/已发/已拒) |

> 完整建表语句见 `points-backend/sql/init.sql`

## 部署方式

### 前置条件

- JDK 1.8+ / Maven 3.6+ / Node.js 18+ / MySQL 8.0+ / FISCO-BCOS 3.0+ 节点

### 步骤

```bash
# 1. 初始化数据库
mysql -u root -p < points-backend/sql/init.sql

# 2. 配置 application.yml（数据库地址、区块链节点IP、合约地址等）

# 3. 部署证书（从FISCO-BCOS节点拷贝到 cert/ 目录）
#    ca.crt / sdk.crt / sdk.key

# 4. 启动后端
cd points-backend && mvn spring-boot:run   # http://localhost:8081

# 5. 启动前端
cd points-frontend && npm install && npm run dev   # http://localhost:5173
```

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

## 安全说明

- `cert/` 目录（含私钥）已加入 `.gitignore`，**未提交到仓库**
- `application.yml` 中敏感配置已用占位符替换，部署时需自行填写
- 密码使用 BCrypt 加密存储
- 接口层 JWT Token 鉴权 + 角色注解权限控制

## 待优化项

- [ ] 单元测试覆盖（Service 层）
- [ ] Redis 缓存集成
- [ ] Prometheus 监控接入
