# Solidity的异常处理总结



## 介绍

Solidity 提供了很多错误检查和错误处理的方法。通常，检查是为了防止未经授权的代码访问，当发生错误时，状态会恢复到初始状态。

在学习了Solidity的一段时间之后，接触到了Soldity的异常处理的方法，总结了有以下重要的方法：

- error：节省gas的自定义异常，搭配 revert对状态所做的任何更改将被还原。

- assert： 如果不满足条件，此方法调用将导致一个无效的操作码，对状态所做的任何更改将被还原。

- require： 如果不满足条件，此方法调用将恢复到原始状态。此方法用于检查输入或外部组件的错误。

- revert： 此方法将中止执行并将所做的更改还原为执行前状态。

- try catch：外部调用的失败，可以通过 `try/catch`语句来捕获。

  

## GAS比较

主要是比较以下三种异常的gas消耗，分别是error、require、assert：

1. `error`方法`gas`消耗最少
2. `require`方法`gas`消耗一般
3. `assert`方法`gas`消耗较多

因为error是比较节省gas，也能抛出所需要的异常，所以推荐使用error。



## 异常的应用场景

实际上，我们希望程序不要出现问题，用户操作永远逻辑清晰而正确，但是我们无法去避免一些程序代码会发生的错误，但是我们不希望用户在操作的时候，出现错误异常或者其他问题，不会反馈任何信息，或者无法处理该异常。

就比如在转账的过程中，我们可以考虑是否有异常操作，比如我们转账的时候账户的金额不够，我就就会造成无法转账，但是我们不希望在余额不足的时候把转账的业务继续进行下去，所以我们需要使用异常处理操作，我们可以判断当前的账户余额是否大于转账余额，判断对方的账户地址是否正确。如果满足条件就继续进行，否则触发异常操作，需要抛出一个异常，以便回退。



## 案例分析

### 1.Error和Revert结合使用

在^0.8.4版本，合约结构增加了**错误（error）**，为应对失败时，错误可以在revert 中使用。与错误字符串相比，error花费更少的gas（即更便宜），并且允许编码额外的数据，还可以使用natspec注释形式。

> 在执行当中，`error`必须搭配`revert`（回退）命令使用。也可以单独使用 `revert` 语句和 `revert` 函数来直接触发回退。

这里使用一个简单的转账案例来演示一下Error搭配Revert的使用，使用call的方法进行转账。

- 自定义Error的异常
- 基本的转账事件
- 使用revert语句触发异常回退

```solidity
// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.7.0 <0.9.0;

// 自定义 error异常
error CallFailed();

contract RevertTest {
    // 构造函数 
    // @payable  使得部署的时候可以转eth进去
    constructor() payable{}
    
    // 创建转账事件
    event TransferTo(address from,address _to,uint256 amount);

    // 用call()发送ETH
    function callETH(address payable _to, uint256 amount) external payable{
        (bool success,) = _to.call{value: amount}("");
        
        // 处理下call的返回值，如果失败，revert交易并发送error
        if (!success) {
            revert CallFailed();
            
            // 使用revert函数描述错误
            //revert("Not enough Ether provided.");
        }
        // 触发转账事件
        emit TransferTo(msg.sender,_to,amount);
    }
    
    // 返回合约ETH余额
    function getBalance() view public returns(uint) {
        return address(this).balance;
    }

}
```

使用Remix-IDE编译部署合约

![image-20221125020704512](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/image-20221125020704512.png)

部署合约之后可以通过getBalance函数查看当前的余额，然后更换转账的地址进行转账可以发现当前是转账成功的，有相应的输出查看。

![image-20221125020909715](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/image-20221125020909715.png)

把收账的地址更换为转账人的地址，触发当前的异常错误。

![image-20221125021152791](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/image-20221125021152791.png)



### 2.Require用法

**require**函数应用于确保有效条件，例如满足输入或合约状态变量，或验证调用外部合约的返回值。

遵循这种范式，形式分析工具可以验证永远无法到达无效操作码：这意味着代码中的不变量没有被违反，并且代码已经过形式验证。

使用方法：`require(检查条件，"异常的描述")`，当条件不成立就会抛出解释异常。



这里使用的是简单的转账合约，需要满足的条件是收账的人的地址不能是转账人的地址。

- 判断的条件 `address != msg.sender`
- require异常配合修饰符使用

