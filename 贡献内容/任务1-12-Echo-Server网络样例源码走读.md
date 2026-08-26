# Echo Server 网络样例源码走读

> **源码文件**: [echo_server_sample.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/fisco-bcos-demo/echo_server_sample.cpp)
> **代码行数**: 78行
> **核心功能**: 基于 Gateway 底层 P2P 接口实现的 Echo 服务器，将收到的消息直接原路返回

---

## 任务目标

讲清三个核心要点：**Gateway 层初始化（与 SDK 层的区别）**、**自定义消息处理器的注册与回调机制**、**消息回显的极简实现**。

---

## 一、源码结构总览

| 行号范围 | 功能 |
|---------|------|
| 1-27 | 头文件引入 |
| 32-38 | `usage()` |
| 40-57 | **核心：GatewayFactory 初始化 + Gateway 启动** |
| 60-71 | **核心：registerHandlerByMsgType 注册回显处理器** |
| 72-77 | 主循环保持 Gateway 运行 |

---

## 二、分步走读

### 第1步：配置文件加载（行40-51）

```cpp
g_BCOSConfig.setCodec(std::make_shared<ProtocolInfoCodecImpl>());
auto keyFactory = std::make_shared<KeyFactoryImpl>();
auto nodeConfig = std::make_shared<NodeConfig>();

std::string configFilePath = "config.ini";    // ★ 硬编码
nodeConfig->loadConfig(configFilePath);

boost::property_tree::ptree pt;
boost::property_tree::read_ini(configFilePath, pt);
auto logInitializer = std::make_shared<BoostLogInitializer>();
logInitializer->initLog(pt);
```

配置文件路径**硬编码为 `"config.ini"`**，不通过命令行参数传入。`NodeConfig::loadConfig()` 负责解析 P2P 和 Gateway 相关配置。

### 第2步（核心）：GatewayFactory 初始化（行53-57）

```cpp
GatewayFactory gatewayFactory(nodeConfig->chainId(), "localClient", nullptr);
auto gateway = gatewayFactory.buildGateway(configFilePath, true, nullptr, "localClient");
auto service = std::dynamic_pointer_cast<Service>(gateway->p2pInterface());
gateway->start();
```

**与 SDK 初始化的关键差异**：

| 维度 | SDK 示例（bcos-sdk/sample） | Echo 示例（fisco-bcos-demo） |
|------|---------------------------|----------------------------|
| 工厂类 | `SdkFactory` | `GatewayFactory` |
| 构建产物 | `Sdk` 对象 | `Gateway` 对象 |
| 通信层级 | RPC / AMOP（应用层） | P2P Session（传输层） |
| 接口获取 | `sdk->service()` / `sdk->jsonRpc()` | `gateway->p2pInterface()` → `Service` |

### 第3步（核心）：回显处理器（行60-71）

```cpp
service->registerHandlerByMsgType(
    999,                    // 自定义消息类型编号
    [](NetworkException _e,
       std::shared_ptr<P2PSession> _session,
       P2PMessage::Ptr _msg) {

        if (_e.errorCode()) return;

        _msg->setRespPacket();                    // ★ 直接标记为响应包
        _session->session()->asyncSendMessage(_msg);  // ★ 原消息发回
    }
);
```

**回调只有 3 个参数**（无 `NodeID`、无单独 `bytes`）：

| 参数 | 类型 | 含义 |
|------|------|------|
| `_e` | `NetworkException` | 异常信息，`errorCode()!=0` 则跳过 |
| `_session` | `shared_ptr<P2PSession>` | P2P 会话 |
| `_msg` | `P2PMessage::Ptr` | 收到的消息对象 |

**极简回显逻辑**（两步）：

1. `_msg->setRespPacket()` —— 在原有消息对象上标记为响应包
2. `_session->session()->asyncSendMessage(_msg)` —— 原封不动发送回去

**没有**创建新的 `P2PMessageFactory`，**没有** `setPayload()`，**没有**构建新消息——直接复用收到的 `_msg`。

---

## 三、运行方法

```bash
# 启动 Echo Server（需要当前目录有 config.ini）
./echo-server-sample

# 配合 echo_client 测试
./echo-client-sample 100 127.0.0.1 30300 1024
```

---

## 四、Echo 请求-响应流程图

```
     Echo Client                          Echo Server
     ──────────                         ────────────
  GatewayFactory::buildGateway()      GatewayFactory::buildGateway()
         ↓                                    ↓
  gateway->start()                     registerHandlerByMsgType(999, cb)
         ↓                                    ↓
  asyncSendMessageByEndPoint           等待 P2P 消息...
    (msgType=999, payload=...)                ↓
         ↓                           收到 msgType=999 → cb(_e, session, _msg)
    等待响应...                              ↓
         ↓                             _e.errorCode()==0
    收到响应 ←───────────────────      _msg->setRespPacket()
         ↓                             asyncSendMessage(_msg)  ← 回显
  receiveResponse                            ↓
                                         继续等待...
```

---

## 五、与 SDK 层编程模型对比

| SDK 层（bcos-sdk/sample） | Gateway 层（fisco-bcos-demo） |
|--------------------------|-----------------------------|
| `SdkFactory::buildSdk(config)` | `GatewayFactory::buildGateway(...)` |
| `registerBlockNumberNotifier(group, cb)` | `registerHandlerByMsgType(999, cb)` |
| `sendTransaction(...)` | `asyncSendMessage(msg)` |
| `amop()->publish(...)` | 直接 P2P 消息 |
| 回调含 Group、Error、bytes | 回调含 NetworkException、P2PSession、P2PMessage |

---

## 小结

`echo_server_sample.cpp` 是 FISCO BCOS 中唯一直接操作 Gateway 层 P2P 接口的示例。78 行代码展示了极简的回显实现：**`registerHandlerByMsgType(999, cb)` 注册处理器 → `_msg->setRespPacket()` 标记响应 → `asyncSendMessage(_msg)` 原消息发回**。不需要消息工厂，不需要构建新消息对象。配置文件路径硬编码为 `"config.ini"`。