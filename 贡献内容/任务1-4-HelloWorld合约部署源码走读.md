# HelloWorld 合约部署源码走读

> **源码文件**: [deploy_hello.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tx/deploy_hello.cpp)
> **代码行数**: 298行
> **核心功能**: 通过 SDK 部署 HelloWorld 合约到区块链，覆盖完整的交易生命周期

---

## 任务目标

讲清四个核心流程：**配置读取 → 群组信息获取(含密码类型判断) → 账户(密钥对)生成 → 合约部署交易发送与回执解析**。

---

## 一、源码结构总览（按行号分段）

| 行号范围 | 功能 |
|---------|------|
| 1-30 | 头文件引入 |
| 36-116 | HelloWorld 合约源码 + 两套字节码常量 |
| 120-123 | `getBinary(sm)` 辅助函数 |
| 125-133 | `usage()` 帮助信息 |
| 135-153 | **SDK 初始化** |
| 156-176 | **群组信息获取 + 密钥对生成** |
| 184-236 | **getBlockLimit + 字节码选择 + sendTransaction + 回执解析 + f.get()** |
| 237-295 | 注释掉的备选实现（TransactionBuilderService 版本） |

---

## 二、分步走读

### 第1步：SDK 初始化（行148-153）

```cpp
auto factory = std::make_shared<SdkFactory>();
auto sdk = factory->buildSdk(config);   // config 是 argv[1]，直接传字符串路径
sdk->start();
```

`buildSdk()` 接收配置文件路径字符串，内部完成 INI 解析、证书加载、节点连接。不需要额外的 `SampleConfig` 包装。

### 第2步：获取群组信息（行156-163）

```cpp
bcos::group::GroupInfo::Ptr groupInfo = sdk->service()->getGroupInfo(group);
if (!groupInfo) {
    std::cout << "group not exist" << std::endl;
    exit(-1);
}
```

`GroupInfo` 对象携带群组链信息。最关键的是 `smCryptoType()` 字段——它决定所有密码学操作走国密 SM2 还是标准 ECDSA。

### 第3步：生成密钥对（行165-176）

```cpp
crypto::SignatureCrypto::Ptr keyPairFactory;
crypto::KeyPairInterface::UniquePtr keyPair;

if (groupInfo->smCryptoType()) {
    keyPairFactory = std::make_shared<bcos::crypto::SM2Crypto>();
    keyPair = keyPairFactory->generateKeyPair();
} else {
    keyPairFactory = std::make_shared<bcos::crypto::Secp256k1Crypto>();
    keyPair = keyPairFactory->generateKeyPair();
}
```

关键设计：**密钥对类型必须与群组密码类型匹配**。注意这里分两个阶段——先创建 `SignatureCrypto` 工厂，再调用 `generateKeyPair()` 生成 `KeyPairInterface::UniquePtr`。

### 第4步：获取 BlockLimit（行184-185）

```cpp
int64_t blockLimit = -1;
sdk->service()->getBlockLimit(group, blockLimit);   // 输出参数模式
```

`getBlockLimit` 采用输出参数模式（传入引用），不是返回值。`-1` 是初始值，调用后被更新为当前区块号 + 上限（通常约 1000）。

### 第5步：选择合约字节码（行120-123 + 190-191）

HelloWorld 有两套预编译字节码：
- `hwBIN`（行56-79）：ECDSA 标准版
- `hwSmBIN`（行81-104）：国密 SM 版

```cpp
auto hexBin = getBinary(groupInfo->smCryptoType());  // 根据国密标志选版本
auto binBytes = fromHexString(std::string(hexBin));    // hex → bytes
```

### 第6步（核心）：sendTransaction 部署调用（行193-234）

