# 一次 RPC 调用是如何发起的

> 基于 FISCO-BCOS 单机四节点链的 RPC 压力测试案例分析

**参考代码**：[FISCO-BCOS/rpc_test.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/rpc/rpc_test.cpp)

---

## 引言

RPC 是 FISCO-BCOS 节点对外提供服务的核心接口。通过 RPC，应用层可以查询区块链状态（如区块高度、交易信息）、发送交易、管理节点等，而无需直接访问底层存储或共识模块。

本文以 FISCO-BCOS 官方示例 [rpc_test.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/rpc/rpc_test.cpp) 为例，剖析**从命令行参数输入到 getBlockNumber 调用完成**的完整流程，帮助理解 SDK 如何与节点建立连接并发起 RPC 请求。

---

## 一、命令行参数

执行命令格式：

```bash
./rpc <host> <port> <ssl_type> <group_id>
```

参数说明：

| 参数 | 示例值 | 说明 |
|------|--------|------|
| `host` | 127.0.0.1 | 节点 IP 地址 |
| `port` | 20200 | RPC 端口号（四个节点分别为 20200-20203） |
| `ssl_type` | ssl / sm_ssl | 连接类型 |
| `group_id` | group0 | 群组 ID |

使用示例：

```bash
./rpc 127.0.0.1 20200 ssl group0
```

参数通过 `main` 函数的 `argv` 接收：

```cpp
const char* host = argv[1];
int port = atoi(argv[2]);
const char* type = argv[3];
const char* group = argv[4];
```

---

## 二、WsConfig 初始化

`WsConfig` 是连接 FISCO-BCOS 节点的核心配置类，封装了 WebSocket 连接所需的所有参数。

初始化流程在 `initWsConfig` 函数中完成：

```cpp
static std::shared_ptr<bcos::boostssl::ws::WsConfig> initWsConfig(
    std::shared_ptr<bcos_sdk_c_config> config)
{
    auto wsConfig = std::make_shared<bcos::boostssl::ws::WsConfig>();
    
    wsConfig->setModel(bcos::boostssl::ws::WsModel::Client);
    
    auto peers = std::make_shared<bcos::boostssl::ws::EndPoints>();
    for (size_t i = 0; i < config->peers_count; i++)
    {
        auto host = config->peers[i].host;
        auto port = config->peers[i].port;
        bcos::boostssl::NodeIPEndpoint endpoint = bcos::boostssl::NodeIPEndpoint(host, port);
        peers->insert(endpoint);
    }
    wsConfig->setConnectPeers(peers);
    
    wsConfig->setDisableSsl(config->disable_ssl);
    wsConfig->setThreadPoolSize(config->thread_pool_size);
    wsConfig->setReconnectPeriod(config->reconnect_period_ms);
    wsConfig->setHeartbeatPeriod(config->heartbeat_period_ms);
    wsConfig->setSendMsgTimeout(config->message_timeout_ms);
    
    if (!config->disable_ssl)
    {
        auto contextConfig = std::make_shared<bcos::boostssl::context::ContextConfig>();
        contextConfig->setSslType(config->ssl_type);
        contextConfig->setIsCertPath(config->is_cert_path ? true : false);
        bcos::boostssl::context::ContextConfig::CertConfig certConfig;
        certConfig.caCert = config->cert_config.ca_cert;
        certConfig.nodeCert = config->cert_config.node_cert;
        certConfig.nodeKey = config->cert_config.node_key;
        contextConfig->setCertConfig(certConfig);
        wsConfig->setContextConfig(contextConfig);
    }
    return wsConfig;
}
```

关键配置项：

| 配置项 | 说明 |
|--------|------|
| `setModel(Client)` | 设置为客户端模式 |
| `setConnectPeers({host:port})` | 配置连接端点 |
| `setThreadPoolSize(8)` | 线程池大小 |
| `setSendMsgTimeout(10000)` | 消息超时（毫秒） |
| `setDisableSsl(1)` | 禁用 SSL（测试时可设为 1） |

---

## 三、getBlockNumber 调用

SDK 启动后，通过 `jsonRpc()` 获取 RPC 接口，然后发起调用：

```cpp
auto sdk = factory->buildSdk(wsConfig, arg->send_rpc_request_to_highest_block_node);
sdk->start();

auto rpc = sdk->jsonRpc();
rpc->getBlockNumber(
    "group0", "",
    [](bcos::Error::Ptr error, std::shared_ptr<bcos::bytes> resp) {});
```

调用签名：

```cpp
void getBlockNumber(
    const std::string& group,           // 群组 ID
    const std::string& node,            // 节点名称（空字符串表示任意节点）
    std::function<void(Error::Ptr, std::shared_ptr<bytes>)> callback  // 回调函数
);
```

调用流程：

```
  组装 JSON-RPC 请求
   {"jsonrpc": "2.0", "method": "getBlockNumber", "params": ["group0", ""], "id": 1}
          ↓
  通过 WebSocket 发送到节点
          ↓
  节点查询区块链状态
          ↓
  返回 JSON-RPC 响应
   {"jsonrpc": "2.0", "result": "0x1a2b3c", "id": 1}
          ↓
  触发回调函数处理响应
```

---

## 四、压力测试场景

代码创建 100 个并发线程，每个线程循环执行：连接 → 调用 → 断开

```cpp
std::thread threads[100];

for (t = 0; t < 100; t++)
{
    threads[t] = std::thread(thread_function, config);
}

for (t = 0; t < 100; t++)
{
    threads[t].join();
}
```

四节点测试：

```bash
./rpc 127.0.0.1 20200 ssl group0  # 节点0
./rpc 127.0.0.1 20201 ssl group0  # 节点1
./rpc 127.0.0.1 20202 ssl group0  # 节点2
./rpc 127.0.0.1 20203 ssl group0  # 节点3
```

---

## 五、完整调用链路

```
命令行参数 → bcos_sdk_create_config() → initWsConfig() 
    → SdkFactory::buildSdk() → sdk->start() 
    → sdk->jsonRpc() → rpc->getBlockNumber() 
    → 回调处理 → sdk->stop()
```