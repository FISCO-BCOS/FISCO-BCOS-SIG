# Fisco Bcos V3.0新版本解读--关键特性

## 搭链可分为三种形态

* 轻便Air版：拥有与 v2.0版本相同的形态，所有功能集成一个区块链节点中（all-in-one）。该架构简单，可快速部署在任意环境中。可以用它进行区块链入门、开发、测试、POC验证等工作。
V3.0的节点目录也是有所更新，从整体结构上观察，与V2.0比较更为整洁了，一目了然。并且还添加了用户私钥和公钥，直接使用创建账户的脚本根据pem私钥文件计算出账户地址,在每个node*/conf配置文件中新增了ssl证书和私钥等一系列有关安全的加密文件,同时还将cert文件夹替换为ca文件夹用于存储和账户有关的信息。
```shell
fisco@ubuntu:~/fisco/nodes$ tree
.
├── 127.0.0.1
│   ├── ca
│   │   ├── ca.crt
│   │   ├── ca.key
│   │   └── cert.cnf
│   ├── fisco-bcos
│   ├── node0
│   │   ├── conf
│   │   │   ├── ca.crt
│   │   │   ├── cert.cnf
│   │   │   ├── node.nodeid
│   │   │   ├── node.pem
│   │   │   ├── ssl.crt
│   │   │   ├── ssl.key
│   │   │   └── ssl.nodeid
│   │   ├── config.genesis
│   │   ├── config.ini
│   │   ├── data
│   │   │   ├── 000005.log
│   │   │   ├── consensus_log
│   │   │   │   ├── 000005.log
│   │   │   │   ├── CURRENT
│   │   │   │   ├── IDENTITY
│   │   │   │   ├── LOCK
│   │   │   │   ├── LOG
│   │   │   │   ├── MANIFEST-000004
│   │   │   │   └── OPTIONS-000007
│   │   │   ├── CURRENT
│   │   │   ├── IDENTITY
│   │   │   ├── LOCK
│   │   │   ├── LOG
│   │   │   ├── MANIFEST-000004
│   │   │   └── OPTIONS-000007
│   │   ├── log
│   │   │   ├── log_2023092005.56.log
│   │   │   └── log_2023092006.00.log
│   │   ├── nodes.json
│   │   ├── nohup.out
│   │   ├── start.sh
│   │   └── stop.sh
│   │── node1
│   │   ...
│   │
│   │── node2
│   │   ...
│   │
│   │── node3
│   │   ...
│   │
│   │── sdk
│   │   ├── ca.crt
│   │   ├── cert.cnf
│   │   ├── sdk.crt
│   │   ├── sdk.key
│   │   └── sdk.nodeid
│   ├── start_all.sh
│   └── stop_all.sh
└── ca
    ├── accounts
    │   ├── 0x470f1fdf25ed11c294a03bc14d7d43750d4bb398.pem
    │   ├── 0x470f1fdf25ed11c294a03bc14d7d43750d4bb398.pem.pub
    │   ├── 0x5159c20fcf86f6c1cec0924c0b21a4ea8cfd2686.pem
    │   └── 0x5159c20fcf86f6c1cec0924c0b21a4ea8cfd2686.pem.pub
    ├── ca.crt
    ├── ca.key
    ├── ca.srl
    └── cert.cnf
```

* 专业Pro版：该架构通过将区块链节点的接入层模块独立为进程，在实现接入层与核心模块分区部署的同时，让区块链核心功模块以多群组方式扩展。该架构实现了分区隔离，拥有较强的业务拓展能力，适合有持续业务扩展的生产环境。

* 大容量Max版：该架构在Pro版的基础上提供链的核心模块主备切换的能力，并可通过多机部署交易执行器和接入分布式存储TiKV，实现计算与存储的平行拓展。该架构中的一个节点由一系列微服务组成，但它依赖较高的运维能力，适合需要海量计算和存储的场景。

FISCO BCOS V3.0中不同的区块链形态区分了不同的使用者，使用者可以根据自己学习或工作的需要，部署合适的区块链形态，达到事半功倍的效果。

