# FISCO BCOS V3.0 Air建链体验——对比V2.9建链差别
作者：重庆电子工程职业学院 | 杜小敏 向键雄

## 前提

 好久不见，最近因为毕业的手续等问题，一直都没有更新，FISCO BCOS第二季task挑战赛如期展开啦，因为毕业的问题，也是非常遗憾的错过了上一期的task挑战赛，这一期一定双倍挑战，hhhhhh

Air版本FISCO BCOS采用all-in-one的封装模式，将所有模块编译成一个二进制（进程），一个进程即为一个区块链节点，包括网络、共识、接入等所有功能模块，适用于初学者入门、功能验证、POC产品。

所以我们这篇文章会更加注重的去讲3.0Air和2.0的区别，以便于各位更加轻松的向3.0进行转型

环境：Ubuntu20

     FISCO BCOS V3.0 rc1

## 正文
首先咱们先看第一个区别，第一个区别最大的地方还是在体验上，起链的时候虽然一如既往的使用了build_chain，但是增加了许多新的选项参数，比如

### build_chain 2.X 版本的help

|参数  |功能|
|----|---|
|-l<IP list>[必需]	|“ip1:nodeNum1，ip2:nodeNum2”例如：“192.168.0.1:2192.168.0.2:3”
|-f<IP list file>|[可选]	按行拆分，每行应为“IP:nodeNum agencyName groupList p2p_port，channel_port，
|-v	|<FISCO-BCOS二进制版本>默认为最新的v$｛Default_version｝t，
|-e	|＜FISCO-BCOS二进制路径＞默认从GitHub下载FISCO-BCOS。如果设置-e，则在指定位置使用二进制
|-o	|＜输出方向＞默认值/节点/
|-p	|＜开始端口＞默认30300202008545表示p2p_Port从30300开始，channel_Port从20200开始，jsonrpc_Port从8545开始
|-q	|＜列出FISCO-BCOS发布＞列出FISCO-BCOS发布版本
|-i	|＜主机ip＞默认127.0.0.1。如果设置-i，则侦听0.0.0.0
|-s	|＜DB类型＞默认RocksDB。选项可以是RocksDB/MySQL/Scalible，建议使用RocksDB
|-d	|＜docker mode＞默认关闭。如果设置为-d，则使用docker构建
|-c	|＜共识算法＞默认PBFT。选项可以是pbft/rraft/rpbft，建议使用pbft
|-C	|＜Chain id＞默认值1。可以设置uint。
|-g	|<生成国密节点>默认否
|-z	|＜生成tar数据包＞默认否
|-t	|＜证书配置文件＞默认自动生成
|-6	|＜使用ipv6＞默认编号。如果设置为-6，则将IP视为ipv6
|-k	|＜ca根的路径＞默认自动生成，ca.crt和ca.key必须在路径中，如果使用中间值，则root.crt必须在路径
|-K	|＜sm crypto ca root的路径＞默认自动生成，gmca.crt和gmca.key必须在路径中，如果使用中间路径，则gmroot.crt必须在路径
|-D	|<使用部署模式>默认值为false，如果设置为-D，则使用部署模式目录结构并使tar
|-G	|<channel use sm crypto ssl>默认为false，仅适用于国密模式
|-X	|＜证书到期时间＞默认36500天
|-T	|＜启用调试日志＞默认关闭。如果设置为-T，则启用调试日志
|-R	|<Channel use ecdsa crypto ssl>默认值为false。如果设置了-R，请为通道ssl使用ecdsa证书，否则将使用rsa证书
|-S	|＜启用统计信息＞默认关闭。如果设置为-S，则启用统计信息
-F	|＜禁用日志自动刷新＞默认打开。如果设置为-F，则禁用日志自动清除
-E	|＜Enable free_storage_evm＞默认关闭。如果设置为-E，则启用free_storage _evm
-h	|帮助
 

 

### build_chain 3.X 版本的help



