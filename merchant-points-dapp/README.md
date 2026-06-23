# 简单的商户积分通兑 DApp

> 基于 **FISCO-BCOS** 区块链的商户积分通兑平台，实现积分的发行、转账、消费与跨商户互通。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5.x-4FC08D.svg?logo=vue.js)](https://vuejs.org/)
[![FISCO-BCOS](https://img.shields.io/badge/FISCO--BCOS-3.0+-blue.svg)](https://fisco-bcos.org/)

---

## 功能特性

| 模块 | 说明 |
|------|------|
| 用户认证 | 登录 / 注册 / JWT Token 鉴权 |
| 积分发行 | 管理员向商户发行积分（上链） |
| 积分转账 | 商户之间互相转账积分 |
| 积分消费 | 商户消费积分并记录流水 |
| 商户管理 | 商户信息维护、审核流程 |
| 交易流水 | 全量交易记录查询与追溯 |
| 数据看板 | 积分统计与可视化展示 |

## 技术栈

```
后端:  Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Knife4j
前端:  Vue 3.5 + TypeScript 6.0 + Element Plus 2.14 + Pinia 3.0 + Vite 8.0
数据库: MySQL 8.0
区块链: FISCO-BCOS 3.0+ (WeBASE-Front)
智能合约: Solidity ^0.4.25 (PointsContract)
```

## 项目结构

```
merchant-points-dapp/
├── points-backend/          # 后端服务 (Spring Boot)
│   ├── src/main/java/com/points/
│   │   ├── controller/      # REST 接口层 (9个Controller)
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/          # MyBatis 数据访问层
│   │   ├── entity/          # 数据库实体
│   │   ├── config/          # 配置类 (区块链/CORS/Swagger)
│   │   ├── interceptor/     # JWT 认证拦截器
│   │   └── utils/           # 工具类
│   ├── src/main/resources/
│   │   ├── contracts/       # 智能合约源码
│   │   ├── cert/            # 区块链证书
│   │   └── application.yml  # 配置文件
│   └── sql/init.sql         # 数据库初始化脚本
│
└── points-frontend/         # 前端应用 (Vue 3)
    ├── src/
    │   ├── views/           # 页面 (7个页面)
    │   ├── api/             # API 接口封装
    │   ├── components/      # 布局组件
    │   ├── stores/          # Pinia 状态管理
    │   └── router/          # 路由配置
    └── vite.config.ts
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- FISCO-BCOS 3.0+ 节点

### 1. 初始化数据库

```bash
mysql -u root -p < points-backend/sql/init.sql
```

### 2. 配置后端

编辑 `points-backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://<MYSQL_HOST>:3306/points_dapp
    username: root
    password: <YOUR_PASSWORD>

bcos:
  node-ip: <BCOS_NODE_IP>
  node-port: 20200
  group-id: 1
  webase-url: http://<BCOS_NODE_IP>:5002/WeBASE-Front/trans/handle
  contract-address:
    points: '<YOUR_CONTRACT_ADDRESS>'

jwt:
  secret: <YOUR_JWT_SECRET>
  expiration: 86400000
```

### 3. 启动后端

```bash
cd points-backend
mvn spring-boot:run
```

后端启动后访问 Swagger 文档：http://localhost:8081/doc.html

### 4. 启动前端

```bash
cd points-frontend
npm install
npm run dev
```

前端地址：http://localhost:5173

### 5. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

## 智能合约

**PointsContract** — 积分管理合约 (Solidity ^0.4.25)

| 方法 | 参数 | 说明 |
|------|------|------|
| `issuePoints` | to, amount, metadata | 发行积分 |
| `transfer` | to, amount | 转账积分 |
| `consume` | amount, metadata | 消费积分 |
| `getBalance` | account | 查询余额 |

合约源码位于：`points-backend/src/main/resources/contracts/PointsContract.sol`

## 数据库设计

| 表名 | 说明 |
|------|------|
| `sys_user` | 系统用户表（管理员/商户） |
| `merchant` | 商户信息表（含链上地址与缓存余额） |
| `points_transaction` | 积分交易流水表（含 txHash） |
| `points_issue` | 积分发行申请表（含审批流程） |

详细建表语句见：`points-backend/sql/init.sql`

## API 接口

| 模块 | 路径前缀 | 主要接口 |
|------|----------|----------|
| 认证 | `/api/auth` | POST login, POST register |
| 积分 | `/api/points` | POST issue, POST transfer, POST consume |
| 余额 | `/api/balance` | GET {merchantId} |
| 商户 | `/api/merchants` | GET list, GET detail, PUT audit |
| 交易 | `/api/transactions` | GET list, GET detail |
| 看板 | `/api/dashboard` | GET stats |

完整接口文档：启动后端后访问 [Knife4j Swagger UI](http://localhost:8081/doc.html)

## 角色权限

| 角色 | 权限范围 |
|------|----------|
| **Admin** | 全部功能：发行积分、审核商户、查看所有数据 |
| **Merchant** | 积分转账、积分消费、查看自身数据 |

## 截图

> （待补充）


