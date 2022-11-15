# 如何使用solidity将数据链上存储

作者：重庆电子工程职业学院 | 向键雄 谭晋

## 文章起始

在看过FISCO BCOS张开翔老师的一文说清“链上”和“链下”过后，我对于数据链上存储有了非常浓厚的兴趣，因此写下了本篇文章，用于自己进行学习
环境：solidity，webase-font，FISCO-BCOS节点链


## 正文

首先，当我第一次看到这篇文章的时候，我就对我之前脑海中的知识体系进行了颠覆，第二次看的时候我明白了其实链上链下的区别是用于提高区块链的工作效率，当我第三次看到这篇文章的时候我已经被张老师幽默风趣的写作风格折服了，老师能在幽默中将案例讲述的非常之清楚，让每个案例为什么这样做而生动，在这之后我就将我自己的想法写到了这里


首先我们先用hello-world来讲一下如何可以增加区块，如何减少区块

```

pragma solidity>=0.4.24 <0.6.11;

contract HelloWorld {
    string name;

    constructor() public {
        name = "Hello, World!";
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }
}

```

这篇合约大家已经见怪不怪了，我现在将图片贴在这里

![](https://img-blog.csdnimg.cn/5d7163c3972c49299aaac970497cd872.png)


我们部署了合约过后在这里我是17个区块，那么我们先调用get()接口看看区块是否会增长

![](https://img-blog.csdnimg.cn/bb535adfe6a147398cd2f3b65a888942.png)
![](https://img-blog.csdnimg.cn/7ac81cc496d9476aa09ac11e56991fae.png)
![](https://img-blog.csdnimg.cn/19804fc7431a44bb8e55e0a7adf1cbc6.png)

这个时候我们发现，区块并没有增加，这是怎么回事呢？那么我们再去调用一下set()接口看看是否会增长

![](https://img-blog.csdnimg.cn/75f6d7baa24f456ab9b7f8a185d86982.png)
![](https://img-blog.csdnimg.cn/5f8eba98bcb942e6893520a36b4005ae.png)
![](https://img-blog.csdnimg.cn/e1a6841454a042018a63a0c63c38d136.png)

这个时候我们发现区块居然涨了一个，这是怎么回事呢？

原来这是因为我们的交易改变了链上状态，那么这时候有问题了，get()接口难道就不改变状态吗？其实get()是不改变的，他仅仅是获取了一下我们当前的数据而已，因为我们FISCO BCSO底层有EVM的存在，所以我们可以看到在EVM中，我们的name仅仅是一个是storage变量而已，那我读取他也只是读取了栈上的数据，所以不算是读取链内因此，区块不会增长。

那么我们反过来看看set()接口，在区块链中我们说到区块链是不会放过任何一个数据变化的，因此非常适合用于数字加密，金融等领域，那么我们set()首先传入参数，在这之后将name修改为传入的参数，这里大家想一下我们是否修改了状态呢？是的，我们修改了本次状态，在这之后就开始将数据记录

光说不做假把式，我们将数据拿出来看看就知道了。

![](https://img-blog.csdnimg.cn/1d3172d5069c420e8d5163dcb7300cf4.png)

这是我们区块中的数据，这个里面就写清楚了我们的交易从哪里来到哪里去，参与共识的节点有那几个，根hash等






说了之后设计一个合约出来，用于链上与链下

```

pragma solidity ^0.4.0;


constructor() {
    
    my = msg.sender;

}
    uint account = 0;

contract    TestChain() public{
        
        event createMan(address myname,uint age,string work);
        
        struct Man {
            address Myname,
            uint Ages,
            string Work
        }
        
    
    function createme(uint ages,string works) public returns(address memory ,uint memory ,string memory){
        
        Man memory man = man(my,ages,works);
        
        return (man.Myname,man.Ages,man.Work);
        emit createMan(my,ages,works);
        
        
    }
    
    
    function getme() public view returns(struct){
        
        return (man);
        
        
    }
    
    function arrayTest() public returns(){
        
        uint[] array = new uint[](account);
        
        for(uint i = 1; i <account;i++){
            
            array[i] = i;
            account ++;
            
            
        }
        
        
        
        
    }
    
}


```


constructor是初始化的时候必会调用的数据，所以这里调用会首先执行这个东西

这里我们使用memory就可以将数据存储在本地中，所以我们会看到很多数组之类的都会用到memory这个关键词


我们首先创建了自己的的角色，createme()将我的相关参数传入，之后我们把数组也进行了实现

我们还是履行张老师文章中的哪些原则

- 记账者们收录交易，按链式数据结构打包成“区块”。

- 共识算法驱动大家验证新区块里的交易，确保计算出一致的结果。

- 数据被广播到所有节点，稳妥存储下来，每个节点都会存储一个完整的数据副本。


但是这里的话还是要坚持，让“链上”归链上，“链下”归链下这句话，因为我们的硬件资源还是很有限的比如我自己的笔记本电脑，就是16+1.5T这样子的资源是完全不够加入一个大型网络中的，可能过去跑了跑就没有了，所以为了不浪费资源我们还是要保证，必要资源上链，不然的话资源过大审核都要很久

## 本篇文章讲的还都是比较片面的语句，具体详细的，我希望我在后面弄懂了EVM这个架构的时候在与大家进行分享