```cpp
auto rpcService = sdk->jsonRpcService();

std::promise<bool> p;
auto f = p.get_future();

rpcService->sendTransaction(
    *keyPair,                    // 1. 签名密钥对
    group,                       // 2. 群组ID
    "",                          // 3. to: 部署合约时为空
    "",                          // 4. data: 构造函数参数，空
    std::move(*binBytes),        // 5. 合约字节码（move 转移所有权）
    "",                          // 6. ABI，部署时不填
    0,                           // 7. 属性值
    "extraData",                 // 8. 附加数据
    // ===== 异步回调 =====
    [&p](bcos::Error::Ptr _error, std::shared_ptr<bcos::bytes> _resp) {
        if (_error && _error->errorCode() != 0) {
            std::cout << "deploy failed: " << _error->errorCode() << ":"
                      << _error->errorMessage() << std::endl;
        } else {
            std::string receipt(_resp->begin(), _resp->end());
            // 解析 JSON 回执
            Json::Value root;
            Json::Reader jsonReader;
            if (jsonReader.parse(receipt, root)) {
                std::cout << "contract address ==> "
                          << root["result"]["contractAddress"].asString() << std::endl;
            }
        }
        p.set_value(true);
    }
);

f.get();  // 阻塞等待异步结果
```

**参数说明**：

| 参数 | 值 | 含义 |
|------|-----|------|
| `*keyPair` | 解引用 `UniquePtr` | 签名密钥对 |
| `""` (to) | 空字符串 | 表示**合约部署**（非调用） |
| `""` (data) | 空字符串 | 构造函数参数，HelloWorld 无参 |
| `std::move(*binBytes)` | 移动 | 字节码所有权转移，避免大对象拷贝 |

**异步同步化**：`std::promise/future` 将异步回调转为同步等待。`f.get()` 阻塞直到 `p.set_value(true)` 被调用。

### 第7步：回执 JSON 结构

```json
{
  "result": {
    "contractAddress": "0x..."
  }
}
```

回调中直接通过 `root["result"]["contractAddress"].asString()` 提取合约地址。

---

## 三、运行方法

```bash
./deploy_hello ./config_sample.ini group0
```

| 参数 | 说明 |
|------|------|
| `./config_sample.ini` | SDK 配置文件路径 |
| `group0` | 目标群组 ID |

---

## 四、完整流程图

```
argv[1] (配置路径字符串)
      ↓
SdkFactory::buildSdk(config)  → sdk->start()
      ↓
getGroupInfo("group0")  ──→  smCryptoType()?
      ↓                         ↓ 是              ↓ 否
SM2Crypto                Secp256k1Crypto
  generateKeyPair()        generateKeyPair()
      ↓                         ↓
getBinary(true)→hwSmBIN   getBinary(false)→hwBIN
      ↓                         ↓
getBlockLimit(group, blockLimit)  ← 输出参数
      ↓
jsonRpcService()->sendTransaction(
    *keyPair, group, "", "", std::move(*binBytes), "", 0, "extraData", callback)
      ↓ 异步
f.get() 阻塞等待 → callback: JSON解析 → contractAddress
```

---

## 五、关键 API 速查

| API | 作用 | 所在行 |
|-----|------|--------|
| `SdkFactory::buildSdk(configPath)` | 从字符串路径创建 SDK | 150 |
| `Service::getGroupInfo(group)` | 获取群组信息（含国密标识） | 157 |
| `Service::getBlockLimit(group, blockLimit)` | 获取交易有效期（输出参数） | 185 |
| `SM2Crypto::generateKeyPair()` | 生成国密密钥对 | 170 |
| `Secp256k1Crypto::generateKeyPair()` | 生成 ECDSA 密钥对 | 174 |
| `Sdk::jsonRpcService()` | 获取 JSON-RPC 服务 | 193 |
| `jsonRpcService->sendTransaction(...)` | 发送部署交易 | 197 |

---

## 小结

`deploy_hello.cpp` 是 FISCO BCOS SDK 中最完整的入门示例，298 行代码串联了**字符串配置 → 群组连接 → 密码学适配（SM2Crypto / Secp256k1Crypto）→ 字节码选择 → 交易发送 → promise/future 异步同步化 → 回执解析**全链路。理解这个文件后，合约调用只需把 `to` 地址从空改为目标合约地址即可。