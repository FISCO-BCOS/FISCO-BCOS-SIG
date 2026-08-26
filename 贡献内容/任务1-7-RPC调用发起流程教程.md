# 一次 RPC 调用是如何发起的

> **源码文件**: [rpc_test.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/rpc/rpc_test.cpp)
> **代码行数**: 203行
> **核心功能**: 手动构建 WsConfig 并发起 RPC 调用，100 线程极限压测 `getBlockNumber`

---

## 任务目标

讲清三个核心环节：**命令行参数解析 → WsConfig 手动初始化 → `getBlockNumber` 调用的完整链路**。

---

## 一、源码结构总览

| 行号范围 | 功能 |
|---------|------|
| 1-31 | 头文件引入 |
| 33-41 | `usage()` |
| 42-73 | C 结构体定义（bcos_sdk_c_endpoint / cert_config / config） |
| 74-108 | **核心：`initWsConfig()` 手动构建 WsConfig** |
| 109-124 | `thread_function()` 线程压测逻辑 |
| 126-158 | `bcos_sdk_create_config()` 工厂函数 |
| 159-203 | **main()：命令行解析 → 100 线程启动** |

---

## 二、分步走读

### 第1步：命令行参数解析（行159-180）

```cpp
// ./rpc 127.0.0.1 20200 ssl group0      (标准 SSL)
// ./rpc 127.0.0.1 20200 sm_ssl group0   (国密 SSL)

const char* host = argv[1];    // 节点 IP
int port = atoi(argv[2]);      // 端口
const char* type = argv[3];    // "ssl" 或 "sm_ssl"
const char* group = argv[4];   // 群组ID

int is_sm_ssl = (strstr(type, "sm_ssl") != NULL) ? 1 : 0;
```

与 `deploy_hello` 等通过 INI 文件配置不同，本示例**不使用配置文件**，所有连接信息均通过命令行传入。`strstr` 用于检测参数中是否包含 `"sm_ssl"` 子串。

### 第2步：C 结构体工厂函数（行126-158）

```cpp
auto config = bcos_sdk_create_config(is_sm_ssl, host, port);

// 内部实现:
auto config = std::make_shared<bcos_sdk_c_config>();
config->thread_pool_size = 8;              // 8 线程池
config->message_timeout_ms = 10000;         // 10秒超时
config->disable_ssl = 1;                    // ★ 跳过 SSL 校验（压测优化）
config->send_rpc_request_to_highest_block_node = 1;
config->ssl_type = sm_ssl ? "sm_ssl" : "ssl";
config->is_cert_path = 1;
config->cert_config = {
    .ca_cert = "./conf/ca.crt",
    .node_key = "./conf/sdk.key",
    .node_cert = "./conf/sdk.crt"};

// 设置对端节点
bcos_sdk_c_endpoint ep = {host, port};
config->peers.push_back(ep);
config->peers_count = 1;
```

关键点：`disable_ssl = 1`。压测场景下每个线程创建独立 SDK 实例，跳过证书验证减少开销。**生产环境不应设为 1**。

### 第3步：WsConfig 构建（行74-108）

```cpp
static std::shared_ptr<WsConfig> initWsConfig(
    std::shared_ptr<bcos_sdk_c_config> config)
{
    auto wsConfig = std::make_shared<WsConfig>();
    wsConfig->setModel(WsModel::Client);

    // 1. 设置对端节点列表
    auto peers = std::make_shared<EndPoints>();
    for (size_t i = 0; i < config->peers_count; i++) {
        peers->insert(NodeIPEndpoint(config->peers[i].host, config->peers[i].port));
    }
    wsConfig->setConnectPeers(peers);

    // 2. 基础参数
    wsConfig->setDisableSsl(config->disable_ssl);
    wsConfig->setThreadPoolSize(config->thread_pool_size);
    wsConfig->setReconnectPeriod(config->reconnect_period_ms);
    wsConfig->setHeartbeatPeriod(config->heartbeat_period_ms);
    wsConfig->setSendMsgTimeout(config->message_timeout_ms);

    // 3. SSL 证书配置（仅在 enable_ssl 时）
    if (!config->disable_ssl) {
        auto contextConfig = std::make_shared<ContextConfig>();
        contextConfig->setSslType(config->ssl_type);
        contextConfig->setIsCertPath(config->is_cert_path);
        // ... 设置 certConfig
        wsConfig->setContextConfig(contextConfig);
    }
    return wsConfig;
}
```

