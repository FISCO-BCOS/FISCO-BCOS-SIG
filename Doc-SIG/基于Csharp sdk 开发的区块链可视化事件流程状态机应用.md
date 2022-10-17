基于Csharp sdk 开发的区块链可视化事件流程状态机应用

一、简介

基于元磁之力开源框架中的快速开发框架YC.Boilerplate、结合FISCOBCOS Csharp Sdk进行组合开发，前端使用vue 技术栈，结合antv x6组件实现图形可视化，最终实现区块链操作上链流程可视化。

二、视频教程路径

B站教程：https://www.bilibili.com/video/bv17G4y1k74u?vd_source=d13b0630d8f5bdd49b00820fee2bcbde


三、图文教程如下

> 场景构想

区块链的应用开发的主要对象目前还是以开发人员为主，即使简单的区块链交互上链动作，也需要开发人员有一定的区块链开发认知基础，并进行一定代码量的开发，才可以实现区块链应用的构建。

> 需求提炼

1. 有没有一种工具或手段可以让开发人员、普通用户针对常规流程业务上链等需求，不需要编写代码，只需要简单操作，就可以完成一个区块链应用上链呢？
2. 一个多步骤的业务流程上链，在区块链系统是看不见、摸不着，大部分需要开发人员进行二次开发处理，才可以展示。有没有一种工具可以直观的查看区块链上流程，相比列表等更加形象的展示，如：图的交互。

>  实现方案

（一） 技术组合
基于元磁之力开源框架中的快速开发框架YC.Boilerplate、结合FISCOBCOS Csharp Sdk进行组合开发，前端使用vue 技术栈，结合antv x6组件实现图形可视化。

（二）实现思路
通过构建图形化流程设计，让普通人员也可以快速构建上链流程，在区块链上链存证等业务场景中，只需要选择流程中的对应行为进行对应数据填报，使用Csharp Sdk实现区块链的通信并进行上链操作，每次数据上链动作等都会和已上链数据进行比对校验，防止非法上链。

（三）技术实现

1. 构建流程，采用系统流程设计器进行如下操作。
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/1.%E6%B5%81%E7%A8%8B%E8%AE%BE%E8%AE%A1.png)
2. 在流程管理可以查看创建流程
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/2.1%E6%B5%81%E7%A8%8B%E5%88%97%E8%A1%A8.png)
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/2.2%E6%B5%81%E7%A8%8B%E6%9F%A5%E7%9C%8B.png)
3. 在区块链存证示例中进行流程上链操作，业务录入人员需要流程行为和存证数据一一关联，就可以完成上链存证。其中：事务id为整个事件流程唯一id，同一个事件流程下，同一个业务行为只能上链一次，如果有另外需求需要进行二次开发。

- 存证操作
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.1.%E5%AD%98%E8%AF%81.png)
- 查看存证
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.2%20%E6%9F%A5%E7%9C%8B%E5%AD%98%E8%AF%81%E9%9D%A2%E6%9D%BF.png)

- webasefront 查看交易记录
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.3%E5%8C%BA%E5%9D%97%E9%93%BE%E4%BA%A4%E6%98%93%E8%AE%B0%E5%BD%95.png)

- 查看合约数据
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.4%E5%8C%BA%E5%9D%97%E9%93%BE%E5%90%88%E7%BA%A6%E8%AE%B0%E5%BD%95%E6%9F%A5%E8%AF%A2.png)
- 查看合约对应的存储数据
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.5%E5%8C%BA%E5%9D%97%E9%93%BE%E5%90%88%E7%BA%A6%E9%93%BE%E4%B8%8A%E7%95%99%E5%AD%98%E6%95%B0%E6%8D%AE.png)
- 可视化查看业务流程行为存证
![image](https://github.com/linbin524/FISCO-BCOS-SIG/blob/main/Doc-SIG/img/3.6%E5%9B%BE%E5%83%8F%E5%8A%A8%E6%80%81%E6%9F%A5%E7%9C%8B%E8%A1%8C%E4%B8%BA%E5%AD%98%E8%AF%81%E6%95%B0%E6%8D%AE.png)

四、 附录

> 下载源码地址（下载最新master）
https://github.com/yc-l/yc.boilerplate

> 操作流程

1. 将src\Db\traceevidencedb.sql 在mysql 数据库中创建

2. src\Backstage\BasicAppLayer\YC.ServiceWebApi\DefaultConfig.json 文件修改mysql 数据库信息

3. \src\Backstage\BlockChain\FISCOBCOS.CSharpSdk\Config\BaseConfig.cs 配置区块链rpc 地址

4. 将后端还原运行、前端运行，即可查看效果
 
 备注：详细基础操作查看框架说明文档：http://doc.yc-l.com/#/quickStart
