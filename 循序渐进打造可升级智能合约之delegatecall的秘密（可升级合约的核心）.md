# 循序渐进打造可升级智能合约之delegatecall的秘密（可升级合约的核心）

作者：深圳职业技术学院高旭亮

## 前言

在循序渐进打造可升级智能合约系列的牛刀小试篇章中我们实现了可升级逻辑的智能合约，但为什么通过代理合约调用逻辑合约产生的区块链数据修改会在代理合约上呢，如果将牛刀小试篇章中代理合约代码的delegatecall修改成call，会发现，产生的区块链数据修改不在代理合约上了，不难得知，delegatecall是理清可升级智能合约原理的核心。本篇会讲解solidity的多种合约调用方式。

## 正文

### solidity不同的调用方式

在solidity中，我们可以通过call、delegatecall、callcode等三种方式实现跨合约的函数调用，他们主要的区别是在于内置变量msg与运行环境（简单理解为操作哪个合约的数据），由于callcode在0.5.0以后的版本禁用了，本文不作演示。

#### call

假设有一个合约为A，通过A的地址.call()方式调用合约（参数为calldata），内置变量msg的值会修改为调用者(即谁调用了该call方法，内置变量msg就是谁的信息)，运行环境则为被调用者A，此时链上数据的操作都会作用于A合约上。

例如我们将牛刀小试篇章中代理合约的delegatecall修改成call方式调用，会发现代理失效了，因为运行环境为被调用者A合约，对区块链的操作作用于A合约了，此时代理则不会有数据的更改。

```
pragma solidity >=0.5.2;
contract LogicContract{
    uint256 public number;
    
    function  getNumber() public view returns (uint256){
        return number;
    }
    
    function incrNumber() public{
        number++;
        number++;
    }
}
```

```
pragma solidity >=0.5.2;
import "./LogicContract.sol";
contract Proxy{
    uint256 internal number;
    address internal implement;
    
    
    function setImplement(address addr) public{
        implement = addr;
    }
    
    function getImplement() public view returns (address){
        return implement;
    }
    
    function callGetNumber() public returns (uint256){
        // 获取逻辑合约中对应方法的选择器
        bytes4 getNumber = LogicContract(implement).getNumber.selector;
        // calldata
        bytes memory data = abi.encodeWithSelector(getNumber);
        // 进行delegatecall调用
        (bool success, bytes memory result) = implement.call(data);
        return abi.decode(result, (uint256));
    }
    
    function callIncrNumber() public returns (bool){
        // 获取逻辑合约中对应方法的选择器
        bytes4 getNumber = LogicContract(implement).incrNumber.selector;
        // calldata
        bytes memory data = abi.encodeWithSelector(getNumber);
        // 进行delegatecall调用
        (bool success, ) = implement.call(data);
        return success;
    }
}
```

#### delegatecall

假设有一个合约为A，通过A的地址.delegatecall()方式调用合约（参数为calldata），相比于call方式，此处内置变量msg的值不会修改为调用者，且运行环境则为当前调用者合约（比如代理合约），此时链上数据的操作就会作用于调用者合约上，我们自然可以升级逻辑合约而链上数据依然存在了，这就是代理合约存储数据的核心原理。

## 结尾

本文简单解析了为什么通过代理合约调用逻辑合约时作出的数据修改会作用于代理合约上，让读者明白了可升级智能合约的核心原理，delegatecall在使用中需要多加注意，后续会有篇章让读者避开一些“坑”。