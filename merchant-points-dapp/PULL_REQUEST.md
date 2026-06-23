# 商户积分通兑 DApp - PR 提交文档

---

## 一、项目概述

### 1.1 项目名称
**商户积分通兑 DApp** (Merchant Points Exchange DApp)

### 1.2 项目简介
基于 FISCO-BCOS 区块链的商户积分通兑平台，实现商户积分的发行、转账、消费等功能，支持多商户之间的积分互通。

### 1.3 核心价值
- **去中心化积分管理**：积分记录存储在区块链上，不可篡改
- **跨商户通兑**：商户之间可以自由转账积分
- **透明可追溯**：所有交易记录可查、可追溯

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 | 企业级后端服务框架 |
| ORM框架 | MyBatis-Plus | 3.5.5 | 增强型MyBatis框架 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 区块链 | FISCO-BCOS | 3.0+ | 国产联盟链平台（通过WeBASE-Front交互） |
| 前端框架 | Vue | 3.5.x | 渐进式JavaScript框架 |
| 类型系统 | TypeScript | 6.0.x | JavaScript超集 |
| UI组件 | Element Plus | 2.14.x | Vue3 UI组件库 |
| 状态管理 | Pinia | 3.0.x | Vue官方状态管理 |
| 构建工具 | Vite | 8.0.x | 下一代前端构建工具 |

### 2.2 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ 登录页   │ │ 仪表盘   │ │ 积分管理 │ │ 商户管理     │  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────┬───────┘  │
└───────┼────────────┼────────────┼───────────────┼─────────┘
        │            │            │               │
        ▼            ▼            ▼               ▼
┌─────────────────────────────────────────────────────────────┐
│                      API 网关层                            │
│              Spring Boot Controller                        │
└────────────────────────────┬────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Service层   │    │   Service层   │    │   Service层   │
│  (业务逻辑)   │    │  (业务逻辑)   │    │  (业务逻辑)   │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   Mapper层    │    │   Mapper层    │    │   区块链SDK   │
│  (数据访问)   │    │  (数据访问)   │    │  (FISCO-BCOS) │
└───────┬───────┘    └───────┬───────┘    └───────────────┘
        │                    │
        └────────────┬───────┘
                     ▼
            ┌───────────────┐
            │    MySQL      │
            │  (points_dapp)│
            └───────────────┘