## Pipeline：区块流水线，连续且紧凑地生成区块
+ 区块生成过程可拆分成四个阶段：打包、共识、执行、落盘。在过往设计中，系统需要等待上一个区块完成四个阶段后才可进入下一个区块的生成，效率较低，出块慢。本版本采用流水线设计，让相邻区块的四个阶段前后交叠在一起。如：区块103在打包的同时，102在共识，101在执行，100在落盘，当104区块链打包时，102就在落盘依次类推。

通过采用区块流水线的设计，极大地减少了区块生成的等待时间，进而提高了连续出块的速度。系统可以更紧凑地进行区块生成的各个阶段，从而最大化地提高计算机和资源的利用效率。这项改进对于提升区块链的性能和吞吐量具有重要意义，同时也为系统提供了更高的扩展性和可靠性。区块流水线的引入，为FISCO BCOS的区块生成过程注入了更多的灵活性和效率，使得区块链网络能够更好地适应各类应用场景的需求。

## WBC-Liquid：用Rust写合约
+ 在FISCO BCOS的持续发展下，对于支持智能合约的语言也有了进一步拓展。在本版本中不仅支持Soldity语言，还支持用Rust写合约。WBC-Liquid是微众区块链开发的基于Rust的智能合约编程语言，借助Rust语言特性，能够实现比Solidity语言更强大的编程功能，拥有一些强大的特性，例如：内存安全、并发安全、高性能、跨平台支持、生态系统强大，这对于熟悉Rust语言的学习者来说将是一个福音。
以下是一个简易的合约示例：
```Rust
use ink_lang::contract;
use ink_core::storage;

// 定义转账智能合约
#[contract]
mod transfer {
    use super::*;

    // Transfer结构体,存储转账合约的状态
    #[storage]
    struct Transfer {
      // 将账户地址与余额进行映射
        balances: storage::mapping::HashMap<AccountId, Balance>,
    }

    impl Transfer {
        #[ink(constructor)]
        // 构造函数
        fn new(&mut self) {
            // 初始化合约，创建一个空的账户余额映射
            self.balances = storage::mapping::HashMap::new();
        }

        #[ink(message)]
        fn transfer(&mut self, to: AccountId, value: Balance) -> bool {
            // 获取调用者的账户地址
            let caller = self.env().caller();

            // 获取调用者账户余额并检查是否足够进行转账
            let sender_balance = self.balance_of(caller);
            if sender_balance < value {
                return false;
            }

            // 更新调用者账户余额
            self.balances.insert(caller, sender_balance - value);

            // 更新接收者账户余额
            let receiver_balance = self.balance_of(to);
            self.balances.insert(to, receiver_balance + value);

            // 转账成功返回true
            true
        }

        #[ink(message)]
        fn balance_of(&self, owner: AccountId) -> Balance {
            // 获取指定账户的余额
            *self.balances.get(&owner).unwrap_or(&0)
        }
    }
}
```

## +TiKV：分布式事务性提交，支撑海量存储
+ v3.0稳定版集成TiKV存储引擎，并且在其基础上二次开发，支持分布式事务性提交，结合DMC多计算实例，充分发挥存储性能，支撑海量数据上链。同时，本版本引入KeyPage机制，参考内存页的缓存机制，将key-value组织成一个页，并将整个页作为一个单元进行存取，当需要读取或更新某个key的值时，就可以直接通过页级别的操作规模来获取和修改数据，解决了以往采用key-value的方式存储数据时造成的存储数据零散的问题，提升了数据访问局部性，将大批量数据组织成页的方式存储，可以更高效地读取和写入数据，更适合大批量数据存取。

通过以上的改进和优化，FISCO BCOS v3.0在存储引擎和数据结构方面进行了重要升级，以提供更高效、可扩展和适用于海量数据上链的能力。这将有助于进一步提升系统的性能和吞吐量，并满足在实际场景中处理大规模数据的需求。

## 区块链文件系统：所见即所得的合约数据管理
+ 将区块链中的合约资源使用树形文件目录形式进行组织划分管理，使得更加直观和方便，而无需深入了解底层的区块链技术细节。支持通过区块链文件系统管理链上资源，可像操作文件系统一样管理链上的合约，并通过合约的路径进行调用，用户可通过控制台操作智能合约类似于Linux终端的体验命令包括：pwd、cd、ls、tree、mkdir、In。