```solidity
// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.7.0 <0.9.0;

contract RevertTest {
    // 构造函数 
    // @payable  使得部署的时候可以转eth进去
    constructor() payable{}

    // 可以把require的异常写道修饰符中
    modifier Transferor(address _to){
        require(_to == msg.sender, "Transferor and receiver cannot be themselves");
        _;
    }
    
    // 创建转账事件
    event TransferTo(address from,address _to,uint256 amount);

    // 用transfer()发送ETH
    /*
    function transferETH(address payable _to, uint256 amount) external payable{
        // 如果当前的收账人是当前钱包的发起方 就会提示错误信息
        require(_to == msg.sender, "Transferor and receiver cannot be themselves");
        _to.transfer(amount);

    }
    */

    function transferETH(address payable _to, uint256 amount) external payable Transferor(_to){
        // 如果当前的收账人是当前钱包的发起方 就会提示错误信息
        require(_to == msg.sender, "Transferor and receiver cannot be themselves");
        _to.transfer(amount);
        
        // 触发转账事件
        emit TransferTo(msg.sender,_to,amount);
    }

    
    // 返回合约ETH余额
    function getBalance() view public returns(uint) {
        return address(this).balance;
    }

}
```

使用Remix-IDE编译部署，在VALUE处填写5000进行部署合约。

![image-20221125084745503](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/image-20221125084745503.png)

修改收账地址为部署合约的地址，然后触发异常。

![image-20221125085051204](https://pic1.xuehuaimg.com/proxy/raw.githubusercontent.com/isicman/image/main/img/image-20221125085051204.png)



### 3.Assert用法

> 函数**assert**和**require**可用于检查条件并在不满足条件时抛出异常。

**assert**函数应该只用于测试内部错误和检查不变量。assert和require不一样，assert没有字符串，所有不能解释抛出一个异常的原因。

该函数的用法只需要判断检查的条件即可，不成立就会抛出异常。

这里使用的是简单的转账合约，需要满足的条件是收账的人的地址不能是转账人的地址。

```solidity
// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.7.0 <0.9.0;

contract RevertTest {
    // 构造函数 
    // @payable  使得部署的时候可以转eth进去
    constructor() payable{}
    
    // 创建转账事件
    event TransferTo(address from,address _to,uint256 amount);

    // 用transfer()发送ETH
    function transferETH(address payable _to, uint256 amount) external payable{
        // 如果当前的收账人是当前钱包的发起方 就会提示异常
        assert(_to == msg.sender);
        _to.transfer(amount);
        
        // 触发转账事件
        emit TransferTo(msg.sender,_to,amount);
    }

    // 返回合约ETH余额
    function getBalance() view public returns(uint) {
        return address(this).balance;
    }

}
```

assert的测试方法和require的测试方法一样。



### 4.Try...Catch用法

这是引用官方的案例：https://learnblockchain.cn/docs/solidity/control-structures.html#assert-require-revert

对于try  catch而言，`try` 关键字后面必须跟一个表示外部函数调用,或合约创建的表达式`(new ContractName())`。
 表达式内部的错误不会被捕获(例如，如果它是一个包含内部函数调用的复杂表达式)，只会在外部调用本身内部发生还原。
 这个 `returns` 后面的部分（可选）声明与外部调用返回的类型匹配的返回变量。
 在没有错误的情况下，这些变量被赋值，并在第一个成功块内继续执行合约。如果到达成功块的末尾，则在 `catch` 块之后继续执行。

这里的用法其实和其他的语言也大同小异，相对Java的try catch的用法，

```java
try  {
      // 读取文件的操作
      InputStream is = new FileInputStream("/null/null");
  } catch (Exception e) {
      // 如果出现了异常类Exception类型的异常，那么执行该代码
      e.printStackTrace();
  }finally {
       // 最终都会执行
  }
```

如下是Solidity中的try catch的用法。

```solidity
// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.8.1;

interface DataFeed { function getData(address token) external returns (uint value); }

contract FeedConsumer {
    DataFeed feed;
    uint errorCount;
    function rate(address token) public returns (uint value, bool success) {
        // 如果错误超过 10 次，永久关闭这个机制
        require(errorCount < 10);
        try feed.getData(token) returns (uint v) {
            return (v, true);
        } catch Error(string memory ) {
            // 如果在getData内部调用 revert，并提供了一个原因字符串，则执行此操作。 
            errorCount++;
            return (0, false);
        } catch Panic(uint) {
            // 这个是在Panic情况下执行,例如一个严重的错误，除以0或溢出。
            // 错误代码可以用来确定错误的类型。
            errorCount++;
            return (0, false);
        } catch (bytes memory) {
            // 这是在使用revert()时执行的
            errorCount++;
            return (0, false);
        }
    }
}
```