关键差异：使用 `setConnectPeers(EndPoints)` 而非 `addConnectedNode()`，且通过 `setDisableSsl/ setReconnectPeriod / setHeartbeatPeriod` 等精细控制。

### 第4步：thread_function 并发模式（行109-124）

```cpp
void* thread_function(std::shared_ptr<bcos_sdk_c_config> arg)
{
    while (1) {
        auto factory = std::make_shared<SdkFactory>();
        auto wsConfig = initWsConfig(arg);
        auto sdk = factory->buildSdk(wsConfig, arg->send_rpc_request_to_highest_block_node);
        sdk->start();

        auto rpc = sdk->jsonRpc();
        rpc->getBlockNumber(
            "group0", "",    // 群组ID + 空参数
            [](Error::Ptr error, std::shared_ptr<bcos::bytes> resp) {}  // 空回调
        );

        usleep(100);
        sdk->stop();
        sdk.reset(nullptr);
    }
}
```

**关键模式**：
1. 每次循环**创建独立 SDK 实例**——测试每次新建连接的开销
2. 通过 `sdk->jsonRpc()` 获取 JSON-RPC 接口
3. 调用 `getBlockNumber("group0", "", callback)` —— **不是 `callRemoteMethod`**
4. 回调为空 lambda（压测不需要处理结果）
5. `sdk->stop()` + `sdk.reset(nullptr)` 彻底销毁

### 第5步：100 线程启动（行184-201）

```cpp
std::thread threads[100];
for (long t = 0; t < 100; t++) {
    threads[t] = std::thread(thread_function, config);  // 共享同一个 config
}
for (long t = 0; t < 100; t++) {
    threads[t].join();
}
```

100 个线程共享同一个 `config` 对象（`shared_ptr`），每个线程内部循环创建/销毁 SDK。

---

## 三、调用链路图

```
命令行参数 (host, port, ssl_type, group)
  ↓
bcos_sdk_create_config(is_sm_ssl, host, port)
  ├── disable_ssl = 1               ← 跳过 SSL（压测优化）
  ├── cert_config = {ca.crt, ...}   ← 证书路径
  └── peers = [{host, port}]        ← 节点地址
  ↓
initWsConfig(config) → WsConfig
  ├── setModel(Client)
  ├── setConnectPeers(EndPoints)
  ├── setDisableSsl / setThreadPoolSize / ...
  └── setContextConfig (仅 !disable_ssl 时)
  ↓
SdkFactory::buildSdk(wsConfig, ...) → SDK
  ↓
sdk->jsonRpc()->getBlockNumber("group0", "", callback)
  ↓  ↓  ↓  (100线程并发循环 ↑)
  ↓  ↓  ↓
sdk->stop() / sdk.reset()
```

---

## 四、运行方法

```bash
./rpc 127.0.0.1 20200 ssl group0
./rpc 127.0.0.1 20200 sm_ssl group0
```

| 参数 | 说明 |
|------|------|
| 127.0.0.1 | 节点 IP |
| 20200 | 节点 RPC 端口 |
| ssl / sm_ssl | SSL 类型（含子串匹配） |
| group0 | 群组 ID |

---

## 五、与 deploy_hello 的关键差异

| 对比维度 | deploy_hello | rpc_test |
|---------|-------------|----------|
| 配置来源 | INI 文件路径字符串 | 命令行 → C结构体 → WsConfig |
| 实例数 | 1 个全局 SDK | 100 线程各建独立 SDK |
| SSL | 标准证书校验 | `disable_ssl = 1` |
| RPC 调用 | `jsonRpcService()->sendTransaction(...)` | `jsonRpc()->getBlockNumber(...)` |
| SDK 生命周期 | 全程存活 | 每次创建/销毁 |

---

## 小结

`rpc_test.cpp` 展示了绕过 INI 配置、纯代码构建 `WsConfig` 的完整流程。203 行覆盖了 **C 结构体工厂 → WsConfig::setConnectPeers/DisableSsl → SdkFactory::buildSdk → jsonRpc::getBlockNumber → 100 线程压测** 全链路。是理解底层 RPC 机制和性能压测的标准模板。