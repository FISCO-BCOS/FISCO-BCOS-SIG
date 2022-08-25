
## 小组目标

FISCO BCOS节点架构与存储专项小组（Architecture and Storage Special Interest Group, AS-SIG）围绕FISCO BCOS底层平台架构与存储进行持续的研究、开发与优化工作，实现模块间解耦，模块内实现可定制的灵活架构。

关于下一代的FISCO BCOS设计目标与背景，请[参考这里](https://zhuanlan.zhihu.com/p/342043241)，更具体的小组目标包括：

- 抽象一个区块链工厂框架，可以插件化配置不同功能模块的引擎
- 功能模块组件化，独立维护升级
- 解决区块链存储扩展问题，包括快照、数据恢复、扩容等
- 支持更多不同类型的账本，包括UTXO、Account、DAG以及原生隐私账本等

## 加入小组

欢迎所有对区块链底层技术有探究精神的同学参与小组讨论，在此基础上如果希望更深入的参与下一代FISCO BCOS的设计开发，请填写[申请表单](https://wj.qq.com/s2/7773399/ee41)或联系小助手（微信号：FISCOBCOS010）申请加入小组，我们会基于报名信息对申请者做出一定的筛选，希望报名深度参与的你愿意遵守下述约定：

- 认同开源社区价值观，遵守社区运营规则
- 积极参与小组活动，愿意贡献自己的力量，帮助产品迭代进化
- 对未知事物充满好奇，拥有开放的视野和乐于探索的心态，能够持续投入学习
- 具有责任意识。当你承担了社区某项任务，你便需要开始为任务进度、任务产出及后续维护负责
- 尊重其他成员，平等友好的交流沟通，理性的讨论不同观点

## 小组成员

| **姓名** | **所属单位**  | **职业**   | **擅长技能** | **个人简介/主页** |
| -------- | ------------ | --------- | -------------- | -------------------- |
| 白兴强  | FISCO BCOS |   核心开发者、AS-SIG组长 | 区块链底层开发，存储、智能合约语言与虚拟机、网络等，C/C++/shell/Golang/rust | 编译器驱动开发。https://github.com/bxq2011hust|
| 林宣名  | 厦门哈希科技 |  厦门哈希科技联合创始人& CTO| 区块链底层开发、区块链应用架构，精通 C#，熟练使用java、python、go、rust、node.js，前端vue.js、angular.js等|FISCO BCOS C# SDK 作者， 集美大学区块链产学研导师，CCF YOCSEF委员。CSDN签约讲师，BSN合格开发者，FISCO BCOS年度开源贡献者，元磁之力区块链社区发起者。技术极客，热衷开源和生产实践的技术赋能。关注前沿技术，专注领域：区块链、AI。https://github.com/linbin524|
| 黄清昊  | 腾讯 | 普通打工人 | 熟悉 Traffic Infra 相关开发。 熟悉Golang/C++/JavaScript。 | 对存储、网络、分布式协议感兴趣。 前阿里云前端工程师，前字节跳动服务端工程师。[wfnuser](https://github.com/wfnuser/)|
| 梁睿 | 成都如清科技 | 后台开发 | 服务端开发、熟悉C++ | 后台开发、预研，C++秃击者，刚入门还在用吃奶劲学习的技术小白[stingliang](https://gitee.com/stingliang) |
| 黄一 | 四川万物数创科技 | 后端/桌面开发 | windows PE/驱动，区块链智能合约，BIM相关平台，熟悉MASM/C/C++/C#，使用过Golang/Lua/Python/Rust/MaxScript/Lisp | 对区块链底层原理感兴趣，前逆向工程师，现BIM+边缘区块链开发工程师，BIM轻量化工程师[saberdance](https://github.com/saberdance) |
| 梁腾 | 上海新致软件 | 技术经理 | 云原生，区块链智能合约，熟悉java，shell，JavaScript，前端vue.js,angular.js等 | 对区块链底层感兴趣，全栈工程师 [ltyuanmu](https://github.com/ltyuanmu) |
| 刘仲明 | 中兴协力（山东）教育科技集团 | 全栈开发 | 擅长java，熟悉go语言、solidity语言，熟悉kubernetes、docker等容器技术 | [aiottots](https://github.com/aiottots) |
| 郭锐 | FISCO BCOS | WeCross/FISCO BCOS核心开发者 | 跨链开发，区块链底层开发，docker容器，c++/Java/Vue.js/shell/python | https://github.com/kyonRay |



## 小组会议

小组成员根据实际需求组织会议，包括但不限于技术分享、方案讨论、代码Review等。

- 1月16日，小组初始6名成员（黄一、林宣名、梁睿、黄清昊、李辉忠、白兴强）第一次例会，会议主要促进现有成员之间相互了解。
- 1月23日，小组组织第一次FISCO BCOS源码阅读，依据[带你读源码：四大视角多维走读区块链源码](https://mp.weixin.qq.com/s/Erm1XV9n9gmwVn6-8Tda7w)阅读初始化模块。
- 2月6日，小组组织第二次FISCO BCOS源码阅读，由梁睿分享阅读存储模块代码与接入其他数据库的实践经验，[会议录屏请参考](https://meeting.tencent.com/user-center/shared-record-info?id=b8d88f3d-af71-4227-a790-24c9b161bbad&reload=1)。
- 4月10日，小组组织第四次FISCO BCOS源码阅读，由郭锐分享执行模块代码的阅读，[会议录屏请参考](https://meeting.tencent.com/user-center/shared-record-info?id=36da6144-633e-466d-865f-66b612efcc7c)

