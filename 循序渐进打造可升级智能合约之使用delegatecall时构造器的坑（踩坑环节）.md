# 循序渐进打造可升级智能合约之使用delegatecall时构造器的坑（踩坑环节）

作者：深圳职业技术学院高旭亮

## 前言

在循序渐进打造可升级智能合约系列前面的篇章中我们学习到可升级智能合约的核心原理是使用delegatecall，那么delegatecall我们就可以随心所欲的使用吗，本篇会写一个作者在开发合约应用时遇到的一个坑点——在可升级合约中使用了solidity构造器导致的bug。

## 正文

### 案例介绍

作者是在测试应用是出现的bug，应用中逻辑合约实现了Owner接口，并且在构造器中设置了所有者（部署合约者为最初的所有者），此处会用较为简短的代码来复现bug

### 案例代码

#### 所有者合约

`````
contract Owner {

    address private owner;
    
    event OwnerSet(address indexed oldOwner, address indexed newOwner);
    
    modifier isOwner() {
        require(tx.origin == owner, "Caller is not owner");
        _;
    }
    
    constructor() {
        owner = msg.sender; 
        emit OwnerSet(address(0), owner);
    }

    function changeOwner(address newOwner) public isOwner {
        emit OwnerSet(owner, newOwner);
        owner = newOwner;
    }

    function getOwner() external view returns (address) {
        return owner;
    }
}
`````

#### 逻辑合约

此处incrNumber添加了需要为逻辑合约所有者的限制

`````
pragma solidity >=0.5.2;
import "./Owner.sol";
contract LogicContract is Owner{
    uint256 public number;
    
    function  getNumber() public view returns (uint256){
        return number;
    }
    
    function incrNumber() public isOwner{
        number++;
    }
}
`````

#### 代理合约

注意：此处新增的a状态变量，暂且理解为一个占位符，为的是对齐代理合约与逻辑合约的插槽顺序，后面会编写一篇让读者学习solidity slot相关的知识，引出为何可升级合约有可升级逻辑和可升级数据结构的区别。

`````
pragma solidity >=0.5.2;
import "./LogicContract.sol";
contract Proxy{
	uint256 public a;
    uint256 internal number;
    address internal implement;
    
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

### 问题解析

这些代码看起来没有任何问题，但是在部署完逻辑合约，并且通过代理合约调用逻辑合约的incrNumber方法却出现问题了，在WeBASE-Front上给出的报错状态码为0x16，作者在掉了许多头发后发现了一个问题，我通过代理合约调用逻辑合约，获取的逻辑合约Owner竟然是0x00000....，但我们不是在部署逻辑合约的时候构造器就设置了Owner吗？

对，坑就在这里，相信聪明的读者已经反应过来了，我们在部署逻辑合约的时候的确执行了构造方法，设置了Owner，但很显然我们是在逻辑合约上修改了区块链数据，这个在可升级合约中是绝对不能出现的，我们的数据需要存储在代理合约上。在我们通过代理合约调用逻辑合约的方法，进入isOwner这个修饰器的tx.origin==Owner这个判断时，代理合约里Owner状态变量是初始值0x00000....，那调用必然就失败了。

那既然构造器用不了，我们需要怎么做呢？我们可以在逻辑合约编写一个初始化方法，然后部署完通过代理合约调用逻辑合约来完成初始化。

### 手动初始化

- 去除Owner合约中的构造器方法（逻辑合约如有构造方法同样去除）
- 在逻辑合约中编写初始化方法来代替构造器方法

`````
pragma solidity >=0.5.2;
import "./Owner.sol";
contract LogicContract is Owner{
    uint256 public number;
    
    function initDemo() public {
    	owner = tx.origin; 
        emit OwnerSet(address(0), owner);
    }
    
    function  getNumber() public view returns (uint256){
        return number;
    }
    
    function incrNumber() public isOwner{
        number++;
    }
}
`````

通过代理合约来调用逻辑合约的初始化方法设置所有者即可。

## 结尾

本篇文章通过真实的案例来为读者解析编写可升级合约时构造器的坑，主要需要注意的点是一定得区分好逻辑合约与代理合约的职能，确保任何数据都不能上链到逻辑合约上，在开发可升级合约时逻辑合约不能使用构造器，需要手动初始化。

