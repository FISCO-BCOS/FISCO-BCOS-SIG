FISCO BCOS（六）——— 在Ubantu上安装python

1、以root用户或具有sudo访问权限的用户身份运行以下命令，以更新软件包列表并安装必备组件：

```
sudo apt update
sudo apt install software-properties-common
```

![image-20220228184140025](./img/image-20220228184140025.png)

![image-20220228184156504](./img/image-20220228184156504.png)

2、将deadsnakes PPA添加到系统的来源列表中：

```
sudo add-apt-repository ppa:deadsnakes/ppa
```

![image-20220228184426377](./img/image-20220228184426377.png)

出现提示 按enter继续

3、启用存储库后，请使用以下命令安装Python 3.8：

```
sudo apt install python3.8
```

![image-20220228184601116](./img/image-20220228184601116.png)

4、通过键入以下命令验证安装是否成功：

```
python3.8 --version
```

![image-20220228184715156](./img/image-20220228184715156.png)