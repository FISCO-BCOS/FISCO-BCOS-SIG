# FISCO BCOS教程|Docker Host模式部署BCOS区块链

> 作者：liwh1227
>
> github：https://github.com/liwh1227

### 1. 背景

本文档针对[air版本BCOS链](https://fisco-bcos-doc.readthedocs.io/zh_CN/latest/docs/tutorial/air/build_chain.html)的docker部署过程进行实操和问题总结，主要是通过`build_chain`工具完成区块链网络的部署。

### 2. 搭建区块链

**备注：** 为了避免权限问题导致环境依赖安装失败，以下操作均在root用户权限下进行，若非root用户使用`su root`切换到root用户。

#### 2.1 安装依赖

1、查看系统版本，本教程的运行环境为centos系统

```shell
[root@VM-4-14-centos ~]# cat /proc/version
Linux version 3.10.0-1160.59.1.el7.x86_64 (mockbuild@kbuilder.bsys.centos.org) (gcc version 4.8.5 20150623 (Red Hat 4.8.5-44) (GCC) ) #1 SMP Wed Feb 23 16:47:03 UTC 2022
```

2、安装必备依赖或工具

docker:  请参看docker官方文档 https://docs.docker.com/engine/install/

安装curl、openssl、openssl-devel及wget，*备注：由于我测试的环境已经安装过上述工具，所以显示installed和Nothin to do*。

```shell
[root@VM-4-14-centos ~]# yum install -y curl openssl openssl-devel wget
Loaded plugins: fastestmirror, langpacks, product-id, search-disabled-repos, subscription-manager

This system is not registered with an entitlement server. You can use subscription-manager to register.

Bad id for repo: download.docker.com_linux_centos_docker-ce.xn--repo()-fw7ii8bx78cvkm, byte = ( 51
Repository epel is listed more than once in the configuration
Bad id for repo: mirrors.aliyun.com_docker-ce_linux_centos_docker-ce.xn--repo()-mu8ig98h4l0hokj, byte = ( 60
Loading mirror speeds from cached hostfile
 * centos-sclo-rh: mirrors.aliyun.com
 * centos-sclo-sclo: ftp.sjtu.edu.cn
Package curl-7.29.0-59.el7_9.1.x86_64 already installed and latest version
Package 1:openssl-1.0.2k-25.el7_9.x86_64 already installed and latest version
Package 1:openssl-devel-1.0.2k-25.el7_9.x86_64 already installed and latest version
Package wget-1.14-18.el7_6.1.x86_64 already installed and latest version
Nothing to do
```

#### 2.2 创建操作目录，下载build_chain.sh脚本

1、创建目录`fisco-task`：

```shell
[root@VM-4-14-centos ~]# cd /root && mkdir -p fisco-task && cd fisco-task
```

2、利用curl工具下载`build_chain.sh`工具：

```shell
[root@VM-4-14-centos fisco-task]# curl -#LO https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/FISCO-BCOS/releases/v3.0.0-rc1/build_chain.sh
######################################################################## 100.0%
```

3、查看下载的工具，若成功可以看到：`build_chian.sh`和`nodes`

```shell
[root@VM-4-14-centos fisco-task]# ls -al
total 48
drwxr-xr-x   3 root root  4096 May 30 22:28 .
dr-xr-x---. 38 root root  4096 Jun 18 10:20 ..
-rwxr-xr-x   1 root root 35620 Jun 18 10:25 build_chain.sh
drwxr-xr-x   4 root root  4096 May 30 22:28 nodes
```

#### 2.3 搭建4节点非国密联盟链

**备注：** 以下过程均在脚本所在的`fisco-task`目录下进行，搭建前对要部署链的节点进行规划，确保区块链节点所使用的端口没有被占用。本次创建的是

1、查看当前的工作目录

```shell
[root@VM-4-14-centos fisco-task]# pwd
/root/fisco-task
```

2、查看`build_chain.sh`脚本工具的主要功能：

```shell
[root@VM-4-14-centos fisco-task]# ./build_chain.sh --help
./build_chain.sh: illegal option -- -

Usage:
    -C <Command>                        [Optional] the command, support 'deploy' and 'expand' now, default is deploy
    -v <FISCO-BCOS binary version>      Default is the latest v3.0.0-rc1
    -l <IP list>                        [Required] "ip1:nodeNum1,ip2:nodeNum2" e.g:"192.168.0.1:2,192.168.0.2:3"
    -o <output dir>                     [Optional] output directory, default ./nodes
    -e <fisco-bcos exec>                [Required] fisco-bcos binary exec
    -p <Start Port>                     Default 30300,20200 means p2p_port start from 30300, rpc_port from 20200
    -s <SM model>                       [Optional] SM SSL connection or not, default is false
    -c <Config Path>                    [Required when expand node] Specify the path of the expanded node config.ini, config.genesis and p2p connection file nodes.json
    -d <CA cert path>                   [Required when expand node] When expanding the node, specify the path where the CA certificate and private key are located
    -D <docker mode>                    Default off. If set -d, build with docker
    -A <Auth mode>                      Default off. If set -A, build chain with auth, and generate admin account.
    -a <Auth account>                   [Optional when Auth mode] Specify the admin account address.
    -w <WASM mode>                      [Optional] Whether to use the wasm virtual machine engine, default is false
    -h Help

deploy nodes e.g
    bash ./build_chain.sh -p 30300,20200 -l 127.0.0.1:4 -o nodes -e ./fisco-bcos
    bash ./build_chain.sh -p 30300,20200 -l 127.0.0.1:4 -o nodes -e ./fisco-bcos -s
expand node e.g
    bash ./build_chain.sh -C expand -c config -d config/ca -o nodes/127.0.0.1/node5 -e ./fisco-bcos
    bash ./build_chain.sh -C expand -c config -d config/ca -o nodes/127.0.0.1/node5 -e ./fisco-bcos -s
```

我们重点关注：

- `-D`  使用docker的方式启动节点
- `-o` 脚本运行后，产生内容的输出目录
- `-l` 节点的ip列表，如`127.0.0.1:4`就是在本机启动4个节点

3、执行命令，生成必要配置和证书文件：

```shell
[root@VM-4-14-centos fisco-task]# ./build_chain.sh -D -l 127.0.0.1:4 -o nodes
[INFO] Generate ca cert successfully!
Processing IP:127.0.0.1 Total:4
[INFO] Generate nodes/127.0.0.1/sdk cert successful!
[INFO] Generate nodes/127.0.0.1/node0/conf cert successful!
[INFO] Generate nodes/127.0.0.1/node1/conf cert successful!
[INFO] Generate nodes/127.0.0.1/node2/conf cert successful!
[INFO] Generate nodes/127.0.0.1/node3/conf cert successful!
==============================================================
[INFO] docker mode     : true
[INFO] docker tag      : v3.0.0-rc1
[INFO] Auth Mode           : false
[INFO] Start Port          : 30300 20200
[INFO] Server IP           : 127.0.0.1:4
[INFO] SM Model            : false
[INFO] output dir          : nodes
[INFO] All completed. Files in nodes
```

4、启动节点

**备注：** 此过程可能会因为镜像拉取过慢等待较长时间，若出现拉取过慢的问题，可配置如下内容到`/etc/docker/daemon.json`配置文件。

```shell
{
   "registry-mirrors": ["https://registry.docker-cn.com"]
}
```

运行start_all脚本：

```shell
[root@VM-4-14-centos fisco-task]# bash nodes/127.0.0.1/start_all.sh 
try to start node0
28ecb94b5b647afd87c157c11dee59d529775096ee1a337e05967db3a2dfab7b
 node0 start successfully
try to start node1
185bbb026462ecfa429cccfa9db8276674c88fccf0e1122f4b3a28e0950443fb
 node1 start successfully
try to start node2
8007817607d1b36dad444bc045070b01225a1ab3ab5cd68ccf62199a7d727c0c
 node2 start successfully
try to start node3
c5f3c3fa937f94282d85c464cd7014ac05ce635c90c41e938ec1095b9998e062
 node3 start successfully
```

5、查看网络前节点运行状态，`status`都是up状态说明运行成功。

```shell
[root@VM-4-14-centos fisco-task]# docker ps
CONTAINER ID   IMAGE                           COMMAND                  CREATED              STATUS              PORTS     NAMES
c5f3c3fa937f   fiscoorg/fiscobcos:v3.0.0-rc1   "/usr/local/bin/fisc…"   55 seconds ago       Up 54 seconds                 rootfisco-tasknodes127.0.0.1node3
8007817607d1   fiscoorg/fiscobcos:v3.0.0-rc1   "/usr/local/bin/fisc…"   57 seconds ago       Up 56 seconds                 rootfisco-tasknodes127.0.0.1node2
185bbb026462   fiscoorg/fiscobcos:v3.0.0-rc1   "/usr/local/bin/fisc…"   59 seconds ago       Up 58 seconds                 rootfisco-tasknodes127.0.0.1node1
28ecb94b5b64   fiscoorg/fiscobcos:v3.0.0-rc1   "/usr/local/bin/fisc…"   About a minute ago   Up About a minute             rootfisco-tasknodes127.0.0.1node0
```

6、查看网络模式，`dockert inspect`后的参数是节点容器的名称

```shell
[root@VM-4-14-centos fisco-task]# docker inspect rootfisco-tasknodes127.0.0.1node0 | grep -i "network"
            "NetworkMode": "host",
        "NetworkSettings": {
            "Networks": {
                    "NetworkID": "307d15ab46e48a30c65e2e57826bb1c54b34b259b24ad90304ceadee3b718046",
```

### 3. 参考

1. build_chain.sh工具说明： https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/build_chain.html
2. 网络搭建基础要求：https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/blockchain_dev/env.html
3. 使用docker容器部署区块链：https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/tutorial/docker.html