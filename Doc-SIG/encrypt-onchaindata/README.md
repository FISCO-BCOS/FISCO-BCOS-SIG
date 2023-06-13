#### 支持环境
os: linux
arch: 仅测试amd64

#### 使用

*encrypt* 为编译好的linux/amd64 下的二进制文件，可以直接运行。

参数:

- -f [xx.pem] 指定.pem文件，加密模式时为公钥，解密时为私钥
- -c *e* 时为加密模式 *d* 为解密模式
- -t 加密模式时，为明文字符串（需要加密的内容）；解密模式时为密文的base64字符串

在AES密钥分发的场景下，需要加密的内容则是**AES密钥**。

流程如下，alice为share合约的发起者，需要在这个合约上共享一些数据给bob和carlo，而不给dell。
1. alice收集bob和carlo的公钥
2. 使用bob和carlo各自的公钥给aes密钥加密，再发回给bob和carlo。
3. alice，bob，carlo，dell都可以使用share合约，但是alice，bob，carlo提交到share的数据都是经过aes密钥加密的，只有他们自己可以解密，dell看到的是密文。
