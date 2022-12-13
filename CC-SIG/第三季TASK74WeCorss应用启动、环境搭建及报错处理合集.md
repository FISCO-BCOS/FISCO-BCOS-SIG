​# WeCross应用启动、环境搭建及报错处理合集
作者 重庆电子工程职业学院 向键雄
## 前言

今天在阅读了官方文档之后，发现WeCross是区块链必然的产物，一条链永远不可能形成一条生态，那么一条链不够两条链呢？不够？那就再来一条直到你够为止。话不多说直接开始正文

## 环境

对配置的要求还算可以，不是很高大家避一下雷，不要因为配置的问题而搭建不出来

|配置	|最低配置	|推荐配置|
|-------|----------|--------|
|CPU	|1.5GHz	|2.4GHz|
|内存	|4GB	|8GB|
|核心	|4核	|8核|
|带宽	|2Mb	|10Mb|
Ubuntu 16.04及以上
CentOS 7.2及以上
macOS 10.14及以上
Gradle 5.0及以上
MySQL 5.6及以上
Docker 17.06.2-ce 及以上
openssl, curl, expect

### 环境配置

检查docker版本

```

docker -v

```

#### 其次是MySQL链接在这里，检查MySQL版本

在这里经常遇到一个报错遇到ERROR 1045 (28000): Access denied for user ‘fisco‘@‘localhost‘ (using password: NO)[解决方法](https://blog.csdn.net/qq_57309855/article/details/127602061?spm=1001.2014.3001.5501)
```

mysql -v

使用命令行下载MySQL 
sudo apt install mysql-server
 
 输入命令查看是否下载成功

mysql --version

检查进程是否启动

systemctl status mysql.service

如果没有启动就让他启动      

sudo systemctl start mysql


```

#### 检查java版本没有的话就需要下载

```

java -version

```

#### 下载java 

```

sudo apt install -y default-jdk

```

#### 检查gradle版本

```

gradle -v

```



### gradle

```


// 这里创建一个属于gradle的安装目录
 
sudo mkdir /opt/gradle
 
// 使用wget下载
 
wget https://services.gradle.org/distributions/gradle-6.3-bin.zip
 
// 解压到目录中
 
sudo unzip -d /opt/gradle gradle-6.3-bin.zip
 
// 查看是否解压成功
 
cd /opt/gradle/
 
ls
 
// 配置环境变量需要下载vim否则就用gedit
 
vim ~/.bashrc
 
// 添加如下内容之后，:wq保存退出
 
export GRADLE_HOME=/opt/gradle/gradle-6.3
export PATH=$GRADLE_HOME/bin:$PATH
 
// 刷新使配置的文件生效
 
source ~/.bashrc
 
// 查看安装后的版本
 
gradle -v


```

还需要下载依赖

```

sudo apt-get install -y openssl curl expect tree fontconfig

```

### 卸载旧版本

旧版本的 Docker 称为 docker 或者 docker-engine，使用以下命令卸载旧版本：

```

sudo apt-get remove docker docker-engine docker.io

```

### 使用 APT 安装

```

由于 apt 源使用 HTTPS 以确保软件下载过程中不被篡改。因此，我们首先需要添加使用 HTTPS 传输的软件包以及 CA 证书。

sudo apt-get update

sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

```

鉴于国内网络问题，强烈建议使用国内源，官方源请在注释中查看。


```

为了确认所下载软件包的合法性，需要添加软件源的 GPG 密钥。

curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

官方源

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

然后，我们需要向 sources.list 中添加 Docker 软件源

 echo \
  "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

官方源

 $ echo \
 "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

```

以上命令会添加稳定版本的 Docker APT 镜像源，如果需要测试版本的 Docker 请将 stable 改为 test。

### 安装 Docker

```

更新 apt 软件包缓存，并安装 docker-ce：

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

```

### 使用脚本自动安装

```

在测试或开发环境中 Docker 官方为了简化安装流程，提供了一套便捷的安装脚本，Ubuntu 系统上可以使用这套脚本安装，另外可以通过 --mirror 选项使用国内源进行安装：

若你想安装测试版的 Docker, 请从 test.docker.com 获取脚本

  curl -fsSL test.docker.com -o get-docker.sh
 sudo sh get-docker.sh --mirror Aliyun

执行这个命令后，脚本就会自动的将一切准备工作做好，并且把 Docker 的稳定(stable)版本安装在系统中。

启动 Docker
 sudo systemctl enable docker
 sudo systemctl start docker

```

### 建立 docker 用户组

```

默认情况下，docker 命令会使用 Unix socket 与 Docker 引擎通讯。而只有 root 用户和 docker 组的用户才可以访问 Docker 引擎的 Unix socket。出于安全考虑，一般 Linux 系统上不会直接使用 root 用户。因此，更好地做法是将需要使用 docker 的用户加入 docker 用户组。

建立 docker 组：
sudo groupadd docker

将当前用户加入 docker 组：
 sudo usermod -aG docker $USER

退出当前终端并重新登录，进行如下测试。

测试 Docker 是否安装正确
 sudo docker run --rm hello-world

```


## 正文


先把关键词介绍搬过来

### 关键词
- 跨链路由（WeCross Router）
  - 与链对接，对链上的资源进行抽象
  - 向外暴露统一的接口
  - 将调用请求路由至对应的区块链
- 账户服务（WeCross Account Manager）
  - 管理跨链账户
  - Router连接所属机构的Account Manager
  - 用户在Router上登录，以跨链账户的身份发交易
- 控制台（WeCross Console）
  - 命令行式的交互
  - 查询跨链信息，发送调用请求，操作跨链事务
- 网页管理平台
  - 可视化操作界面
  - 查询跨链信息，发送调用请求，操作跨链事务
- 跨链 SDK（WeCross Java SDK）
  - WeCross开发工具包，供开发者调用WeCross
  - 集成于各种跨链APP中，提供统一的调用接口
  - 与跨链路由建立连接，调用跨链路由
- 跨链资源（Resource）
  - 各种区块链上内容的抽象
  - 包括：合约、资产、信道、数据表
- 跨链适配器（Stub）
  - 跨链路由中对接入的区块链的抽象
  - 跨链路由通过配置Stub与相应的区块链对接
  - FISCO BCOS需配置FISCO BCOS Stub、Fabric需配置Fabric Stub
- IPath（Interchain Path）
  - 跨链资源的唯一标识
  - 跨链路由根据IPath将请求路由至相应区块链上
  - 在代码和文档中将IPath简称为path
- 跨链分区
  - 多条链通过跨链路由相连，形成跨链分区
  - 跨链分区有唯一标识，即IPath中的第一项（payment.stub3.resource-d的payment）
### 多链部署布局
​​​​
![](https://img-blog.csdnimg.cn/4bf4a4609f334baa85ad414a14847ed0.png)

(图片与素材来源于官方)
​​​​​​​​​​​​

### 解读一下这个图

```

首先我们会以一个用户的身份去调用跨链应用——>之后我们使用跨链路由来进行联系——>他们统一运行在一个stub中这个叫跨链适配器大概作用就和EVM一样——>多链之间使用统一暴露的结构进行联系

其次就是我对这个应用的理解，他就像我们的手机一样打电话给对方就是p2p，然后使用手机卡进行身份判定，在之后有手机装手机卡，启动使手机拥有打电话功能与身份

```
## 实操以及报错

### WeCorss快速搭建

#### 下载demo

```

cd ~
# 下载WeCross demo合集，生成wecross-demo目录，目录下包含各种类型的demo
bash <(curl -sL https://github.com/WeBankBlockchain/WeCross/releases/download/resources/download_demo.sh)

# 若出现长时间下载Demo包失败，请尝试以下命令重新下载：
bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_demo.sh)
这里我们就玩一个比较简单的多群组，因为只需要FISCO-BCOS环境就够了

```

大家想玩的话，就先去了解一下fabric我把我写的相关文章都放这里了

[fabric关键概念](https://blog.csdn.net/qq_57309855/article/details/126445990?spm=1001.2014.3001.5501)

[fabric搭建区块链](https://blog.csdn.net/qq_57309855/article/details/126450292?spm=1001.2014.3001.5501)

后面我会做一些新的和fabric相关的东西发出来现在我们就玩一个简单一点的FISCO BCOS吧

需要有一个区块链网络单机四节点[区块链网络教程](https://blog.csdn.net/qq_57309855/article/details/126180787?spm=1001.2014.3001.5501)

```

cd ~/wecross-demo

#清理旧demo环境
bash clear.sh

# 运行部署脚本，输入数据库账号密码，第一次运行需耗时10-30分钟左右
bash build_cross_groups.sh # 若出错，可用 bash clear.sh 清理后重试。bash build.sh -h 可查看更多用法
 这里是ip配置，配置过后就会安装文件

```

![](https://img-blog.csdnimg.cn/cf5f2a45bb07401399172a2a0a8c2066.png)
 
![](https://img-blog.csdnimg.cn/46f218ddc4604d05a6cc7bf391a2dd1f.png)

## WeCorss手动搭建组件部署

可以基于已有（或新部署）的区块链环境，搭建一个与Demo相似的跨链网络。

![](https://img-blog.csdnimg.cn/5d06de92e7544673be7e07acf920c11a.png)


操作步骤分为以下4项：

- 基础组件部署：部署WeCross基础组件，包括跨链路由、账户服务、控制台、网页管理台
- 区块链接入与账户配置：将区块链接入WeCross跨链网络，并配置跨链账户
- 资源部署与操作：基于搭建好的WeCross环境部署和操作跨链资源
- 区块头验证配置：配置区块头验证参数，完善跨链交易的验证逻辑

```

若已搭建WeCross Demo，请先关闭所有服务

创建手动组网的操作目录

mkdir -p ~/wecross-networks && cd ~/wecross-networks

```

### 基础组件部署



- 跨链路由（router）：与区块链节点对接，并彼此互连，形成跨链分区，负责跨链请求的转发
- 账户服务（account manager）：为跨链系统提供账户管理
- 跨链控制台（console）：查询和发送交易的操作终端


### 下载WeCross

下载WeCross，用WeCross中的工具生成跨链路由，并启动跨链路由。

WeCross中包含了生成跨链路由的工具，执行以下命令进行下载（提供三种下载方式，可根据网络环境选择合适的方式进行下载），程序下载至~/wecross-networks/WeCross/中。

```

bash <(curl -sL https://github.com/WeBankBlockchain/WeCross/releases/download/resources/download_wecross.sh)

# 若出现长时间下载WeCross包失败，请尝试以下命令重新下载：
bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_wecross.sh)
部署跨链路由
构建两个跨链路由。首先创建一个ipfile配置文件，将需要构建的两个跨链路由信息（ip:rpc_port:p2p_port）按行分隔，保存到文件中。

注：请确保机器的8250，8251, 25500，25501端口没有被占用。

```

```

cd ~/wecross-networks
vim ipfile

# 在文件中键入以下内容
127.0.0.1:8250:25500
127.0.0.1:8251:25501
生成好ipfile文件后，使用脚本build_wecross.sh生成两个跨链路由。

# -f 表示以文件为输入
bash ./WeCross/build_wecross.sh -n payment -o routers-payment -f ipfile
成功之后反馈的信息 



-n 指定跨链分区标识符(zone id)，跨链分区通过zone id进行区分，可以理解为业务名称。
-o 指定输出的目录，并在该目录下生成一个跨链路由。
-f 指定需要生成的WeCross跨链路由的列表，包括ip地址，rpc端口，p2p端口，生成后的router已完成互联配置。

```

![](https://img-blog.csdnimg.cn/fe7342271e164520ac88c87a9e8ab6ed.png)

部署账户服务

执行过程中需输入相应数据库的配置。

```

cd ~/wecross-networks
bash <(curl -sL https://github.com/WeBankBlockchain/WeCross/releases/download/resources/download_account_manager.sh)

# 若出现长时间下载WeCross-Account-Manager包失败，请尝试以下命令重新下载：
bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_account_manager.sh)
拷贝证书
cd ~/wecross-networks/WeCross-Account-Manager/
cp ~/wecross-networks/routers-payment/cert/sdk/* conf/
生成私钥
bash create_rsa_keypair.sh -d conf/
配置
cp conf/application-sample.toml conf/application.toml
vim conf/application.toml

```

需配置内容包括：


```


admin：配置admin账户，此处可默认，router中的admin账户需与此处对应，用于登录账户服务

db：配置自己的数据库账号密码

[service]
    address = '0.0.0.0'
    port = 8340
    sslKey = 'classpath:ssl.key'
    sslCert = 'classpath:ssl.crt'
    caCert = 'classpath:ca.crt'
    sslOn = true

[admin] 
    # admin账户配置，第一次启动时写入db，之后作为启动校验字段
    name = 'org1-admin' # admin账户名
    password = '123456' # 密码

[auth]
    # for issuring token
    name = 'org1'
    expires = 18000 # 5 h
    noActiveExpires = 600 # 10 min

[db]
    # for connect database
    url = 'jdbc:mysql://localhost:3306/wecross_account_manager'
    username = 'root' # 配置数据库账户
    password = '123456' # 配置数据库密码，不支接受空密码
[ext]
    # for image auth code, allow image auth token empty
    allowImageAuthCodeEmpty = true

```

### 启动

```

bash start.sh

cd ~/wecross-networks/routers-payment/127.0.0.1-8250-25500/
bash start.sh

cd ~/wecross-networks/routers-payment/127.0.0.1-8251-25501/
bash start.sh

如果启动失败，检查8250, 25500端口是否被占用。

```

### 部署控制台

WeCross提供了控制台，方便用户进行跨链开发和调试。可通过脚本build_console.sh搭建控制台。

```

执行如下命令进行下载（提供三种下载方式，可根据网络环境选择合适的方式进行下载），下载完成后在当前目录下生成WeCross-Console目录。

cd ~/wecross-networks
bash <(curl -sL https://github.com/WeBankBlockchain/WeCross/releases/download/resources/download_console.sh)

# 若出现长时间下载WeCross-Console包失败，请尝试以下命令重新下载：
bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_console.sh)
配置
cd ~/wecross-networks/WeCross-Console

# 拷贝连接router所需的TLS证书，从生成的routers-payment/cert/sdk目录下拷贝
cp ~/wecross-networks/routers-payment/cert/sdk/* conf/ 

# 拷贝配置文件，并配置跨链路由RPC服务地址以及端口。此处采用默认配置，默认连接至本地8250端口。
cp conf/application-sample.toml conf/application.toml

```

## 到这里整个的文章就结束了，报错也大都是环境搭建时的报错，在搭建时也需要各位走一步一个快照，否则嘻嘻嘻嘻，就要从头再来了，好了，有问题大家可以在社区群内提出来，欢迎大家在社区里提问哦




​

​
