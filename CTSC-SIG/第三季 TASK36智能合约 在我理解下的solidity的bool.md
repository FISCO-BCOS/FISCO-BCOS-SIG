​#第三季 TASK36智能合约 在我理解下的solidity的bool

作者： 重庆电子工程职业学院 | 向键雄 杨易
## 前言

环境使用

remix编译器点我跳转

## 正文

我们可以想一下，如果我们遇到了一个对象，他需要判断，这个时候怎么样会最简单呢？

是直接返回对和错？还是直接返回报错？又或者是拿很多fi else 去进行判断？

在solidity中我们就有这样的一个变量“bool”，这个变量可以说功能很简单，但却在哪里都有用到，先简单讲一下这个类型在solidity的作用，通常用于指定或者是判断，他只有两个选项，“true和false”，通过判断返回值来判断一个变量是否正确与符合规则

与其他语言中的bool一样，他不是一个整数也不能转换为一个整数，他就是一个值类型，任何赋值给其他的bool变量都会创建一个新的副本。

任何赋值给其他的bool变量都会创建一个新的副本。这句话我们如何理解呢？

因为我们的bool类型也算是一个值类型，所以我们通过赋值的时候也会像其他的类型一样，变成一个变量，那么这个变量他就会进行副本的创建，就和我们的正常变量一样。

## bool与运算符
bool支持运算符是逻辑运算符和关系运算符

### 逻辑运算符
|||
|-|-|
|！	|逻辑非|
|&&	|逻辑与|
|||	|逻辑或|


### 关系运算符
|||
|-|-|
|==	|等于|
|！|=	不等于|

使用方法如下：

- bool pubilc _bool = true;                                        结果 _bool: ture

- bool public _bool1 = !_bool; 取反                          结果_bool1: false     

- bool public _bool2 = _bool && _bool1; 与             结果_bool2: false

- bool public _bool3 = _ bool || _bool1; 或               结果_bool3: true

- bool public _bool4 = bool == _bool1; 等于            结果_bool4: false

- bool public _ bool = _bool1 != _ bool1 ; 不相等     结果_bool4:true

### 特殊记忆

bool默认值为false

两种声明类型

bool a;  //不带默认值
bool b=false;  //带默认值
短路
与很多语言中一样sol也是有着短路的存在，比如||逻辑或中前面的变量为true，那么不会执行后面的逻辑直接断开，同样在&&逻辑与中，前面的逻辑为false后面的逻辑就不会被执行
### 实际运用

```

pragma solidity ^0.4.23; //声明版本

contract BooleanTest { //声明是一个合约 contract是合约声明关键字

bool _a; //声明了一个bool类型的变量

function getBool() public view returns(bool) { //定义了一个函数叫getBool 后面的权限为公共只读，返回一个bool类型

return !_a; //这里的话是灵活运用可以取反也可以直接返回

}

}

```



​
