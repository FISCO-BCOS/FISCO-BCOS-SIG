@[toc](部署多机多群FISCO BCOS链 | FISCO BCOS开发系列（一）)


实验环境

两台centos7机器（虚拟机和远程服务器都可以）

- 192.168.80.144

- 192.168.80.145

java1.8



# 搭建多机多群FISCO BCOS链
为了检测实验，我们计划搭建两个机构A和B，一个机构4节点一个机构5节点，一共两个群组，A和B均在这两个群组里，呈现一种嵌套的结构。

我将192.168.80.144 作为机构A， 192.168.80.145机器作为机构B

如图，这是我们的拓扑图（嵌套结构）


![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105124058664.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)



给两台机构机器安装依赖

```bash
sudo yum install -y openssl openssl-devel
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130107706.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)


**创建实验文件夹**

首先，我们给两台机器都创建一个操作目录 （比如：~/fisco）


**创建操作目录**
```bash
cd ~ && mkdir -p fisco &&cd fisco
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130035943.png)
**选择一台机器下载安装脚本（build_chain.sh）**

这里我们选择机构A（192.168.80.144）下载我们的安装脚本
```bash
## 下载脚本
curl -#LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.7.1/build_chain.sh && chmod u+x build_chain.sh
```
**创建`~/fisco/ipconf`文件**
向`ipconf`文件写入以下内容 —— （多机构多群组5+4节点联盟链）

```bash
192.168.80.144:5 A 1,2 30300,20200,8545
192.168.80.145:4 B 1,2 30300,20200,8545
```

> 注意：ipconf文件内不允许存在任何多余的回车。
> 格式说明：
> ​ip:节点数（当前ip/机构下共有几个节点） 机构名 群组 p2p端口,channel端口,rpc端口

**开始搭建多机多群组联盟链**
```bash
bash build_chain.sh -f ipconf -T
```
此时 fisco文件夹下会多出个nodes文件夹
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130332351.png)

将nodes/192.168.80.145目录复制到机构B机器（192.168.80.145）的~/fisco/下

```bash
scp -r 192.168.80.145 root@192.168.80.145:/root/fisco/192.168.80.145
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130410920.png)

**启动所有节点**

**机构A**进入 `~/fisco/nodes/192.168.80.144` 目录，执行`./start_all.sh`

同时**机构B**进入 `~/fisco/nodes/192.168.80.145` 目录，执行 `./start_all.sh`

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130436154.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)


**检测是否搭建成功（检测节点之间是否实现共识）**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105130458333.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)

如果有内容则表示成功达成共识，既节点搭建成功。
# 总结
**内存分配问题**

节点搭建成功了，但是异常退出，并且在log文件里找不到报警内容的时候，这个情况就很有可能是内存太小导致的原因。

推荐内存配置 节点数/1G
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210105132039203.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE5MzgxOTg5,size_16,color_FFFFFF,t_70)

**我搭建的示例并不是一个规范的示范**

在进行多机部署的时候，首先要确保的一点是 —— 单个机构掌握的节点数，应该低于共识算法可容错的数量。我部署了9个节点，只能容忍2个拜占庭节点，所以每个机构下只能有一个节点。这样可以避免机构内部强行修改自己掌握的节点数据，或一个机构的所有节点都意外出错、掉线（比如机房光纤都被挖断了），导致链无法出块。

此处可以参考：

https://mp.weixin.qq.com/s/xt-hRDAkUCCnrodwMkFjnw


**更多总结。。。**

# 参考链接
https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/tutorial/installation.html

https://mp.weixin.qq.com/s/1RGKEdcGhZbjqKv7LBrAVA

https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/design/consensus/index.html

https://blog.csdn.net/jfkidear/article/details/81275974

http://pmg.csail.mit.edu/papers/osdi99.pdf

# 关于作者
<div align=center><a href="https://blog.csdn.net/qq_19381989" target="_blank"><img src="https://img-blog.csdnimg.cn/20200427000145250.png" width="40%" /></a></div>


**作者的联系方式：**

微信：`thf056`
qq：1290017556
邮箱：1290017556@qq.com

你也可以通过 <strong><a href="https://github.com/99kies" target="_blank">github</a></strong> | <strong><a href="https://blog.csdn.net/qq_19381989" target="_blank">csdn</a></strong> | <strong><a href="https://weibo.com/99kies" target="_blank">@新浪微博</a></strong> 关注我的动态
