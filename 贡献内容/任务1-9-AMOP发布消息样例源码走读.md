# AMOP 发布消息样例源码走读

> **源码文件**: [publish.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/publish.cpp)
> **代码行数**: 107行
> **核心功能**: 通过 AMOP 协议向指定 Topic 发布消息，并异步等待订阅方响应

---

## 任务目标

讲清三个核心要点：**配置读取与 SDK 初始化**、**topic/message 参数的含义**、**发布回调的异步响应处理流程**。

---

## 一、源码结构总览

| 行号范围 | 功能 |
|---------|------|
| 1-37 | 头文件引入 + namespace |
| 40-47 | `usage()` 帮助信息 |
| 49-69 | 参数校验 + SDK 初始化 |
| 71-104 | **核心：while 循环中 `publish()` 调用** |

---

## 二、分步走读

### 第1步：参数校验与 SDK 初始化（行49-69）

```cpp
// ./publish ./config_sample.ini topic msg
// 三个参数均为必填
if (argc < 4) {
    usage();
}

std::string config = argv[1];
std::string topic = argv[2];
std::string msg = argv[3];

auto factory = std::make_shared<SdkFactory>();
auto sdk = factory->buildSdk(config);   // 字符串路径
sdk->start();
```

消息参数 `msg` **必须提供**（`argc < 4`），没有默认值。

### 第2步（核心）：publish() 循环发送（行71-103）

```cpp
while (true) {
    sdk->amop()->publish(
        topic,                                               // 1. Topic
        bytesConstRef((byte*)msg.data(), msg.size()),        // 2. 消息体（零拷贝）
        -1,                                                  // 3. 超时（-1=无限等待）
        // ===== 异步回调（3 个参数）=====
        [](Error::Ptr _error,
           std::shared_ptr<WsMessage> _msg,
           std::shared_ptr<WsSession> _session) {

            if (_error) {
                std::cout << "something is wrong"
                          << " code: " << _error->errorCode()
                          << " message: " << _error->errorMessage() << std::endl;
                return;
            }

            if (_msg->status() != 0) {
                std::cout << "something is wrong"
                          << " status: " << _msg->status()
                          << " message: " << std::string(_msg->payload()->begin(),
                                                         _msg->payload()->end())
                          << std::endl;
                return;
            }

            std::cout << "recv response message ===>>>> "
                      << std::string(_msg->payload()->begin(), _msg->payload()->end())
                      << std::endl;
        }
    );
    std::this_thread::sleep_for(std::chrono::milliseconds(5000));
}
```

**关键点**：

1. **SDK 在 while 循环外创建，循环内复用**——不是每次循环重建
2. **回调只有 3 个参数**：`Error`、`WsMessage`、`WsSession`，无 `LocalTopic` 和 `P2PMessage`
3. **超时 = -1**：无限等待订阅方响应
4. **响应提取**：先检查 `_msg->status() != 0`，再通过 `_msg->payload()` 获取响应内容
5. **间隔 5 秒**：`sleep_for(5000ms)` 控制发送频率

---

## 三、运行方法

```bash
./publish ./config_sample.ini myTopic "Hello from publisher"
```

| 参数 | 说明 |
|------|------|
| config_sample.ini | SDK 配置文件 |
| myTopic | 目标 Topic 名称 |
| "Hello from publisher" | 消息内容（必填） |

---

## 四、publish 调用流程图

```
main() → 解析 topic, msg
  ↓
SdkFactory::buildSdk(config) → sdk->start()     ← 只创建一次
  ↓
while(true):
  amop()->publish(topic, bytesConstRef(msg), -1, callback)
    │                                      ↓
    │                          AMOP 发送消息到网络
    │                                      ↓
    │                          等待 subscribe 端匹配 Topic
    │                                      ↓
    ↓ 返回                         收到响应 → 触发 callback
    │                                      ↓
  sleep(5000ms)                          _error → 打印错误
                                         _msg->status() → _msg->payload()
                                              ↓
                                         打印 "recv response message: ..."
```

---

## 五、关键 API

| API | 作用 |
|-----|------|
| `Sdk::amop()->publish(topic, data, timeout, callback)` | 发布消息并等待响应 |
| `bytesConstRef(data, size)` | 零拷贝字节引用 |
| `WsMessage::status()` | 响应状态，0 = 成功 |
| `WsMessage::payload()` | 响应消息体，返回 `shared_ptr<bytes>` |

---

## 小结

`publish.cpp` 展示了 AMOP 发布端实现：107 行覆盖 **topic/msg 必填 → 零拷贝发送 → 无限超时 → 3 参数回调提取响应** 的完整流程。SDK 在 while 外创建一次后循环复用，`status() != 0` 的判断不可省略。