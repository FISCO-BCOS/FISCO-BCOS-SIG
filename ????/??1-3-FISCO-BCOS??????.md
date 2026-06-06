# FISCO BCOS 样例代码总览

> 基于 FISCO BCOS v3.x 仓库中 `bcos-sdk/sample`、`fisco-bcos-demo`、`tools` 三个目录的样例代码，帮助新同学快速了解 SDK 能力，找到适合自己的学习入口。

---

## 目录结构总览

```
bcos-sdk/sample/
├── amop/              # AMOP 消息通道（publish / subscribe / broadcast）
├── config/            # SDK 配置文件模板（标准 / 国密）
├── eventsub/          # 合约事件订阅
├── rpc/               # RPC 调用（区块通知 / 压力测试）
├── swig/              # Python SDK 快速入门
├── tars/              # Tars RPC 性能查询
└── tx/                # 交易操作（部署 / 压测 / 签名性能）

fisco-bcos-demo/
├── echo_server_sample.cpp   # P2P Echo 服务端
├── echo_client_sample.cpp   # P2P Echo 客户端

tools/
├── BcosBuilder/       # 节点配置生成工具
├── archive-tool/      # 数据归档工具
├── storage-tool/      # 存储数据查看工具
├── hsm-tool/          # 硬件安全模块工具
├── kms-tool/          # 密钥管理工具
└── template/          # 监控仪表盘模板
```

---

## 一、bcos-sdk/sample — SDK 核心示例

所有样例均遵循统一的三步初始化模式：`SdkFactory::buildSdk(config)` → `sdk->start()` → 调用具体功能 API。

### 1.1 快速导航表

| 文件 | 代码量 | 难度 | 一句话概括 | 源码链接 |
|------|--------|------|-----------|----------|
| `config/config_sample.ini` | 28行 | ★ | SDK 配置文件模板，含 SSL/节点/线程池参数 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/config/config_sample.ini) |
| `rpc/blocknotifier.cpp` | 88行 | ★ | 监听区块高度变化，最简示例 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/rpc/blocknotifier.cpp) |
| `tx/deploy_hello.cpp` | 298行 | ★★★ | **核心示例**：部署 HelloWorld 合约，覆盖配置读取→密钥生成→交易构建→回执解析全流程 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tx/deploy_hello.cpp) |
| `amop/publish.cpp` | 107行 | ★★ | AMOP 消息发布，等待订阅方响应 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/publish.cpp) |
| `amop/subscribe.cpp` | 101行 | ★★★ | AMOP 消息订阅，支持多 Topic，接收消息后 echo 回复 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/subscribe.cpp) |
| `amop/broadcast.cpp` | 81行 | ★★ | AMOP 广播（无回调单向推送） | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/broadcast.cpp) |
| `eventsub/eventsub.cpp` | 128行 | ★★★ | 按区块范围和合约地址过滤订阅事件日志 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/eventsub/eventsub.cpp) |
| `rpc/rpc_test.cpp` | 203行 | ★★★★ | 100 线程极限 RPC 压测，手动构建 WsConfig（不使用 INI 文件） | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/rpc/rpc_test.cpp) |
| `tx/hello_perf.cpp` | 280行 | ★★★★ | 含速率控制的合约调用压测工具 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tx/hello_perf.cpp) |
| `tx/tx_sign_perf.cpp` | 190行 | ★★ | ECDSA vs SM2 签名性能对比测试 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tx/tx_sign_perf.cpp) |
| `tx/random_perf.cpp` | 79行 | ★ | 随机数生成基准（纯计算，无网络 IO） | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tx/random_perf.cpp) |
| `swig/python_client.py` | 149行 | ★ | Python SDK 快速入门 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/swig/python_client.py) |
| `tars/performanceQuery.cpp` | ~320行 | ★★★★★ | Tars 协议下 DAG 批量转账性能查询 | [查看](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/tars/performanceQuery.cpp) |

### 1.2 各模块详解

#### 配置文件：`config_sample.ini`

```ini
[common]
    thread_pool_size = 8           # 网络 IO 线程池
    message_timeout_ms = 10000     # 消息超时(ms)
    send_rpc_request_to_highest_block_node = true

[cert]
    ssl_type = ssl                 # ssl 或 sm_ssl
    ca_path = ./conf
    ca_cert = ca.crt
    sdk_key = sdk.key
    sdk_cert = sdk.crt

[peers]
    node.0 = 127.0.0.1:20200
    node.1 = 127.0.0.1:20201
```

所有样例共享这份配置，只需修改 `[peers]` 中的节点地址和 `[cert]` 中的证书路径即可。

#### AMOP 消息通道

AMOP（Advanced Message Online Protocol）是链上链下安全消息协议，支持三种通信模式：