|参数  |功能|
|-|-|
|-C<Command>[可选]|命令	现在支持“deploy”和“expand”，默认为deploy|
|-g<group id>[可选]	|设置组id，默认值：group0|
|-I<chain id>[可选]	|设置chain id，默认值：chain0|
|-v<FISCO-BCOS二进制版本>[可选]	|默认为最新的v3.4.0|
|-l<IP list>[必需]	|“ip1:nodeNum1，ip2:nodeNum2”例如：“192.168.0.1:2192.168.0.2:3”|
|-L<fisco-bcos lightnode exec>[可选]	|fisco-bdos 轻节点的好可执行文件，输入“download_binary”下载lightnode二进制文件或指定正确的lightnode二元路径|
|-e＜fisco-bcos exec＞〔可选〕	|fisco-bcosbinary exec|
|-t<mtail exec>[可选]  | mtail二进制exec|
|-o＜output dir＞〔可选〕	|输出目录，默认值/节点|
|-p＜启动端口＞〔可选〕	|默认3030020200表示p2p_port从30300开始，rpc_port从20200开始|
|-s<SM型号>[可选]	|SM SSL连接与否，默认为false|
|-H<HSM型号>[可选]	|是否使用HSM（硬件安全模块），默认为false|
|-c＜Config Path＞〔展开节点时必需〕|指定展开节点的路径Config.ini、Config.generion和p2p连接文件nodes.json|
|-d＜CA cert path＞[展开节点时必需]	|展开节点时，指定CA证书和私钥所在的路径|
|-D<docker模式>默认关闭	|如果设置为-D，则使用docker构建，如果设置为-D，则使用docker构建	
|-a <Auth-account> |当Auth模式时指定管理员帐户地址。
|-w <WASM mode>  | 是否使用WASM虚拟机引擎，默认为false|
|-R<Serial_mode>[可选]	|是否使用串行执行，默认为true|
|-k<关键页大小>[可选]	|关键页大小，默认为10240|
|-m<fisco-bcos monitor>[可选] |节点监视器与否，默认值为false|	
|-i<fisco-bcos monitor ip/port>[可选]|展开节点时，应指定ip和端口|	
|-M<fisco-bcos monitor>[可选]|展开节点时，指定prometheus所在的路径|
|-z＜生成tar包＞〔可选〕	|将数据打包到链上生成tar包|
|-n<node-key path>[可选]	|设置要加载nodeid的节点密钥文件的路径|
|-N＜node path＞[可选]	|-N＜node path＞[可选]|
|-u<multi-ca-path>[可选]	|-u<multi-ca-path>[可选]|
|-h	|帮助|


我们可以通过脚本的新增参数发现，多乐许多不一样的地方，比如轻节点的概念，亦或者是-d的功能改成--D了，都有不同的变化，那么我们本篇就以启动一个FISCO BCOS V3.0 Air来试一下轻量级的变化

## 起链对比
### V3.0 起链
```
bash ../tools/build_chain.sh -p 30300,20200 -l 127.0.0.1:4 -o nodes
```


### V2.0起链
```
bash ../tools/build_chain.sh -l 127.0.0.1:4 -p 30300,20200,8545 -o nodes
```


### 返回消息对比
与2.0相比起链少了一个指定的8545的端口，默认的起链命令倒是没什么变化

 相较于2.0来说多了很多返回的内容，例如：
``` 
Processing IP:127.0.0.1 Total:4
writing RSA key
[INFO] Generate nodes/127.0.0.1/sdk cert successful!
writing RSA key
[INFO] Generate nodes/127.0.0.1/node0/conf cert successful!
writing RSA key
[INFO] Generate nodes/127.0.0.1/node1/conf cert successful!
writing RSA key
[INFO] Generate nodes/127.0.0.1/node2/conf cert successful!
writing RSA key
[INFO] Generate nodes/127.0.0.1/node3/conf cert successful!
[INFO] Downloading get_account.sh from https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/FISCO-BCOS/tools/get_account.sh...

```
这里的返回指的是，创建了RSA的秘钥，RSA秘钥指的是非对称加密算法的秘钥，也就是我们所说的公私钥

 
```
[INFO] Admin account: 0x984a17c383a35dcc7da48924ca83c078ae384790
[INFO] Generate uuid success: 0578afd8-df1e-49ae-bf93-f01105e33500
[INFO] Generate uuid success: 8505abdf-5c60-45cf-b8d8-94ae641e1e5f
[INFO] Generate uuid success: dae56b62-0584-486c-821a-b5499dd3d2b9
[INFO] Generate uuid success: fd0d62e6-15a6-4422-bb3b-c9ade14a283e
```

 这里的返回是UUID，UUID是我们分布式系统中常见的唯一性表示，就和物理mac地址一样 

```
[INFO] GroupID              : group0
[INFO] ChainID              : chain0
[INFO] fisco-bcos path      : bin/fisco-bcos
[INFO] Auth mode            : false
[INFO] Start port           : 30300 20200
[INFO] Server IP            : 127.0.0.1:4
[INFO] SM model             : false
[INFO] enable HSM           : false
[INFO] Output dir           : nodes
[INFO] All completed. Files in nodes
```

这里就是一些基本情况的阐述

文件目录对比
最终生成的目录对比，我们发现3.0多了许多证书类，监控类的文件，这意味着我们的3.0更加安全，完善

