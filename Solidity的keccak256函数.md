# Solidity的keccak256详解

>学校： 深圳职业技术学院
>
>作者：钟文慧

## 介绍

### 1.keccak256的背景

以太坊在许多地方使用_Keccak-256_加密哈希函数。Keccak-256被设计为于2007年举行的SHA-3密码哈希函数竞赛的候选者。Keccak是获胜的算法，在2015年被标准化为 FIPS（联邦信息处理标准）。

不过NIST接受原始的Keccak256设计后，更改了Padding的格式， 以太坊坚持使用了原始的方案，因为这一更改存在争议，导致了正式的SHA3实现和原始的Keccak不兼容。



### 2.用途

为了隐藏起某些信息，且保证这些信息不被篡改，需要用到[哈希](https://so.csdn.net/so/search?q=哈希&spm=1001.2101.3001.7020)算法。keccak256算法则可以将任意长度的输入压缩成64位16进制的数，且哈希碰撞的概率近乎为0.



### 3.原理图

![image-20220415162725779](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/a.png)





## Solidity 加密函数

Solidity 提供了常用的加密函数。以下是一些重要函数：

- `keccak256(bytes memory) returns (bytes32)` 计算输入的Keccak-256散列。
- `sha256(bytes memory) returns (bytes32)` 计算输入的SHA-256散列。
- `ripemd160(bytes memory) returns (bytes20)` 计算输入的RIPEMD-160散列。
- `ecrecover(bytes32 hash, uint8 v, bytes32 r, bytes32 s) returns (address)` 从椭圆曲线签名中恢复与公钥相关的地址，或在出错时返回零。函数参数对应于签名的ECDSA值: r – 签名的前32字节; s: 签名的第二个32字节; v: 签名的最后一个字节。这个方法返回一个地址。



keccak的用途： 

为了隐藏起某些信息，且保证这些信息不被篡改，需要用到哈希算法。keccak256算法则可以将任意长度的输入压缩成64位[16进制](https://so.csdn.net/so/search?q=16进制&spm=1001.2101.3001.7020)的数，且哈希碰撞的概率近乎为0



### 1.第一个示例

下面的例子说明了加密函数的用法：

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

contract keccak256Test {   
	/*
	* @callKeccak256
	* 返回一个bytes32数据类型的keccak256的加密值
	*/
   function callKeccak256() public pure returns(bytes32 result){
   	  // 用keccak256来生成唯一标识
      return keccak256("ABC");
   }  
}
```

输出的结果： 

```bash
bytes32: result 0xe1629b9dda060bb30c7908346f6af189c16773fa148d3366701fbaa35d54f3c8
```

> 注: 在区块链中安全地产生一个随机数是一个很难的问题， 本例的方法不安全，但是在我们的Zombie DNA算法里不是那么重要，已经很好地满足我们的需要了。

将此处的ABC更改为`“ABD”`

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

contract keccak256Test {   
	/*
	* @callKeccak256
	* 返回一个bytes32数据类型的keccak256的加密值
	*/
   function callKeccak256() public pure returns(bytes32 result){
   	  // 用keccak256来生成唯一标识
      return keccak256("ABD");
   }  
}
```

输出的结果：

```bash
bytes32: result 0xd5234846f6dd69362b89130f125f51644cfa60dc54fba2f920ac00998fe21668
```

从差别上来看，改变一个字母，keccak256算法计算完的哈希值相差非常大，所以可以看出哈希碰撞的概率近乎为0。



### 2.生成数据唯一标识

可以利用`keccak256`来生成一些数据的唯一标识。比如我们有几个不同类型的数据，像uint，string，address，可以先用abi.encodePacked方法将他们打包编码，然后再用keccak256来生成唯一标识：

示例说明： 

用户需要把三个不同数据类型的传参打包编码之后进行keccak256加密函数来生成唯一的标识。

- 创建`keccakHash`函数
- 传递参数为：`string`类型的姓名，`uint256`类型的年龄，`address`类型的地址
- 函数返回值： 返回bytes32类型的唯一标识

```solidity
// SPDX-License-Identifier: MIT
pragma solidity 0.8.4;

contract KeccakHash {
    bytes32 _msg = keccak256(abi.encodePacked("0xAA"));

    // 唯一数字标识
    /*
    * @hash
    * @string	姓名
    * @uint256	年龄
    * address	地址
    */
    function hash(
        string memory _name,
        uint256  _age,
        address _addr
    ) public pure returns (bytes32) {
    	// 打包编码用keccak256来生成唯一标识
        return keccak256(abi.encodePacked(_name, _age, _addr));
    }

}
```

- 使用remix-ide调用查看，当前的三个参数已经被打包编码生成唯一标识。

![image-20230504204051267](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305042040470.webp)



### 3.弱抗碰撞性

`keccak256`演示一下之前讲到的弱抗碰撞性，即给定一个消息`x`，找到另一个消息`x'`使得`hash(x) = hash(x')`是困难的。

给定一个消息`0xAA`，试图去找另一个消息，使得它们的哈希值相等：

```solidity
// SPDX-License-Identifier: MIT
pragma solidity 0.8.4;

contract KeccakHash {
    bytes32 _msg = keccak256(abi.encodePacked("0xAA"));

    // 唯一数字标识
    /*
    * @hash
    * @string	姓名
    * @uint256	年龄
    * @address	地址
    */
    function hash(
        string memory _name,
        uint256  _age,
        address _addr
    ) public pure returns (bytes32) {
    	// 打包编码用keccak256来生成唯一标识
        return keccak256(abi.encodePacked(_name, _age, _addr));
    }
    
    // 弱抗碰撞性
    /*
    * @weak
    * @string	消息
    */ 
    function weak(
        string memory _message
    )public view returns (bool){
    	// 判断两个哈希是否碰撞 返回布尔值
        return keccak256(abi.encodePacked(_message)) == _msg;
    }
}
```

- 使用remix-ide调用查看，发现当前的返回的布尔值是true，完成验证哈希的弱抗碰撞性。

![image-20230504204108554](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305042041734.webp)



### 4.强抗碰撞性

`keccak256`演示一下之前讲到的强抗碰撞性，即找到任意不同的`x`和`x'`，使得`hash(x) = hash(x')`是困难的。

构造一个函数`strong`，接收两个不同的`string`参数`_message1`和`_message2`，然后判断它们的哈希是否相同：

```solidity
// SPDX-License-Identifier: MIT
pragma solidity 0.8.4;

contract KeccakHash {
    bytes32 _msg = keccak256(abi.encodePacked("0xAA"));

    // 唯一数字标识
    /*
    * @hash
    * @string	姓名
    * @uint256	年龄
    * @address	地址
    */
    function hash(
        string memory _name,
        uint256  _age,
        address _addr
    ) public pure returns (bytes32) {
    	// 打包编码用keccak256来生成唯一标识
        return keccak256(abi.encodePacked(_name, _age, _addr));
    }
    
    // 弱抗碰撞性
    /*
    * @weak
    * @string	消息
    */ 
    function weak(
        string memory _message
    )public view returns (bool){
        return keccak256(abi.encodePacked(_message)) == _msg;
    }

    // 强抗碰撞性
    /*
    * @strong
    * string	消息
    */ 
    function strong(
        string memory _message1,
        string memory _message2
    )public pure returns (bool){
    	// 判断两个哈希是否碰撞 返回布尔值
        return keccak256(abi.encodePacked(_message1)) == keccak256(abi.encodePacked(_message2));
    }
}
```

- 使用remix-ide调用查看，发现当前的返回的布尔值是false，完成验证哈希的强抗碰撞性。

![image-20230504204129705](https://blog-1304715799.cos.ap-nanjing.myqcloud.com/imgs/202305042041851.webp)