```

---

## 三、功能特性

### 3.1 功能模块清单

| 模块 | 功能 | 状态 |
|------|------|------|
| 认证模块 | 用户登录、商户注册 | ✅ 完成 |
| 积分发行 | 管理员发行积分给商户 | ✅ 完成 |
| 积分转账 | 商户间积分转账 | ✅ 完成 |
| 积分消费 | 商户积分消费 | ✅ 完成 |
| 商户管理 | 商户信息管理、审核 | ✅ 完成 |
| 交易流水 | 交易记录查询 | ✅ 完成 |
| 仪表盘 | 数据统计展示 | ✅ 完成 |

### 3.2 角色权限

| 角色 | 权限 | 说明 |
|------|------|------|
| **Admin** | 全部权限 | 系统管理员，可发行积分、审核商户 |
| **Merchant** | 转账、消费 | 商户用户，可转账给其他商户、消费积分 |

---

## 四、项目结构

### 4.1 后端结构 (points-backend)

```
points-backend/
├── src/main/java/com/points/
│   ├── annotation/          # 自定义注解
│   │   ├── RequiresAdmin.java
│   │   └── RequiresMerchant.java
│   ├── config/              # 配置类
│   │   ├── BcosConfig.java      # 区块链配置
│   │   ├── CorsConfig.java      # CORS配置
│   │   ├── Knife4jConfig.java   # Swagger配置
│   │   ├── SecurityConfig.java  # 安全配置
│   │   └── WebMvcConfig.java    # MVC配置
│   ├── controller/          # 控制器
│   │   ├── AuthController.java       # 认证接口
│   │   ├── BalanceController.java    # 余额接口
│   │   ├── ConsumeController.java    # 消费接口
│   │   ├── DashboardController.java  # 仪表盘接口
│   │   ├── IssueController.java      # 发行接口
│   │   ├── MerchantController.java   # 商户接口
│   │   ├── TransactionController.java       # 交易接口
│   │   ├── TransferController.java   # 转账接口
│   │   └── UserController.java       # 用户接口
│   ├── dto/                 # 数据传输对象
│   │   ├── request/         # 请求DTO
│   │   └── response/        # 响应DTO
│   ├── entity/              # 数据库实体
│   │   ├── Merchant.java
│   │   ├── PointsIssue.java
│   │   ├── PointsTransaction.java
│   │   └── SysUser.java
│   ├── enums/               # 枚举类
│   ├── exception/           # 异常处理
│   ├── interceptor/         # 拦截器
│   │   └── JwtAuthInterceptor.java
│   ├── mapper/              # MyBatis Mapper
│   ├── scheduled/           # 定时任务
│   │   ├── BalanceSyncTask.java     # 余额同步
│   │   └── HealthCheckTask.java     # 健康检查
│   ├── service/             # 业务服务层
│   ├── utils/               # 工具类
│   └── PointsDappApplication.java
├── src/main/resources/
│   ├── abi/                 # 合约ABI
│   ├── cert/                # 区块链证书
│   ├── contracts/           # 智能合约源码
│   └── application.yml      # 配置文件
├── sql/                     # 数据库初始化脚本
└── pom.xml                  # Maven配置
```

### 4.2 前端结构 (points-frontend)

```
points-frontend/
├── src/
│   ├── api/                 # API接口定义
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   ├── merchant.ts
│   │   ├── points.ts
│   │   └── user.ts
│   ├── components/          # 组件
│   │   └── layout/          # 布局组件
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── stores/              # Pinia状态管理
│   │   └── useUserStore.ts
│   ├── utils/               # 工具函数
│   │   └── request.ts       # Axios封装
│   ├── views/               # 页面视图
│   │   ├── dashboard/       # 仪表盘
│   │   ├── login/           # 登录页
│   │   ├── merchants/       # 商户管理
│   │   ├── points/          # 积分操作
│   │   ├── profile/         # 个人中心
│   │   ├── register/        # 注册页
│   │   └── transactions/    # 交易流水
│   ├── App.vue
│   ├── main.ts
│   └── style.css
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 五、API 接口清单

### 5.1 认证模块

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 用户登录 | POST | `/api/auth/login` | 用户登录获取JWT Token |
| 商户注册 | POST | `/api/auth/register` | 商户注册（需审核） |

### 5.2 积分模块

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 积分发行 | POST | `/api/points/issue` | 管理员发行积分 |
| 积分转账 | POST | `/api/points/transfer` | 商户间转账 |
| 积分消费 | POST | `/api/points/consume` | 商户积分消费 |
| 查询余额 | GET | `/api/balance/{merchantId}` | 查询商户余额 |

### 5.3 商户模块

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 商户列表 | GET | `/api/merchants` | 获取商户列表 |
| 商户详情 | GET | `/api/merchants/{id}` | 获取商户详情 |
| 审核商户 | PUT | `/api/merchants/{id}/audit` | 审核商户申请 |
| 更新商户 | PUT | `/api/merchants/{id}` | 更新商户信息 |

### 5.4 交易模块

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 交易列表 | GET | `/api/transactions` | 获取交易流水 |
| 交易详情 | GET | `/api/transactions/{id}` | 获取交易详情 |

### 5.5 仪表盘模块

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 统计数据 | GET | `/api/dashboard/stats` | 获取统计数据 |

---

## 六、数据库设计