| 模式 | API | 是否等待响应 | 适用场景 |
|------|-----|:---:|----------|
| **发布-订阅** | `publish()` + `subscribe()` | 是 | 点对点可靠消息、命令下发 |
| **广播** | `broadcast()` | 否 | 一对多通知公告、日志推送 |

**publish 核心代码：**

```cpp
sdk->amop()->publish(
    topic,                                          // 主题名
    bytesConstRef((byte*)msg.data(), msg.size()),   // 消息体
    -1,                                             // 超时(-1=无限等待)
    [](Error::Ptr error, WsMessage::Ptr msg, ...) { // 异步回调
        if (msg->status() == 0) {
            // 收到订阅方响应
            std::cout << string(msg->payload()->begin(), msg->payload()->end());
        }
    }
);
```

**subscribe 双回调机制：**

```cpp
// 1. 设置全局消息处理器
sdk->amop()->setSubCallback(
    [&](Error::Ptr, const string& endPoint, const string& seq,
        bytesConstRef data, ...) {
        // 2. 将原消息 echo 回复给发布方
        sdk->amop()->sendResponse(endPoint, seq, data);
    }
);
// 3. 订阅 Topic（支持多 Topic）
sdk->amop()->subscribe(topicList);
```

关键点：subscribe 必须先于 publish 启动；Topic 需完全匹配（区分大小写）；`publish()` 的 `-1` 超时表示永久等待响应。

**broadcast vs publish：**

```cpp
// broadcast：无回调，纯单向推送
sdk->amop()->broadcast(topic, bytesConstRef(...));

// vs publish：有回调，等待响应
sdk->amop()->publish(topic, ..., callback);
```

#### 事件订阅：`eventsub.cpp`

支持按区块范围和合约地址过滤：

```cpp
auto params = std::make_shared<EventSubParams>();
params->setFromBlock(from);    // -1 表示最新区块
params->setToBlock(to);        // -1 表示最新区块
if (!address.empty()) {
    params->addAddress(address); // 可选：按合约地址过滤
}

sdk->eventSub()->subscribeEvent(
    group, params,
    [](Error::Ptr error, const string& events) {
        // events 为 JSON 字符串，包含 blockNumber、address、topics、data 等
    }
);
```

用法示例：
```bash
./eventsub ./config_sample.ini group -1 -1              # 订阅所有事件
./eventsub ./config_sample.ini group -1 -1 0xabc...     # 按合约地址过滤
./eventsub ./config_sample.ini group 100 200            # 查询历史区间事件
```

#### RPC 调用：`blocknotifier.cpp` 与 `rpc_test.cpp`

**blocknotifier（最简示例）：**

```cpp
auto sdk = factory->buildSdk(config);
sdk->start();
sdk->service()->registerBlockNumberNotifier(
    group, [](const string& g, int64_t n) {
        cout << "block: " << n << endl;
    }
);
// 保持主线程运行
while (true) { std::this_thread::sleep_for(10s); }
```

**rpc_test（高级压测）：**

不使用 INI 文件，纯代码构建 `WsConfig`，然后创建 100 个线程持续创建/销毁 SDK 实例并发调用 `getBlockNumber()`。这是极限压力测试，不适合生产环境参考。

```bash
./rpc 127.0.0.1 20200 ssl group0       # 标准 SSL
./rpc 127.0.0.1 20200 sm_ssl group0    # 国密 SSL
```

#### 交易操作：`tx/` 目录

**deploy_hello — 最完整的合约部署流程（298行）：**

```
配置读取 → 群组信息查询 → 密钥对生成(SM2/ECDSA) → BlockLimit获取
→ 字节码准备(标准/国密) → sendTransaction → 异步回调解析回执 → 提取合约地址
```

关键代码片段：

```cpp
// 根据群组密码类型选择算法和字节码
if (groupInfo->smCryptoType()) {
    keyPairFactory = make_shared<SM2Crypto>();     // 国密 SM2
    hexBin = hwSmBIN;                              // 国密版字节码
} else {
    keyPairFactory = make_shared<Secp256k1Crypto>(); // 标准 ECDSA
    hexBin = hwBIN;                                 // 标准版字节码
}

// 发送部署交易
rpcService->sendTransaction(
    *keyPair, group, "", "",          // 部署时 to="" data=""
    move(*binBytes),                  // 合约字节码
    "", 0, "extraData",
    [&](Error::Ptr e, shared_ptr<bytes> resp) {
        Json::Value root;
        reader.parse(string(resp->begin(), resp->end()), root);
        string addr = root["result"]["contractAddress"].asString();
    }
);
```

用法：`./deploy_hello ./config_sample.ini group0`

**hello_perf — 含速率控制的压测工具：**

先部署合约，再用 `TimeWindowRateLimiter` 控制 QPS，通过 `RateReporter` 统计 TPS，多线程持续调用合约方法。