```
V3.0

nodes/
├── monitor
│   ├── grafana # grafana配置文件
│   ├── prometheus # prometheus配置文件
│   ├── start_monitor.sh # 启动脚本，用于开启监控
│   ├── stop_monitor.sh # 停止脚本，用于停止监控
│   ├── compose.yaml # docker-compose配置文件
├── 127.0.0.1
│   ├── fisco-bcos # 二进制程序
│   ├── mtail # 二进制程序
│   ├── node0 # 节点0文件夹
│   │   ├── mtail # mtail配置文件夹
│   │   │   ├── start_mtail_monitor.sh  # 启动脚本，用于启动该节点mtail程序
│   │   │   ├── stop_mtail_monitor.sh   # 停止脚本，用于停止该节点mtail程序
│   │   │   ├── node.mtail # mtail配置文件
│   │   ├── conf # 配置文件夹
│   │   │   ├── ca.crt # 链根证书
│   │   │   ├── cert.cnf
│   │   │   ├── ssl.crt # ssl证书
│   │   │   ├── ssl.key # ssl连接证书私钥
│   │   │   ├── node.pem # 节点签名私钥文件
│   │   │   ├── node.nodeid # 节点id，公钥的16进制表示
│   │   ├── config.ini # 节点主配置文件，配置监听IP、端口、证书、日志等
│   │   ├── config.genesis # 创世配置文件，共识算法类型、共识超时时间和交易gas限制等
│   │   ├── nodes.json # 节点json信息，展示节点的ip和端口，示例：{"nodes": [127.0.0.1:30300]}
│   │   ├── start.sh # 启动脚本，用于启动节点
│   │   └── stop.sh # 停止脚本，用于停止节点
│   ├── node1 # 节点1文件夹
│   │.....
│   ├── node2 # 节点2文件夹
│   │.....
│   ├── node3 # 节点3文件夹
│   │.....
│   ├── sdk # SDK证书
│   │   ├── ca.crt # SSL连接根证书
│   │   ├── cert.cnf # 证书配置
│   │   ├── sdk.crt # SDK根证书
│   │   ├── sdk.key # SDK证书私钥
│   ├── start_all.sh # 启动脚本，用于启动所有节点
│   ├── stop_all.sh # 停止脚本，用于停止所有节点

```

```

V2.0

nodes/
├── 127.0.0.1
│   ├── download_bin.sh
│   ├── download_console.sh
│   ├── fisco-bcos
│   ├── node0
│   │   ├── conf
│   │   │   ├── ca.crt
│   │   │   ├── group.1.genesis
│   │   │   ├── group.1.ini
│   │   │   ├── node.crt
│   │   │   ├── node.key
│   │   │   └── node.nodeid
│   │   ├── config.ini
│   │   ├── scripts
│   │   │   ├── load_new_groups.sh
│   │   │   ├── monitor.sh
│   │   │   ├── reload_sdk_allowlist.sh
│   │   │   └── reload_whitelist.sh
│   │   ├── start.sh
│   │   └── stop.sh
│   ├── node1
│   │   ├── conf
│   │   │   ├── ca.crt
│   │   │   ├── group.1.genesis
│   │   │   ├── group.1.ini
│   │   │   ├── node.crt
│   │   │   ├── node.key
│   │   │   └── node.nodeid
│   │   ├── config.ini
│   │   ├── scripts
│   │   │   ├── load_new_groups.sh
│   │   │   ├── monitor.sh
│   │   │   ├── reload_sdk_allowlist.sh
│   │   │   └── reload_whitelist.sh
│   │   ├── start.sh
│   │   └── stop.sh
│   ├── node2
│   │   ├── conf
│   │   │   ├── ca.crt
│   │   │   ├── group.1.genesis
│   │   │   ├── group.1.ini
│   │   │   ├── node.crt
│   │   │   ├── node.key
│   │   │   └── node.nodeid
│   │   ├── config.ini
│   │   ├── scripts
│   │   │   ├── load_new_groups.sh
│   │   │   ├── monitor.sh
│   │   │   ├── reload_sdk_allowlist.sh
│   │   │   └── reload_whitelist.sh
│   │   ├── start.sh
│   │   └── stop.sh
│   ├── node3
│   │   ├── conf
│   │   │   ├── ca.crt
│   │   │   ├── group.1.genesis
│   │   │   ├── group.1.ini
│   │   │   ├── node.crt
│   │   │   ├── node.key
│   │   │   └── node.nodeid
│   │   ├── config.ini
│   │   ├── scripts
│   │   │   ├── load_new_groups.sh
│   │   │   ├── monitor.sh
│   │   │   ├── reload_sdk_allowlist.sh
│   │   │   └── reload_whitelist.sh
│   │   ├── start.sh
│   │   └── stop.sh
│   ├── sdk
│   │   ├── ca.crt
│   │   ├── sdk.crt
│   │   ├── sdk.key
│   │   └── sdk.publickey
│   ├── start_all.sh
│   └── stop_all.sh
├── cert
│   ├── agency
│   │   ├── agency.crt
│   │   ├── agency.key
│   │   ├── agency.srl
│   │   ├── ca.crt
│   │   └── cert.cnf
│   ├── ca.crt
│   ├── ca.key
│   ├── ca.srl
│   └── cert.cnf
└── cert.cnf

```


