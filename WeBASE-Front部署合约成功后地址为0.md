# WeBASE-Front部署合约成功后地址为0

作者：深圳职业技术学院高旭亮

## 前言

作者在编写合约完毕后部署的时候，发生了部署后合约地址为0x00000..的现象，且在合约列表也能够看到地址0x00000...的合约，最终锁定了原因。

## 正文

### 现象演示

为了方便讲解，简写了造成该情况的代码，从图片能够看出部署成功后的合约地址的确是0x00000...，应该是WeBASE中间件代码的错误造成的（下面会说为什么下这个结论）。

![图片](https://user-images.githubusercontent.com/85018486/194747000-56acaf21-bffb-41f9-a894-a40e6306f95e.png)

![图片](https://user-images.githubusercontent.com/85018486/194747035-a1237d98-dadf-46a8-aeb1-8483e85b443a.png)

### 出现原因

在构造函数中出现了revert（如require不满足条件，手动revert()），都会出现该现象。

### 在Remix上的现象

由下图可得知应该是WeBASE中间件代码的问题。

![图片](https://user-images.githubusercontent.com/85018486/194747052-b086d4de-a0f9-4b72-aa18-60a05f3ee34f.png)

### 建议

尽量避免在构造函数中使用require、revert等函数。

## 结论

这个也算是WeBASE中间件的一个BUG吧，刚发生部署完成0x000...的时候是比较手足无措的，现在因为时间精力不允许，没办法修改这个BUG，有社区大神比较熟悉WeBASE代码的，可以修复一下～