### 6.1 核心数据表

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sys_user` | 系统用户表 | id, username, password, role, status |
| `merchant` | 商户信息表 | id, user_id, merchant_name, chain_address, points_balance |
| `points_transaction` | 积分交易流水表 | id, tx_no, tx_type, amount, tx_hash, chain_status |
| `points_issue` | 积分发行申请表 | id, issue_no, merchant_id, amount, status |

### 6.2 数据库连接信息

```yaml
数据库: MySQL 8.0+
数据库名: points_dapp
用户名: root
密码: 123456
端口: 3306
```

---

## 七、智能合约

### 7.1 合约概述

**合约名称**: `PointsContract`

**Solidity 版本**: ^0.4.25

**部署地址**: `0x4382c0fb361325a206fa7f6b87262a924b1200b3`

### 7.2 合约方法

| 方法名 | 参数 | 描述 |
|--------|------|------|
| `issuePoints` | `to`: address, `amount`: uint256, `metadata`: string | 发行积分给指定地址 |
| `transfer` | `to`: address, `amount`: uint256 | 转账积分 |
| `consume` | `amount`: uint256, `metadata`: string | 消费积分 |
| `getBalance` | `account`: address | 查询余额 |

### 7.3 合约事件

| 事件名 | 触发时机 |
|--------|----------|
| `PointsIssued` | 积分发行成功 |
| `PointsTransferred` | 积分转账成功 |
| `PointsConsumed` | 积分消费成功 |

---

## 八、部署说明

### 8.1 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| FISCO-BCOS | 3.0+ |

### 8.2 后端部署

```bash
# 进入后端目录
cd points-backend

# 编译项目
mvn clean package -DskipTests

# 运行项目
java -jar target/points-backend-1.0.0.jar
```

### 8.3 前端部署

```bash
# 进入前端目录
cd points-frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

### 8.4 配置说明

**后端配置文件**: `points-backend/src/main/resources/application.yml`

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://192.168.124.128:3306/points_dapp
    username: root
    password: 123456

bcos:
  node-ip: 192.168.124.128
  node-port: 20200
  group-id: 1

jwt:
  secret: points-exchange-dapp-secret-key-2026
  expiration: 86400000
```

---

## 九、测试说明

### 9.1 测试账户

| 角色 | 用户名 | 密码 | 状态 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 已激活 |

### 9.2 测试步骤

1. **启动后端服务**
   ```bash
   cd points-backend
   mvn spring-boot:run
   ```

2. **启动前端服务**
   ```bash
   cd points-frontend
   npm run dev
   ```

3. **访问应用**
   - 前端地址: http://localhost:5173
   - Swagger文档: http://localhost:8081/doc.html

4. **测试流程**
   - 登录管理员账户 (admin/123456)
   - 注册商户账户
   - 审核商户
   - 发行积分给商户
   - 商户间转账
   - 查询交易流水

---

## 十、代码规范

### 10.1 后端规范

- 命名规范：类名大驼峰，方法名小驼峰
- 接口返回统一格式：`R<T>` 封装
- 异常处理：全局异常处理器统一处理
- 参数校验：使用 `@Valid` 注解

### 10.2 前端规范

- TypeScript 严格模式
- 组件命名：大驼峰，后缀 `Page`/`Component`
- API 调用：统一封装在 `api/` 目录
- 状态管理：使用 Pinia

---

## 十一、待办事项

| 序号 | 事项 | 状态 | 说明 |
|------|------|------|------|
| 1 | 单元测试覆盖 | ⏳ 进行中 | 增加Service层单元测试 |
| 2 | 集成测试 | ⏳ 进行中 | 接口集成测试 |
| 3 | 性能优化 | ⏳ 进行中 | Redis缓存优化 |
| 4 | 日志优化 | ✅ 完成 | 结构化日志输出 |
| 5 | 监控告警 | ⏳ 进行中 | Prometheus监控 |

---

## 十二、贡献指南

### 12.1 代码提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**type** 可选值：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

### 12.2 PR 提交要求

1. 确保代码通过 lint 检查
2. 确保所有测试通过
3. 提供清晰的变更说明
4. 关联相关 Issue

---

**PR 作者**: Jerry George Liang
**提交日期**: 2026年6月
**版本**: v1.0.0
