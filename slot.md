# 循序渐进打造可升级智能合约之有趣的插槽（slot）

## 前言

在循序渐进打造可升级智能合约系列中的使用delegatecall时构造器的坑篇章埋了一个坑——为什么在合约中会出现声明一个从不使用的变量来作为占位符，这就不得不提到今天的主角slot了，在学习完evm slot以后，一切都会被解释清楚。

## 正文

### EVM存储结构

EVM存储结构主要分为Code、Storage、Stack、Args、Memory，本文主要介绍Storage，也就是在智能合约开发中常说的状态变量所在的位置。

#### Storage

Storage在EVM中体现为一个哈希表map，通过key-value的形式来一一映射智能合约中状态变量的值，且key的值是根据定义状态变量定义顺序从0开始决定的。以下通过代码进一步介绍Storage：

```
contract Demo{
	uint256 a = 100;
	uint256 b = 200;
}
```

如以上合约代码所示，声明了uint256类型状态变量a、b，那么这两个变量在Stroage中的体现则为map(0)=>a值、map(1)=>b值，为了方便讲解，此处暂不介绍EVM中对存储的优化。

当定义了结构体，则按照结构体中的变量来依次占用插槽slot（后面都会使用slot来指代key）：

```
contract Demo{
	struct s{
		uint256 a;
		uint256 b;
	}
	uint256 c;
}
```

如上合约代码所示，则slot-0、slot-1分别映射结构体s的变量a与b值，slot-2则映射c的值。

更多不同类型状态变量的slot映射，请查阅EVM相关文档，本文意在引出slot，来介绍可升级合约中“占坑”的原因。

### 变量顺序引起的问题

使用本系列文章前面使用过的可升级逻辑的合约来修改并演示问题。

#### 逻辑合约

`````
pragma solidity >=0.5.2;
import "./Owner.sol";
contract LogicContract is Owner{
    uint256 public number;
    
    function  getNumber() public view returns (uint256){
        return number;
    }
    
    function incrNumber() {
        number++;
    }
}
`````

#### 代理合约

`````
pragma solidity >=0.5.2;
import "./LogicContract.sol";
contract Proxy{
    address internal implement;
    uint256 internal number;
    
    function() external { 
    	(bool success, ) = getImplement().delegatecall(msg.data);
            assembly {
              let free_mem_ptr := mload(0x40)
              returndatacopy(free_mem_ptr, 0, returndatasize())
              switch success
              case 0 { revert(free_mem_ptr, returndatasize()) }
              default { return(free_mem_ptr, returndatasize()) }
        }
    }
    
    function setImplement(address addr) public{
        implement = addr;
    }
    
    function getImplement() public view returns (address){
        return implement;
    }
}
`````

#### 问题现象

我们将Proxy合约中的状态变量顺序作了调换，在通过代理合约执行业务逻辑的时候就出现了不正确的结果。

#### 问题原因

上文提到了slot相关的知识，我们可以知道逻辑合约中状态变量number位于slot-0，代理合约中状态变量number位于slot-1，EVM在对Storage上的数据进行读写时不是根据变量名的，而是获取变量的slot后，通过slot来读写Storage上的数据，由此可知，在此案例中，代理合约调用逻辑合约方法时是使用slot-0来更改Storage上的数据的（为什么是slot-0，前面提到了delegatecall的操作最终会作用到代理合约的环境，在逻辑合约中修改了slot-0，最终作用到代理合约），也就是我们代理合约表面修改了状态变量number，实际修改了状态变量implement。

#### 解决方法

很简单，将代理合约中的变量调换顺序即可，如果你看到这考虑到了一个问题——如果增加新数据结构该怎么办，那么你很快就写出可升级结构的合约就不远了。

## 结语

本文简单的介绍了EVM中的插槽概念，解释了为什么**循序渐进打造可升级智能合约系列之使用delegatecall时构造器坑篇章**中的合约代码声明了无用的状态变量作为占位符，读者想更深入的了解EVM的话，可以自行查阅相关资料。