用法：`./hello_perf ./config_sample.ini group0 16 1024`（16并发 / 1024 QPS）

**tx_sign_perf — 签名性能对比：**

```bash
./tx_sign_perf false 30000    # ECDSA 签名 30000 次
./tx_sign_perf true 30000     # SM2 签名 30000 次
```

**random_perf — 纯计算基准：**

无网络 IO，纯粹测试随机数生成性能，用于建立性能基线。

#### 跨语言：`swig/` 和 `tars/`

- **python_client.py**：通过 SWIG 绑定，提供 Python 调用 SDK 的快速入门示例
- **tars/performanceQuery.cpp**：基于腾讯 Tars RPC 框架的批量 DAG 转账性能查询，涉及 TBB 并行计算，是最高难度的示例

---

## 二、fisco-bcos-demo — P2P 网络层演示

不同于 SDK 层的 RPC/AMOP 接口，这两个示例直接使用 Gateway 层的 P2P 接口，展示节点间最底层的网络通信。

### echo_server_sample.cpp（78行）

通过 `GatewayFactory` 构建 P2P Gateway，注册自定义消息类型（999 = Echo 测试），收到消息后原路返回：

```cpp
GatewayFactory gatewayFactory(nodeConfig->chainId(), "localClient", nullptr);
auto gateway = gatewayFactory.buildGateway(configFilePath, true, nullptr, "localClient");
auto service = dynamic_pointer_cast<Service>(gateway->p2pInterface());
gateway->start();

service->registerHandlerByMsgType(999,
    [](NetworkException e, P2PSession::Ptr session, P2PMessage::Ptr msg) {
        if (e.errorCode()) return;
        msg->setRespPacket();
        session->session()->asyncSendMessage(msg);
    }
);
```

### echo_client_sample.cpp（111行）

使用 `RateLimiter` 控制发包速率，向指定节点发送自定义消息类型的 P2P 数据包，统计往返延迟（RTT）：

```bash
./echo-client-sample 10 127.0.0.1 30303 1024
# 参数: QPS(Mbps) 服务端地址 端口 载荷(KB)
```

输出示例：

```
### payLoad:1048576  ### packetQPS: 10
receiveResponse, timecost:15, msgSize:1048576
receiveResponse, timecost:16, msgSize:1048576
```

### Echo Demo 与 SDK 的关系

| 层面 | SDK 示例（bcos-sdk/sample） | Echo 示例（fisco-bcos-demo） |
|------|---------------------------|----------------------------|
| 通信层级 | RPC / AMOP（应用层协议） | P2P Session（传输层协议） |
| 初始化方式 | `SdkFactory::buildSdk(config.ini)` | `GatewayFactory::buildGateway(config.ini)` |
| 适用场景 | 交易、合约调用、事件订阅 | 自定义 P2P 协议开发、网络性能测试 |
| 难度 | ★ ~ ★★★★ | ★★ |

---

## 三、tools — 运维辅助工具

tools 目录提供节点运维、数据管理和安全相关的辅助工具。

| 工具 | 主要文件 | 用途说明 |
|------|---------|----------|
| **BcosBuilder** | `src/common/utilities.py` | Python 脚本，用于生成节点配置文件（genesis、config.ini、tars 配置），含配置模板 |
| **archive-tool** | `archiveTool.cpp` | 区块链历史数据归档服务，将旧区块数据从活跃存储迁移到归档存储 |
| **storage-tool** | `storageTool.cpp` / `reader.cpp` | 直接读取 RocksDB/TiKV 底层存储数据，用于调试和数据巡检 |
| **hsm-tool** | `encryptCertFile.cpp` | 与硬件安全模块（HSM）集成，对证书文件进行加密处理 |
| **kms-tool** | `KmsTool.cpp` | 对接密钥管理系统（KMS），管理节点的签名密钥 |
| **template** | `Dashboard.json` | Grafana 仪表盘模板，用于监控节点运行状态 |

### BcosBuilder 配置生成

BcosBuilder 读取 Python 配置文件，自动生成：
- `config.genesis` — 创世块配置（共识节点列表、群组配置）
- `config.ini.node` — 节点配置文件模板
- `config.ini.rpc` — RPC 服务配置模板
- `tars_node.conf` / `tars_rpc.conf` — Tars 服务配置

这是搭建 FISCO BCOS 网络的第一步工具。

### storage-tool 数据巡检

直接读取节点存储层（RocksDB）的数据，用于：
- 查看指定区块的交易列表
- 检查合约状态存储
- 验证数据完整性
- 调试存储层异常

这些工具适合运维和深入排查问题时使用，与开发层面的 SDK 示例互补。

---

## 四、推荐学习路径

针对不同背景的新同学，推荐以下入口：

