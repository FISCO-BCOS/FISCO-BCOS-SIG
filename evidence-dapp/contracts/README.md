# FISCO-BCOS 智能合约

## 合约说明

### EvidenceContract

存证合约，基于 FISCO-BCOS 2.x TableFactory 实现链上数据存证与验证。

**功能：**

| 方法 | 说明 |
|------|------|
| `saveEvidence(hash, metadata)` | 保存存证，返回插入条数 |
| `getEvidence(hash)` | 查询存证，返回 metadata、owner、timestamp、verifyCount |
| `verifyEvidence(hash)` | 验证存证，verify_count +1 |
| `getEvidenceCount()` | 获取存证总数 |

**事件：**

| 事件 | 触发时机 |
|------|---------|
| `EvidenceSaved` | 存证保存成功 |
| `EvidenceVerified` | 存证验证成功 |

### TableFactory

FISCO-BCOS 2.x 内置的 CRUD 表操作接口，提供 Table、Entry、Condition、Entries、UpdateField 等类型。

## 部署步骤

1. 启动 FISCO-BCOS 节点
2. 获取 TableFactory 合约地址（FISCO-BCOS 2.x 内置，地址通常为 `0x1001`）
3. 使用控制台或 SDK 部署 EvidenceContract，构造参数传入 TableFactory 地址：

```bash
# FISCO-BCOS 控制台部署
deploy EvidenceContract 0x1001
```

4. 记录部署后的合约地址，供后端服务调用

## 参数说明

| 参数 | 说明 |
|------|------|
| `tableFactoryAddress` | 构造函数参数，FISCO-BCOS 内置 TableFactory 地址 |
| `hash` | 存证哈希，作为主键 |
| `metadata` | 存证元数据 |
| `owner` | 存证所有者地址（自动填充） |
| `timestamp` | 存证时间戳（自动填充） |
| `verify_count` | 验证次数（自动维护） |
| `status` | 状态标识，1 为有效 |
