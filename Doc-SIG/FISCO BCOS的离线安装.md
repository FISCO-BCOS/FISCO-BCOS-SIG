# FISCO BCOS（十）——— FISCO BCOS的离线安装

1、需要在github上下载如下压缩包和脚本

![image-20220309075909180](./img/image-20220309075909180.png)

2、将压缩包和脚本上传至终端，随便放哪

![image-20220309080236699](./img/image-20220309080236699.png)

3、创建目录

```
mkdir fisco
```

![image-20220309080413227](./img/image-20220309080413227.png)

4、将脚本和压缩包拷贝到当前目录

```

root@wyg-virtual-machine:/home/wyg# cp -r console.tar.gz build_chain.sh webase-front.zip /root/fisco/
```

![image-20220309080850290](./img/image-20220309080850290.png)

![image-20220309080908892](./img/image-20220309080908892.png)

5、搭建单群组4节点联盟链

```
root@wyg-virtual-machine:~/fisco# bash build_chain.sh -l 127.0.0.1:4 -p 30300,20200,8545
```

![image-20220309081813116](./img/image-20220309081813116.png)

6、解压webase-front.zip

```
root@wyg-virtual-machine:~/fisco# unzip webase-front.zip
```

7、将证书拷贝至~/fisco/webase-front/conf

```
root@wyg-virtual-machine:~/fisco/webase-front/conf# cp ../../../fisco/nodes/127.0.0.1/sdk/* ./
```

![image-20220309082151270](./img/image-20220309082151270.png)

![image-20220309082608273](./img/image-20220309082608273.png)

8、启动节点

```
root@wyg-virtual-machine:~/fisco/nodes/127.0.0.1# bash start_all.sh
```

![image-20220309082725181](./img/image-20220309082725181.png)

9、打开端口

```
root@wyg-virtual-machine:~/fisco/webase-front# bash start.sh
```

![image-20220309082802054](./img/image-20220309082802054.png)

10、访问WeBASE-Front![1](./img/image-20220309083030462.png)1

