@[toc](理解和验证Pbft共识机制 | FISCO BCOS开发系列（三）)


实验环境

两台centos7机器（虚拟机和远程服务器都可以）

- 192.168.80.144

- 192.168.80.145

java1.8

作为一个不太敢相信别人说的话，我总是喜欢用实验或者说用数据来扇自己巴掌，

# 搭建多机多群FISCO BCOS链
剧情回放：我们搭建了两个机构A和B，一个机构4节点一个机构5节点，一共两个群组，A和B均在这两个群组里，呈现一种嵌套的结构。

我将192.168.80.144 作为机构A， 192.168.80.145机器作为机构B

如图，这是我们的拓扑图（嵌套结构）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105124058664.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)
# 理解PBFTfi算法机制
**共识算法分类**

根据是否容忍 拜占庭错误 ，共识算法可分为容错(Crash Fault Tolerance, CFT)类算法和拜占庭容错(Byzantine Fault Tolerance, BFT)类算法：

- CFT类算法 ：普通容错类算法，当系统出现网络、磁盘故障，服务器宕机等普通故障时，仍能针对某个提议达成共识，经典的算法包括Paxos、Raft等，这类算法性能较好、处理速度较快、可以容忍不超过一半的故障节点；

- BFT类算法 ：拜占庭容错类算法，除了容忍系统共识过程中出现的普通故障外，还可容忍部分节点故意欺骗(如伪造交易执行结果)等拜占庭错误，经典算法包括PBFT等，这类算法性能较差，能容忍不超过三分之一的故障节点。

**FISCO BCOS共识算法**

FISCO BCOS基于多群组架构实现了插件化的共识算法，不同群组可运行不同的共识算法，组与组之间的共识过程互不影响，FISCO BCOS目前支持PBFT(Practical Byzantine Fault Tolerance)和Raft(Replication and Fault Tolerant)两种共识算法



**PBFT**(Practical Byzantine Fault Tolerance)共识算法可以在少数节点作恶(如伪造消息)场景中达成共识，它采用签名、签名验证、哈希等密码学算法确保消息传递过程中的防篡改性、防伪造性、不可抵赖性，并优化了前人工作，将拜占庭容错算法复杂度从指数级降低到多项式级别，在一个由(3*f+1)个节点构成的系统中，只要有不少于(2*f+1)个非恶意节点正常工作，该系统就能达成一致性。（f为拜占庭错误节点）

所以如果一个（3*f+1）个节点的系统中只要确保有不少于（2*f+1）个非恶意节点正常工作，该系统就能达成一致性，反之则会停止共识。

因为在（3*f+1）个节点系统中要求了不少于（2*f+1）个非恶意节点正常工作，所以不管是非拜占庭问题（故障节点——断电、网络问题、当机等）还是拜占庭问题（恶意节点）全都被囊括于其中。—— 故可以容忍不超过三分之一的**故障节点**和**恶意节点**，可达到最终一致性。

**模拟故障节点诱发的停止共识：**

根据公式（3*f+1、2*f+1），我们可以提前知道当前节点系统（9个节点）如果拥有了两个故障节点的时候就会停止共识。

**当前情况**


![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105131603531.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)




**停止一个节点**



![在这里插入图片描述](https://img-blog.csdnimg.cn/2021010513164975.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)
其余节点还在共识

**停止两个节点**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105131803608.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)

其余节点还在共识

因为f为2，所以当前节点系统下必须有不少于7个非恶意节点才能正常运行，故此时已经到了我们的节点系统的“极限”了，如果再出现一个故障节点，那么就会停止共识了。

**停止三个节点**
可见，当停止第三个节点的时候，所有的节点都停止了共识。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105131833327.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)

**统计总览：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105131914852.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)
**停止节点/服务器当机属于故障节点呀，那恶意节点呢？恶意节点对链的影响也是这样子的吗？**
**挖坑：因为涉及拜占庭节点（恶意节点）涉及到数据库操作，我放到下篇更新，大致的内容为：拜占庭节点引发的停止共识，以及数据库与群组和节点的关系（探索对比远程数据库 / 本地单个数据库 / 本地多个数据库的选择）**

# 总结
**我搭建的示例并不是一个规范的示范**

在进行多机部署的时候，首先要确保的一点是 —— 单个机构掌握的节点数，应该低于共识算法可容错的数量。我部署了9个节点，只能容忍2个拜占庭节点，所以每个机构下只能有一个节点。这样可以避免机构内部强行修改自己掌握的节点数据，或一个机构的所有节点都意外出错、掉线（比如机房光纤都被挖断了），导致链无法出块。

此处可以参考：

https://mp.weixin.qq.com/s/xt-hRDAkUCCnrodwMkFjnw
# 参考链接
https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/tutorial/installation.html

https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Front/install.html

https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Install/developer.html

https://mp.weixin.qq.com/s/1RGKEdcGhZbjqKv7LBrAVA

https://mp.weixin.qq.com/s?__biz=MzA3MTI5Njg4Mw==&mid=2247485982&idx=1&sn=ffe15ee030ca7b339de5319663aaea85&chksm=9f2ef802a859711466dd3791ca34082572f87dba000f9ada3d5ae50291664be203461f8993f7&scene=21#wechat_redirect

https://mp.weixin.qq.com/s?__biz=MzA3MTI5Njg4Mw==&mid=2247485298&idx=2&sn=c7c0f14ea9d8fe99828b3ee25f9d31e8&chksm=9f2ef56ea8597c78bacd326e19228958f3329c77a3e3fb43609e4691f34d543a89a33fd3be58&scene=21#wechat_redirect

https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/design/consensus/index.htmlfi

https://blog.csdn.net/jfkidear/article/details/81275974

http://pmg.csail.mit.edu/papers/osdi99.pdf

# 关于作者
<div align=center><a href="https://blog.csdn.net/qq_19381989" target="_blank"><img src="https://img-blog.csdnimg.cn/20200427000145250.png" width="40%" /></a></div>

**作者的联系方式：**

微信：`thf056`
qq：1290017556
邮箱：1290017556@qq.com

你也可以通过 <strong><a href="https://github.com/99kies" target="_blank">github</a></strong> | <strong><a href="https://blog.csdn.net/qq_19381989" target="_blank">csdn</a></strong> | <strong><a href="https://weibo.com/99kies" target="_blank">@新浪微博</a></strong> 关注我的动态
