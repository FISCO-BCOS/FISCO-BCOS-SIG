# AMOP 广播消息样例源码走读

> **源码文件**: [broadcast.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/broadcast.cpp)
> **代码行数**: 81行
> **核心功能**: 通过 AMOP 协议向全网广播消息，不等待响应

---

## 任务目标

讲清核心对比：**broadcast 的 Fire-and-Forget 模式与 publish 的 Request-Response 模式的区别**。

---

## 一、源码结构总览

| 行号范围 | 功能 |
|---------|------|
| 1-37 | 头文件引入 + namespace |
| 39-46 | `usage()` |
| 48-69 | 参数校验 + SDK 初始化 |
| 71-78 | **核心：while 循环中 `broadcast()` 调用** |

---

## 二、分步走读

### 第1步：SDK 初始化（行48-69）

```cpp
// ./broadcast ./config_sample.ini topic msg
if (argc < 4) usage();      // 三个参数均为必填

std::string config = argv[1];
std::string topic = argv[2];
std::string msg = argv[3];

auto factory = std::make_shared<SdkFactory>();
auto sdk = factory->buildSdk(config);   // 字符串路径
sdk->start();
```

消息参数 `msg` **必须提供**（`argc < 4`），没有默认值。

### 第2步（核心）：broadcast() — 无回调推送（行71-78）

```cpp
while (true) {
    sdk->amop()->broadcast(
        topic,
        bytesConstRef((byte*)msg.data(), msg.size())
    );
    std::this_thread::sleep_for(std::chrono::milliseconds(5000));
}
```

**与 publish() 的关键差异**：

| 维度 | `broadcast()` | `publish()` |
|------|:---:|:---:|
| 参数数量 | 2 | 4 |
| 回调 | **无** | 3 参数回调 |
| 等待响应 | **不等待**（Fire-and-Forget） | 等待（阻塞或超时） |
| 通信模式 | 单向广播 | 双向请求-响应 |
| 订阅方需 sendResponse | 不需要 | 需要 |

**注意**：SDK 在 while 外创建一次后循环复用，与 publish 相同。

---

## 三、broadcast vs publish 核心代码对比

**broadcast（发送即忘，2 参数）**：

```cpp
sdk->amop()->broadcast(topic, bytesConstRef((byte*)msg.data(), msg.size()));
// 无回调，调用后立即返回
```

**publish（等待响应，4 参数）**：

```cpp
sdk->amop()->publish(
    topic,
    bytesConstRef((byte*)msg.data(), msg.size()),
    -1,
    [](Error::Ptr _error, shared_ptr<WsMessage> _msg, shared_ptr<WsSession> _session) {
        if (_msg->status() == 0) {
            cout << string(_msg->payload()->begin(), _msg->payload()->end());
        }
    }
);
```

---

## 四、适用场景对比

| 场景 | 推荐模式 | 原因 |
|------|---------|------|
| 全链通知（新区块上线） | broadcast | 无需确认，所有节点收到即可 |
| 系统公告 | broadcast | 一对多推送，无需回复 |
| 日志聚合推送 | broadcast | 单向数据流，低延迟 |
| 请求查询节点状态 | publish | 需要获取查询结果 |
| 远程命令下发 | publish | 需要确认执行结果 |
| 心跳检测 | publish | 需要对方响应确认存活 |

---

## 五、运行方法

```bash
./broadcast ./config_sample.ini myTopic "System notification"
```

| 参数 | 说明 |
|------|------|
| config_sample.ini | SDK 配置文件 |
| myTopic | 广播 Topic |
| "System notification" | 广播内容（必填） |

---

## 小结

`broadcast.cpp` 是 AMOP 三个示例中最简洁的（81行），核心就一行 `broadcast(topic, data)`。与 `publish()` 的本质区别在于**通信模式**：broadcast 是单向 Fire-and-Forget，无回调不等响应；publish 是双向 Request-Response，通过 3 参数回调获取 subscribe 端的回复。选择哪个取决于是否需要响应确认。