### 起链返回内容
这里更新了启动进程不仅仅是返回启动成功，并且将进程号也一并返回了

```

V3.0

try to start node0
try to start node1
try to start node2
try to start node3
 node3 start successfully pid=5947
 node2 start successfully pid=5957
 node1 start successfully pid=5962
 node0 start successfully pid=5951

```

```

V2.0

try to start node0
try to start node1
try to start node2
try to start node3
 node2 start successfully
 node1 start successfully
 node0 start successfully
 node3 start successfully

```

### 扩容节点对比

```

V3.0

# 创建扩容配置存放目录
$ mkdir config

# 拷贝根证书、根证书私钥
$ cp -r nodes/ca config

# 从被扩容节点node0拷贝节点配置文件config.ini，创世块配置文件config.genesis以及节点连接配置文件nodes.json
$ cp nodes/127.0.0.1/node0/config.ini config/
$ cp nodes/127.0.0.1/node0/config.genesis config/
$ cp nodes/127.0.0.1/node0/nodes.json config/nodes.json.tmp

# 设置新节点P2P和RPC监听端口
# macOS系统（设置P2P监听端口为30304，RPC监听端口为20204）
$ sed -i .bkp 's/listen_port=30300/listen_port=30304/g' config/config.ini
$ sed -i .bkp 's/listen_port=20200/listen_port=20204/g' config/config.ini
# linux系统（设置P2P监听端口为30304，RPC监听端口为20204）
$ sed -i 's/listen_port=30300/listen_port=30304/g' config/config.ini
$ sed -i 's/listen_port=20200/listen_port=20204/g' config/config.ini

# 将新节点连接加入到nodes.json
$ sed -e 's/"nodes":\[/"nodes":\["127.0.0.1:30304",/' config/nodes.json.tmp > config/nodes.json
# 确认新节点连接信息: 127.0.0.1:30304加入成功
$ cat config/nodes.json
{"nodes":["127.0.0.1:30304","127.0.0.1:30300","127.0.0.1:30301","127.0.0.1:30302","127.0.0.1:30303"]}


扩容节点
# 调用build_chain.sh扩容节点，新节点扩容到nodes/127.0.0.1/node4目录
# -c: 指定扩容配置config.ini, config.genesis和nodes.json路径
# -d: 指定CA证书和私钥的路径
# -o: 指定扩容节点配置所在目录
bash ../tools/build_chain.sh -C expand -c config -d config/ca -o nodes/127.0.0.1/node4


cat ~fisco/nodes/127.0.0.1/node4/conf/node.nodeid

cp -rf nodes/127.0.0.1/sdk/* console/conf/

#这里可以根据自己的实际情况来改，我就不改了直接使用示例文件
cp -rf console/conf/config-example.toml console/conf/config.toml

addObserver

addSealer

```



```

V2.0

curl -#LO https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/master-2.0/tools/gen_node_cert.sh

# -c指定机构证书及私钥所在路径
# -o输出到指定文件夹，其中node4/conf中会存在机构agency新签发的证书和私钥
# 成功会输出 All completed 提示
bash gen_node_cert.sh -c ../cert/agency -o node4

cp node0/config.ini node0/start.sh node0/stop.sh node4/

#修改node4/config.ini。对于[rpc]模块，修改channel_listen_port=20204和jsonrpc_listen_port=8549；对于[p2p]模块，修改listen_port=30304并在node.中增加自身节点信息；

cp node0/conf/group.1.genesis node0/conf/group.1.ini node4/conf/

bash node4/start.sh

cat node4/conf/node.nodeid

getObserverList

addSealer 

```

这里我们可以发现，v3.0的扩容步骤明显多于且复杂于v2.0，所以我们这里可以，仔细的分析一下里面为什么要这么做

V3.0对于证书的要求明显是更加的严格了，引入了2.0所没有的证书文件，以及加密方式，但是对于轻量级的版本来说又不如pro或者max那版复杂，剩下的我们在下一篇中来分享搭建pro以及max版本所体会到的感受

# ​感谢浏览