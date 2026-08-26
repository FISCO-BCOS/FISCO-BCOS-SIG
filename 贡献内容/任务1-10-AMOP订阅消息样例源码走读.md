# AMOP 订阅消息样例源码走读

> **源码文件**: [subscribe.cpp](https://github.com/FISCO-BCOS/FISCO-BCOS/blob/master/bcos-sdk/sample/amop/subscribe.cpp)
> **代码行数**: 101行
> **核心功能**: 通过 AMOP 协议订阅 Topic，接收消息后 echo 回复给发布方

---

## 任务目标

讲清三个核心流程：**Topic 订阅机制**、**消息回调处理逻辑**、**`sendResponse` 响应发送的完整链路**。

---

## 一、源码结构总览

| 行号范围 | 功能 |
|---------|------|
| 1-37 | 头文件引入 + namespace |
| 39-46 | `usage()` 帮助信息 |
| 48-62 | 参数解析 + Topic 集合收集 |
| 64-92 | **核心：setSubCallback + subscribe** |
| 94-100 | 主循环保活 |

---

## 二、分步走读

### 第1步：多 Topic 支持（行56-61）

```cpp
// ./subscribe ./config_sample.ini topic1 topic2 topic3 ...

std::set<std::string> topicList;
for (int i = 2; i < argc; i++) {
    topicList.insert(argv[i]);     // 使用 std::set 去重
}
std::string topic = argv[2];       // 保存第一个 topic 用于日志
```

使用 `std::set` 收集 Topic，自动去重。与 publish 端一次只能发一个 Topic 不同，subscribe 端可**同时订阅多个**。

### 第2步（核心）：setSubCallback + sendResponse（行74-91）

```cpp
sdk->amop()->setSubCallback(
    [&sdk](Error::Ptr _error,                          // 参数1: 错误
           const std::string& _endPoint,                // 参数2: 发布方节点标识
           const std::string& _seq,                     // 参数3: 消息序列号
           bytesConstRef _data,                         // 参数4: 消息体
           std::shared_ptr<WsSession> _session) {       // 参数5: WebSocket 会话

        if (_error) {
            std::cout << "something is wrong"
                      << " code: " << _error->errorCode()
                      << " message: " << _error->errorMessage() << std::endl;
        } else {
            std::cout << "recv message and would echo message ===>>>> "
                      << "endPoint: " << _endPoint
                      << " msg: " << std::string(_data.begin(), _data.end())
                      << std::endl;

            // ★ echo 回复
            sdk->amop()->sendResponse(_endPoint, _seq, _data);
        }
    }
);
```

**回调只有 5 个参数**（无 `WsMessage`、`LocalTopic`、`P2PMessage`）：

| 参数 | 类型 | 含义 |
|------|------|------|
| `_error` | `Error::Ptr` | 错误信息 |
| `_endPoint` | `const string&` | 发布方节点标识 |
| `_seq` | `const string&` | 消息序列号，用于匹配请求-响应 |
| `_data` | `bytesConstRef` | 消息体（零拷贝） |
| `_session` | `shared_ptr<WsSession>` | WebSocket 会话对象 |

**sendResponse 三要素**：

```cpp
sdk->amop()->sendResponse(_endPoint,  // 发布方节点标识
                          _seq,       // 消息序列号
                          _data);     // echo 原消息体
```

本例中直接 echo 原消息，实际应用可返回业务处理结果。这三个参数必须与接收到的值一一对应。

### 第3步：订阅 Topic（行92）

```cpp
sdk->amop()->subscribe(topicList);    // 传入 set<string>
```

**先 setSubCallback，再 subscribe**。顺序颠倒会导致窗口期的消息丢失。

---

## 三、运行方法

```bash
# 订阅单个 Topic
./subscribe ./config_sample.ini myTopic

# 订阅多个 Topic
./subscribe ./config_sample.ini topic1 topic2 topic3
```

配合测试：
```bash
# 终端1: 先启动订阅方
./subscribe ./config_sample.ini myTopic

# 终端2: 启动发布方
./publish ./config_sample.ini myTopic "Hello AMOP"
```

---

## 四、Subscribe 与 Publish 协作图

```
   Subscriber 端                        Publisher 端
   ────────────                        ────────────
setSubCallback(cb)                  publish(topic, data, -1, cb)
subscribe(topicList)                      ↓
     ↓                              等待响应...
收到消息 → cb(_error, endPoint, seq, data, session)
     ↓
sendResponse(endPoint, seq, data)  ─────→  触发回调
     ↓                                    _msg->payload() → 打印
继续等待...
```

---

## 五、关键 API

| API | 作用 |
|-----|------|
| `Sdk::amop()->setSubCallback(cb)` | 设置消息接收回调（5 参数） |
| `Sdk::amop()->subscribe(topicSet)` | 订阅 Topic 集合（`set<string>`） |
| `Sdk::amop()->sendResponse(endPoint, seq, data)` | 向发布方回复消息 |
| `Sdk::amop()->unSubscribe(topicSet)` | 取消订阅 |

---

## 小结

`subscribe.cpp` 展示了 AMOP 订阅端的完整实现：**`std::set` 多 Topic 去重 → setSubCallback 注册 5 参数回调 → echo 原消息 sendResponse**。核心要点：回调参数无 WsMessage/LocalTopic/P2PMessage，`sendResponse` 三个参数直接取自回调中接收到的 `_endPoint`、`_seq`、`_data`。