### 路径 A：初次接触区块链开发

```
Step 1: config_sample.ini        理解 SDK 如何连接节点
Step 2: blocknotifier.cpp        体验"连接→回调→后台运行"的最简模式
Step 3: deploy_hello.cpp         掌握合约部署全流程（最重要）
Step 4: publish.cpp + subscribe.cpp  理解链上链下消息通信
```

### 路径 B：有以太坊/web3 经验的开发者

```
Step 1: deploy_hello.cpp         对比 sendTransaction 与 web3 的差异
Step 2: eventsub.cpp             理解事件过滤机制（from/to/address）
Step 3: hello_perf.cpp           压测工具，评估网络吞吐上限
Step 4: rpc_test.cpp             手动构建 WsConfig，绕过 INI 配置
Step 5: python_client.py         快速用 Python 原型验证
```

### 路径 C：区块链运维 / DevOps

```
Step 1: tools/BcosBuilder/       搭建测试网络
Step 2: blocknotifier.cpp        开发区块监控、告警
Step 3: tools/storage-tool/      掌握节点底层数据巡检
Step 4: echo_server_sample.cpp   理解 P2P 网络层，用于网络故障诊断
Step 5: tools/template/          部署 Grafana 监控面板
```

### 路径 D：性能测试与调优

```
Step 1: random_perf.cpp          建立纯计算性能基线
Step 2: tx_sign_perf.cpp         评估密码学开销（SM2 vs ECDSA）
Step 3: hello_perf.cpp           合约调用吞吐压测
Step 4: echo_client_sample.cpp   网络层 RTT 延迟测量
Step 5: rpc_test.cpp             极限并发稳定性测试
```

---

## 五、所有命令速查

| 操作 | 命令 |
|------|------|
| 部署 HelloWorld | `./deploy_hello ./config_sample.ini group0` |
| 监控区块高度 | `./blocknotifier ./config_sample.ini group` |
| AMOP 发布消息 | `./publish ./config_sample.ini topic HelloWorld` |
| AMOP 订阅消息 | `./subscribe ./config_sample.ini topic` |
| AMOP 广播消息 | `./broadcast ./config_sample.ini topic HelloWorld` |
| 事件订阅 | `./eventsub ./config_sample.ini group -1 -1` |
| RPC 压力测试 | `./rpc 127.0.0.1 20200 ssl group0` |
| HelloWorld 压测 | `./hello_perf ./config_sample.ini group0 16 1024` |
| 签名性能测试 | `./tx_sign_perf true 30000` |
| 随机数基准测试 | `./random_perf 30000` |
| Echo 客户端 | `./echo-client-sample 10 127.0.0.1 30303 1024` |

---

## 附录：核心 API 速查

| API | 所属模块 | 用途 |
|-----|---------|------|
| `SdkFactory::buildSdk(config)` | SDK 初始化 | 从 INI 文件构建 SDK |
| `Sdk::start()` | SDK 初始化 | 启动 WebSocket 连接 |
| `Service::registerBlockNumberNotifier()` | RPC | 注册区块高度回调 |
| `Service::getGroupInfo(group)` | RPC | 查询群组信息（含国密类型） |
| `Service::getBlockLimit()` | RPC | 获取交易有效期 |
| `JsonRpcService::sendTransaction()` | RPC | 发送交易（部署/调用） |
| `AMOP::publish(topic, data, timeout, cb)` | AMOP | 发布消息，等待响应 |
| `AMOP::subscribe(topicList)` | AMOP | 订阅 Topic 列表 |
| `AMOP::broadcast(topic, data)` | AMOP | 广播消息（无响应） |
| `AMOP::setSubCallback(cb)` | AMOP | 设置消息接收回调 |
| `AMOP::sendResponse(endPoint, seq, data)` | AMOP | 向发布方发送回复 |
| `EventSub::subscribeEvent(group, params, cb)` | 事件 | 订阅合约事件 |
| `GatewayFactory::buildGateway()` | P2P | 构建 P2P Gateway |
| `Service::registerHandlerByMsgType()` | P2P | 注册自定义消息处理器 |
| `Service::asyncSendMessageByEndPoint()` | P2P | 向指定节点发送 P2P 消息 |

---

## 相关资源

- FISCO BCOS 源码仓库：[https://github.com/FISCO-BCOS/FISCO-BCOS](https://github.com/FISCO-BCOS/FISCO-BCOS)
- 官方技术文档：[https://fisco-bcos-documentation.readthedocs.io/](https://fisco-bcos-documentation.readthedocs.io/)
- Java SDK：[https://github.com/FISCO-BCOS/java-sdk](https://github.com/FISCO-BCOS/java-sdk)
- Python SDK：[https://github.com/FISCO-BCOS/python-sdk](https://github.com/FISCO-BCOS/python-sdk)