```shell
# 使用ls查看当前目录下的资源
[group0]: /apps> ls
hello_bfs

# 使用mkdir命令创建目录
[group0]: /apps> mkdir hello_dir
Success

# 使用cd命令返回上一级目录
[group0]: /apps> cd ..

# 使用ln命令创建合约的软链接
[group0]: /> ln /apps/hello_bfs/v2 6849f21d1e455e9f0712b1e99fa4fcd23758e8f1
{
  "code":0,
  "msg":"Success"
}
[group0]: /> cd /apps

# 直接对路径中合约的软链接进行调用
[group0]: /apps> call ./hello_bfs/v2 get
----------------------------------------------------------
Return code: 0
description: transaction executed successfully
Return message: Success
----------------------------------------------------------
Return value size:1
Return types: (string)
Return values:(Hello, World!)
----------------------------------------------------------

# ls软链接查看映射的地址
[group0]: /> ls ./hello_bfs/v2
v2 -> 6849f21d1e455e9f0712b1e99fa4fcd23758e8f1

# 使用tree命令展示目录结构
[group0]: /> tree /apps
/apps
├─hello_bfs
│ ├─v1
│ └─v2
└─hello_dir
2 directory, 2 contracts.
```

## SDK基础库：更方便的全平台国密接入
+ SDK基础库是一个软件开发工具包，它提供了一些基础组件，使得开发人员在不同平台、不同操作系统和编程语言下能够更方便地接入国密技术。
v3.0稳定版构建了通用国密基础组件，将国密算法、国密通信协议、国产密码机接入协议与FISCO BCOS的区块链基础数据结构封装于其中，基于该基础组件可快速开发出不同平台、不同操作系统和不同编程语言的SDK，相较于V2.0新增了SDK：Rust SDK、C SDK、CPP SDK、大大提升研发效率。

SDK基础库的增添为开发者提供了更为便利和灵活的接入方式，使得他们可以更容易地在各种环境下构建和部署与FISCO BCOS相关的应用程序，同时确保安全性和兼容性。无论是移动设备还是桌面端，无论是使用哪种编程语言，开发者都能够更加简便地接入和使用国密算法。

## 交易并行冲突分析工具：自动生成交易冲突变量
+ 在v2.0版本中要实现并行交易，需要在写合约时手动指定交易冲突变量。本版本引入了交易并行冲突分析工具，写合约时无需手动指定交易冲突变量，分工明确，各司其职。这样，开发人员可以专注于合约的逻辑实现，而不必过多关注并行执行的细节和指定冲突变量，合约编译时工具自动生成交易冲突变量，相应的交易即可自动并行执行。

这项功能的引入旨在简化并加速开发过程，提高并行执行效率，并提供更方便的交易冲突管理方式，使开发人员能够更轻松地利用并行处理的优势，使得并行交易的实现更加简便和可靠，提升系统性能和吞吐量。

## 特性继承与升级
 v3.0稳定版也继承了 v2.0版本的诸多重要特性并进行升级，包括：

* PBFT共识算法：立即一致的共识算法，实现交易秒级确认
* Solidity：支持至0.8.11版本
* CRUD：采用表结构存储数据，本版本中封装了更易用的接口，对业务开发更友好
* AMOP：链上信使协议，借助区块链的P2P网络实现信息传输，实现接入区块链的应用间数据通信
* 落盘加密：区块链节点的私钥和数据加密存储于物理硬盘中，即使物理硬件丢失，被盗或被带出也无法解密。简单理解为：节点敏感数据只能在机构之间的内网获取。当节点被带出机构后不启动直接访问，无法通过层层解密，也就无法拿到敏感数据
* 密码算法：内置群环签名等密码算法，可支持各种安全多方计算场景
* 区块链监控：实现区块链状态的实时监控与数据上报

该继承与升级不仅提高了共识和信息传输速度，还对区块链的加密功能进行了完善，进一步提高了区块链系统的安全性和数据的保密性。

---
总结：FISCO BCOS V3.0,不仅从自身角度去提高了区块链系统的安全性和保密性、提升出块速度，还从用户使用角度拓展了不同部署的区块链形态、编写智能合约的语言、SDK基础库、接口封装等一系列基础支持，这次全面的系统的优化升级必将推动区块链技术和区块链行业进一步发展。