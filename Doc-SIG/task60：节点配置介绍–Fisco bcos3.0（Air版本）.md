**节点配置介绍--Fisco bcos3.0（Air版本）**

今天我们就来讲讲有关于Air版本Fisco bcos3.0中的节点配置

首先我们要知道Air版本FISCO BCOS包括config目录：该目录包含了FISCO BCOS的配置文件，其中主要的配置文件包括：
application.yaml：配置整个区块链网络的基本信息，如节点地址、端口号、账户等。

chain.yaml：配置区块链网络的ChainID和群组信息，包括节点列表、共识配置、合约地址等。

genesis.json：定义了初始的区块链状态和账本结构，包括初始账户、权限配置等。

sdk.jks：SDK密钥库文件，用于加密和签名交易。

config.genesis：创世块配置文件用于定义初始的链状态和账本结构。它在区块链网络启动时被使用，并确定了整个链的初始状态。

config.ini：节点配置文件它用于定义节点的配置参数和网络连接信息。这个文件通常采用INI文件格式。

现在我们就来说说有关于创世块配置包括的内容

**1. 创世块配置**
创世块（Genesis Block）是区块链网络中的第一个区块，它的特殊性在于它是链上的初始状态。创世块配置指的是用来定义创世块的配置信息，包括初始状态、账户余额、初始合约以及其他链的相关信息。

**1.1 配置链信息**
[chain]配置节点的链信息，该配置下的字段信息，一旦确定就不应该再更改：

- sm_crypto：指定是否使用SM加密算法进行加密，默认为false，即不使用。
- group_id：它指的是群组ID，在一般情况下不需要更改。
- chain_id：它指的是链ID，在一般情况下不需要更改。
![在这里插入图片描述](https://img-blog.csdnimg.cn/ddf439fe7f5d46ffa8736b61d537a00f.png)



**1.2 共识配置**
[consensus]共识相关的配置其中包括：
- consensus_type：指定共识算法类型，此处为PBFT，可根据需求修改共识算法。
- block_tx_count_limit：指定每个区块的最大交易数限制，此处为1000。
- consensus_timeout：指定块共识超时时间，单位为毫秒，此处为3000。
- leader_period：指定每个领导者生成的块数目，此处为1。
- node.x：指定共识节点的节点ID和权重，其中x代表节点的编号。

 [consensus]配置示例如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/97479d018e6e47aca3ca5a6d01523a23.png)


**1.3数据兼容性配置**
[version] FISCO BCOS v3.0.0设计并实现了兼容性框架，可支持数据版本的动态升级 
- compatibility_version：指定兼容版本，可通过setSystemConfig进行动态升级，默认为3.4.0。

该配置项位于[version]下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/9fd01c9162014142a893b2bfb1a6dc7f.png)



**1.4 gas配置**
在区块链中，"gas" 是一种计量单位，用于衡量执行智能合约或发送交易的成本。它代表了为完成特定操作所需的计算和存储资源的消耗量。每个操作所需的 gas 数量是根据其复杂性和资源消耗进行计算的。"gas配置" 是指在区块链网络中设置和调整 gas 相关参数的过程.创世块的[tx].gas_limit可配置交易最大gas限制，默认是3000000000，链初始化完毕后，可通过控制台指令动态调整gas限制。

 [tx]
- gas_limit：指定交易的Gas限制，此处为3000000000。

[tx].gas_limit配置示例如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/e15e5a75471747b582918cd52d18c63b.png)



**1.5 执行模块配置**
[executor]配置项涉及执行相关的创`世`块配置，主要包括：
is_wasm：指定是否使用WASM虚拟机，默认为false，即不使用。
is_auth_check：指定是否进行权限检查，默认为false，即不进行权限检查。
auth_admin_account：指定管理员账户的地址，此处            为:0x927a461126735e637217f748069f1fe135c65367。
is_serial_execute：指定是否串行执行，默认为true。

[executor]配置示例如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/61f6d7bc51384daab2bd62b69ee16b84.png)




​
