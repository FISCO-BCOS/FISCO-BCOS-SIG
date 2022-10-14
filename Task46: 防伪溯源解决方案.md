# Task46: 防伪溯源解决方案
### 作者: 深职院-符博<br>
## 一, 概述<br>
(1) 本文主要说明商品溯源的解决方案。在我们的生活中会经常遇到一些仿冒伪劣的商品，这些商品流通于大大小小的市场中，购买到这些劣质产品是肯定会对我们消费者带来一定的损害，给人们的生活也带来了许多麻烦。
区块链追溯平台是以区块链技术为底层数据存储方式的产品全程可信追溯平台，对政府检查、工厂品牌方管理、消费者扫码营销都有积极的促进作用。区块链作为分布式加密记账技术，把信息存储分散开来，比如存储在公链或者联盟链中，区别于以往的中央式存储方式，规避了许多信息安全方面的风险。
在应用逐渐增多、走向成熟的今天，在食品安全领域的应用尤其具有代表性，从食品原料信息、供应商信息、生产加工信息、质检信息、仓储信息、物流信息、代理商信息、终端门店信息、消费者信息，全链打通，轻松实现扫码展示。<br>

### (2) 与传统的方法相比，区块链防伪都具有众多优势，其链上分布式账本逻辑配合链下自动化信息采集可以保证溯源信息的时效性和不可篡改性。区块链技术似乎让我们的生活变得更加美好，但区块链技术只能保证链上信息不可篡改，而无法保证信息的真实准确性，比如之前央视焦点访谈报道了北京某超市有机蔬菜造假事件，最讽刺的是该商品贴有农业区块链溯源平台标识。我们使用的联盟链技术拥有者效率高控制精确等优势，但由于其没有公链那么公开透明，往往会陷入自说自话的尴尬局面，这个问题就不是区块链技术能解决的了，我们必须在拥有强大的政府背书和权威监管机构的监管下去运行整个系统，保证链上链下数据的一致性，可靠性。现在我就给大家讲解一下我的思路和想法。<br>

## 二, 合约流程概述<br>
## 1, 合约流程概述及流程图
### (1)
### 整体流程构思: 首先在政府部门和相关监管机构的强大背书牵头下，我们可以打造一个由区块链构建的溯源交易机构，生产商，中间商，质监局，大型超市为机构里的角色(还可以有物流，仓储等角色，本文只设计了这四个角色)。大型超市售卖的的货物必须通过这个机构进行交易,生产商和中间商若想售卖给大型超市商品就必须加入这个机构, 在通过质检局审核后的商品方可出售给超市。 链下购买，链上记录，在相关部门的监管下，将链上链下数据巧妙结合，保证链上数据的真实可靠性，最终消费者可通过扫二维码的方式调用链上接口进行产品溯源查询。

### (2) 流程图<br>
![image](https://user-images.githubusercontent.com/103564714/193531370-e175ceb3-659a-4e53-8cbb-23e0fa22cb94.png)

## 三， 合约解析
### (1) DataSource合约
此合约里定义了合约中的实体类和一些常量及映射我在其都给好了注释。
```solidity
// SPDX-License-Identifier: SimPL-2.0
pragma solidity >=0.5.0;

contract DataSource {

    
    enum UserTypeEnum{IS_PRODUCER, IS_MIDDLEMAN, IS_MARKET} // 枚举 用户类型 生产商， 中间商， 超市

    // 用户实体
    struct User {
        address id;  // 用户id address
        string userName; // 用户名
        UserTypeEnum userType; // 0 生产商 1中间商 2超市
        uint256[] goodsList;    // 订单idList
        bool validate; // 校验
    }

    // 第三方质监局
    struct Inspect {
        address id; // 质监局id
        string userName; // 昵称

        uint256[] goodsList; // 收获商品
        bool validate;
    }

    // 中间商购买商品订单详情
    struct Goods {
        uint256 id; // 商品订单id
        string goodsName; // 商品名称
        uint256 amount; // 数量
        uint256 price; // 价格
        address[] traceAddress; // 商品流通溯源 存储经手用户地址
        string[] traceName; // 商品流通溯源 存储经手用户名称
        uint256[] traceTimeStamp; // 商品流通溯源 经手时间戳
        uint256 number; // 经手次数
        bool isShip; // 已发货
        bool isReceipt; // 质监局是否收货
        bool isArrival; // 购买者是否收货
        bool isPass; // 是否通过质监局检查
        bool validate;
    }

    // 中间商发布通过质检的商品实体
    struct MiddlePassGoods {
        uint256 id; // 商品id
        uint256 goodsId; // 购买生产商时的订单id 二者一对一关联 相当于溯源凭证
        string middleGoodsName;
        bool validate;
    }

    // 超市购买后的订单 链上简约 链下详情
    struct MarketGoods {
        uint256 id; // 订单id
        uint256 middleGoodsId; // 上架商品id
        string middleGoodsName; // 商品名称
        uint256 buyTimeStamp; // 下单时间
        bool isShip; // 是否发货
        bool validate;
    }

    // 用户实体映射 id => User
    mapping(address => User) internal  userMapping;

    // 质监局实体映射 id => Inspect
    mapping(address => Inspect) internal inspectMapping;

    // 商品订单映射 id => Goods
    mapping(uint256 => Goods) internal goodsMapping;

    // 商品订单id => 生产商地址
    mapping(uint256 => address) internal goodsIdToProducerMapping;

    // 商品订单id => 中间商地址(购买时存储)
    mapping(uint256 => address) internal goodsIdToMiddlemanMapping;

    // 商品订单id => 质监局地址
    mapping(uint256 => address) internal goodsIdToInspectMapping;

    // 中间商发布商品 id => MiddlePassGoods 
    mapping(uint256 => MiddlePassGoods) internal middlePassGoodsMapping;

    // 商品订单id只能对应一个商品 防止重复绑定
    mapping(uint256 => bool) internal goodsIdBindingMapping;


    // 超市订单id => 超市地址
    mapping(uint256 => address) internal markGoodsIdTomarkAddressMapping;

    // 超市订单id => 超市订单
    mapping(uint256 => MarketGoods) internal markGoodsIdToMarketGoodsMapping;

    // 中间商购买商品订单id 自增
    uint256 internal goodsId = 1;

    // 中间商发布商品 id 自增
    uint256 internal middleGoodsId = 1;

    // 超市购买订单id 自增
    uint256 internal marketGoodsId = 1;
    
    // 合约部署者地址
    address internal Deployer;
}
```
### (2) Producer生产商合约 此合约继承了Datasource合约，拥有注册 发货 查询等功能。在中间商向其下单后，随之会生成订单与其绑定 生产商即可向质监局发货进行质检审核。
```solidity
// SPDX-License-Identifier: SimPL-2.0

pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;

// 引入合约
import "./DataSource.sol"; 
contract Producer is DataSource {

    constructor() public {
        Deployer = msg.sender;
    }
    
     
    // 重要操作只允许合约部署者调用
     modifier isDeployer() {
        require(tx.origin == Deployer, "No permission to call");
        _;
    }
    
    // 判断是生产商
     modifier isProducer(address userId) {
        require(userMapping[userId].userType == UserTypeEnum.IS_PRODUCER, "Not a Producer");
        _;
    }

            // 判断质监局是否存在
     modifier isInspect(address inspectId) {
        require(inspectMapping[inspectId].validate == true, "is Not inspect");
        _;
    }

        // 判断用户是否存在
     modifier isNotExist(address userId) {
        require(userMapping[userId].validate == false, "User exist");
        _;
    }

            // 判断用户是否存在
     modifier isExist(address userId) {
        require(userMapping[userId].validate == true, "User is not exist");
        _;
    }

        // 发货时判断商品所有权
     modifier isYourProduer(address userId, uint256 goodsId) {
        require(goodsIdToProducerMapping[goodsId] == userId, "is not you producer");
        _;
    }

        // 判断商品是否存在
          modifier goodsIsExist(uint256 goodsId) {
        require(goodsMapping[goodsId].validate == true, "Goods Not exist");
        _;
    }
        // 判断商品是否发货
         modifier goodsIsNotShip(uint256 goodsId) {
        require(goodsMapping[goodsId].isShip == false, "Goods is ship");
        _;
    }

        // 发货事件
    event toInspectEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address fromAddress, // 发货方地址
        address toInspectAddress, // 收货质监局地址
        uint256 toTimeStamp // 发货时间戳
    );
        

    // 注册
    function addProducer(address userId, string memory userName) 
    public
    isNotExist(userId)
    isDeployer
    returns (bool)
    {   
        uint256[] memory goodList = new uint256[](0);
        User memory user = User(userId, userName, UserTypeEnum.IS_PRODUCER, goodList , true);
        userMapping[userId] = user;
        return true;
    }




    // 中间商购买后向指定质监局发货 
    function toInspect(uint256 goodsId, address inspectAddress)
    public 
    goodsIsExist(goodsId)
    goodsIsNotShip(goodsId)
    isYourProduer(msg.sender, goodsId)
    returns (bool)
     {  
         // 绑定关系
        Inspect storage inspect = inspectMapping[inspectAddress];
        inspect.goodsList.push(goodsId);
        goodsIdToInspectMapping[goodsId] = inspectAddress;    


        Goods storage goods = goodsMapping[goodsId];
        goods.isShip = true;
        emit toInspectEvent(goodsId, goods.goodsName, goods.amount, goods.price, msg.sender, inspectAddress, block.timestamp);
        return true;
     }
     

<!--      // 生产商，质监局，中间商查询所有订单及状态
    function getGoodsStatus(address userId)
    public
    view
    isExist(msg.sender)
    returns (uint256[] memory idList, // 商品id
        string[] memory goodsNameList, // 商品名称
        uint256[] memory amountList, // 数量
        uint256[] memory priceList, // 价格
        address[][] memory traceAddressList, // 商品流通溯源 存储经手用户地址
        string[][] memory traceNameList, // 商品流通溯源 存储经手用户名称
        uint256[][] memory traceTimeStampList, // 商品流通溯源 经手时间戳
        bool[] memory isShipList, // 已发货
        bool[] memory isReceiptList, // 质监局是否收货
        bool[] memory isArrivalList, // 购买者是否收货
        bool[] memory isPassList // 是否通过质监局检查
        )
    {
        require(msg.sender == userId, "is not you id");
        
        User storage user = userMapping[msg.sender];
        idList = new uint256[](user.goodsList.length);
        goodsNameList = new string[](user.goodsList.length);
        amountList = new uint256[](user.goodsList.length);
        priceList = new uint256[](user.goodsList.length);
        traceAddressList = new address[][](user.goodsList.length);
        traceNameList = new string[][](user.goodsList.length);
        traceTimeStampList = new uint256[][](user.goodsList.length);
        isShipList = new bool[](user.goodsList.length);
        isReceiptList = new bool[](user.goodsList.length);
        isArrivalList = new bool[](user.goodsList.length);
        isPassList = new bool[](user.goodsList.length);

        for (uint256 i = 0; i < user.goodsList.length; i++) {
            Goods storage good = goodsMapping[user.goodsList[i]];
            idList[i] = good.id;
            goodsNameList[i] = good.goodsName;
            amountList[i] = good.amount;
            priceList[i] = good.price;
            traceAddressList[i] = good.traceAddress;
            traceNameList[i] = good.traceName;
            traceTimeStampList[i] = good.traceTimeStamp;
            isShipList[i] = good.isShip;
            isReceiptList[i] = good.isReceipt;
            isArrivalList[i] = good.isArrival;
            isPassList[i] = good.isPass;
        }
    } -->
    
    // 生产商，质监局，中间商根据id查询订单及状态
    function getGoodsStatusById(uint256 goodsId)
    public
    view
    isExist(msg.sender)
    returns (uint256 id, // 商品id
        string memory goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address[] memory traceAddress, // 商品流通溯源 存储经手用户地址
        string[] memory traceName, // 商品流通溯源 存储经手用户名称
        uint256[] memory traceTimeStamp, // 商品流通溯源 经手时间戳
        bool isShip, // 已发货
        bool isReceipt, // 质监局是否收货
        bool isArrival, // 购买者是否收货
        bool isPass // 是否通过质监局检查
        )
    {
            Goods storage good = goodsMapping[goodsId];
            return (good.id, 
            good.goodsName, 
            good.amount,
            good.price, 
            good.traceAddress, 
            good.traceName, 
            good.traceTimeStamp, 
            good.isShip, 
            good.isReceipt, 
            good.isArrival, 
            good.isPass);
        
    }

}  
```
### (3) Middleman中间商合约 此合约继承了Producer合约，拥有注册 购买生产者商品 上架通过审核商品 向超市发货 查询等功能。在向生产商下单后，会随之会生成订单与其绑定 生产商即可向质监局发货进行质检审核， 审核通过后即可收货 通过审核的商品可以上架 超市购买后可以向其发货。
```solidity
// SPDX-License-Identifier: SimPL-2.0

pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;

// 引入合约
import "./Producer.sol"; 
contract Middleman is Producer {

    // 判断是中间商
    modifier isMiddleman(address userId) {
        require(userMapping[userId].userType == UserTypeEnum.IS_MIDDLEMAN, "Not a Middleman");
        _;
    }

    // 判断是自己购买的商品
    modifier isYourGoods(address userId, uint256 goodsId) {
        require(goodsIdToMiddlemanMapping[goodsId] == userId, "is not you goods");
        _;
    }

    // 需质审通过后才允许收货
    modifier isPass(uint256 goodsId) {
        require(goodsMapping[goodsId].isPass == true, "goods is not pass");
        _;
    }

    // 需质收货后才能上架
    modifier isArrival(uint256 goodsId) {
        require(goodsMapping[goodsId].isArrival == true, "goods is not isArrival");
        _;
    }

    // 不能重复上架 一个商品订单对应一个商品
    modifier isNotPut(uint256 goodsId) {
         require(goodsIdBindingMapping[goodsId] == false, "goods is binding");
        _;
    }

    // 商品已上架
    modifier isPut(uint256 middleGoodsId) {
         require(middlePassGoodsMapping[middleGoodsId].validate == true, "goods is not put");
        _;
    }

    // 中间商收货事件
     event middleReceiptEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address fromAddress, // 发货方地址
        address toInspectAddress, // 收货质监局地址
        address toMiddleAddress, // 中间商收货地址
        uint256 receiptTimeStamp // 收货时间戳
    );


    // 注册
    function addMiddleman(address userId, string memory userName) 
    public
    isNotExist(userId)
    isDeployer
    returns (bool)
    {   
        uint256[] memory goodList = new uint256[](0);
        User memory user = User(userId, userName, UserTypeEnum.IS_MIDDLEMAN, goodList , true);
        userMapping[userId] = user;
        return true;
    }


    // 购买
    function buyFromProudcer
    (string memory goodsName, uint256 amount, uint256 price, address producerAddress, string memory producerName)
    public 
    isMiddleman(msg.sender)
    isProducer(producerAddress)
    returns (bool)
    {
        address[] memory traceAddress = new address[](0);
        string[] memory traceName = new string[](0);
        uint256[] memory traceTimeStamp = new uint256[](0);

        Goods memory goods = Goods(goodsId, goodsName, amount, price, traceAddress, traceName, traceTimeStamp, 0 , false, false, false, false, true);
        goodsMapping[goodsId] = goods;
        
        goodsMapping[goodsId].traceAddress.push(producerAddress);
        goodsMapping[goodsId].traceName.push(producerName);
        goodsMapping[goodsId].traceTimeStamp.push(block.timestamp);
        // 发货时需判断是否为此生产商
        goodsIdToProducerMapping[goodsId] = producerAddress;
        goodsIdToMiddlemanMapping[goodsId] = msg.sender;
        

        // 生产商 中间商生产订单
        User storage middUser = userMapping[msg.sender]; 
        middUser.goodsList.push(goodsId);
        
        User storage prodUser = userMapping[producerAddress]; 
        prodUser.goodsList.push(goodsId);
        
        goodsId++;
        return true;
    }


    // 取消订单 发货前
    function cancel(uint256 goodsId)
    goodsIsExist(goodsId)
    public 
    goodsIsNotShip(goodsId)
    isYourGoods(msg.sender, goodsId)
    returns (bool)
    {
        delete goodsIdToProducerMapping[goodsId];
        delete goodsIdToMiddlemanMapping[goodsId];
        delete goodsMapping[goodsId];
        return true;
    }

    // 中间商收货
    function middleReceipt(uint256 goodsId)
    public 
    isPass(goodsId)
    isMiddleman(msg.sender)
    isYourGoods(msg.sender, goodsId)
    returns (bool)
    {
        Goods storage goods = goodsMapping[goodsId];

        // 更改订单状态
        User storage user = userMapping[msg.sender];
        goods.traceAddress[goods.number] = msg.sender;
        goods.traceName[goods.number] = user.userName;
        goods.traceTimeStamp[goods.number] = block.timestamp;

        goods.isArrival = true;
        goods.number += 1;

        // 收货后设置为false  上架后设置为true 防止重复上架
        goodsIdBindingMapping[goodsId] = false;

        // 收货事件
        emit middleReceiptEvent(goodsId, goods.goodsName, goods.amount, goods.price, 
        goods.traceAddress[goods.number - 2], goods.traceAddress[goods.number - 1], msg.sender ,block.timestamp);
        return true;

    }


    // 上架已收货商品
    function putIsPassGoods(uint256 goodsId)
    public 
    isArrival(goodsId)
    isMiddleman(msg.sender)
    isYourGoods(msg.sender, goodsId)
    isNotPut(goodsId)
    returns (bool)
    {
        // 存储实体
        MiddlePassGoods memory mGoods = MiddlePassGoods(middleGoodsId, goodsId, goodsMapping[goodsId].goodsName, true);
        middlePassGoodsMapping[middleGoodsId] = mGoods;

        // 上架后设置为true
        goodsIdBindingMapping[goodsId] = true;
        middleGoodsId++;
        return true;
    } 


}
```
### (4) Inspect质检局合约 此合约继承了Middleman合约，拥有注册 收货 检验审核商品 查询等功能。在向生产商向其发货后，对应的订单会与其绑定 收货审查都会实时记录订单状态 审核通过后即可向中间商发货 中间商即可收货并更改链上信息 最后中间商上架质检通过的商品 超市即可购买。
```solidity
// SPDX-License-Identifier: SimPL-2.0

pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;

// 引入合约
import "./Middleman.sol"; 
contract Inspect is Middleman {

    // 判断质监局是否存在
     modifier inspectNotExist(address inspectId) {
        require(inspectMapping[inspectId].validate == false, "Inspect exist");
        _;
    }

        // 判断是自己要质检的商品
    modifier isInspectGoods(address inspectId, uint256 goodsId) {
        require(goodsIdToInspectMapping[goodsId] == inspectId, "is not you goods");
        _;
    }

        // 判断是否已收货
    modifier isReceipt(uint256 goodsId) {
        require(goodsMapping[goodsId].isReceipt == true, "is not receipt");
        _;
    }

        // 判断是否未收货
    modifier isNotReceipt(uint256 goodsId) {
        require(goodsMapping[goodsId].isReceipt == false, "is not receipt");
        _;
    }

        // 检验通过和不通过只允许一次
    modifier isNotPass(uint256 goodsId) {
        require(goodsMapping[goodsId].isPass == false, "goods is Pass");
        _;
    }

        // 收货事件
    event receiptEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address fromAddress, // 发货方地址
        address toInspectAddress, // 收货质监局地址
        uint256 receiptTimeStamp // 收货时间戳
    );

        // 检验通过事件
    event isPassEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address fromAddress, // 发货方地址
        address toInspectAddress, // 收货质监局地址
        uint256 isPassTimeStamp // 质检通过时间戳
    );

        // 检验不通过事件
    event isNotPassEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        uint256 amount, // 数量
        uint256 price, // 价格
        address fromAddress, // 发货方地址
        address toInspectAddress, // 收货质监局地址
        uint256 isNotPassTimeStamp, // 质检不通过时间戳
        string message // 质检不通过原因
    );

      // 注册
    function addInspect(address inspectId, string memory userName) 
    public
    inspectNotExist(inspectId)
    isDeployer
    returns (bool)
    {   
        uint256[] memory goodList = new uint256[](0);
        Inspect memory inspect = Inspect(inspectId, userName, goodList, true);
        inspectMapping[inspectId] = inspect;
        return true;
    }



    // 收货
    function receipt(uint256 goodsId)
    public
    isInspect(msg.sender)
    isInspectGoods(msg.sender, goodsId)
    isNotReceipt(goodsId)
    returns (bool)
    {
        // 获取生产商地址
        address producerAddress = goodsIdToProducerMapping[goodsId];

        // 更改订单状态
        User storage user = userMapping[msg.sender];
        goodsMapping[goodsId].traceAddress.push(msg.sender);
        goodsMapping[goodsId].traceName.push(user.userName);
        goodsMapping[goodsId].traceTimeStamp.push(block.timestamp);
        goodsMapping[goodsId].isReceipt = true;
        
        Goods storage goods = goodsMapping[goodsId];
        goods.number += 1;
        // 质监局收货事件
        emit receiptEvent(goodsId, goods.goodsName, goods.amount, goods.price, producerAddress, msg.sender, block.timestamp);
        return true;
    }


    // 检验合格
    function goodsIsPass(uint256 goodsId)
    public
    isInspect(msg.sender)
    isInspectGoods(msg.sender, goodsId)
    isReceipt(goodsId)
    isNotPass(goodsId)
    returns (bool)
    {
        Goods storage goods = goodsMapping[goodsId];
        goods.isPass = true;

        emit isPassEvent(goodsId, goods.goodsName, goods.amount, goods.price, goods.traceAddress[0], msg.sender, block.timestamp);
        return true;
    }

    // 检验不合格 触发未通过质检事件 链下监听事件, 通知买家，退款等。  可关闭其店铺, 勒令其一定时间内整改。
    function goodsIsNotPass(uint256 goodsId, string memory message) 
    public
    isInspect(msg.sender)
    isInspectGoods(msg.sender, goodsId)
    isReceipt(goodsId)
    returns (bool)
    {
        Goods storage goods = goodsMapping[goodsId];
        emit isNotPassEvent(goodsId, goods.goodsName, goods.amount, goods.price, goods.traceAddress[0], msg.sender, block.timestamp, message);

        // 删除相关值和映射
        
        delete goodsIdToInspectMapping[goodsId];
        Inspect storage inspect = inspectMapping[msg.sender];
        for (uint256 i = 0; i < inspect.goodsList.length; i++) {
            if(goodsId == inspect.goodsList[i]) {
                inspect.goodsList[i] == inspect.goodsList[inspect.goodsList.length - 1];
                inspect.goodsList.pop();
            }
        }

        return true;
    }
}
```
### (5) Market超市合约 此合约继承了Inspect合约，最后部署这个合约就可以了。拥有注册 收货 商品溯源查询等功能。超市可向中间商购买已通过质检的商品，购买后会创建订单，在成功收货后此商品将可被完整溯源，用户可通过扫码等形式调用链上接口查看。
```solidity
// SPDX-License-Identifier: SimPL-2.0

pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;

// 引入合约
import "./Inspect.sol"; 
contract Market is Inspect {


    // 判断是超市
     modifier isMarket(address userId) {
        require(userMapping[userId].userType == UserTypeEnum.IS_MARKET, "Not a Market");
        _;
    }

        // 判断是超市购买的货物
     modifier isYourMiddGoods(address userId, uint256 middleGoodsId) {
        require(markGoodsIdTomarkAddressMapping[middleGoodsId] == userId, "Not your middleGoods");
        _;
    }

    // 超市收货事件
     event marketReceiptEvent(
        uint256 id, // 商品id
        string goodsName, // 商品名称
        address fromAddress, // 发货方地址
        uint256 receiptTimeStamp // 收货时间戳
    );


    // 注册
    function addMarket(address userId, string memory userName) 
    public
    isNotExist(userId)
    isDeployer
    returns (bool)
    {   
        uint256[] memory goodList = new uint256[](0);
        User memory user = User(userId, userName, UserTypeEnum.IS_MARKET, goodList , true);
        userMapping[userId] = user;
        return true;
    }

    // 购买
    function buyMiddleGoods(uint256 middleGoodId)
    public 
    isPut(middleGoodId)
    isMarket(msg.sender)
    returns (bool)
    {
        MiddlePassGoods storage mid = middlePassGoodsMapping[middleGoodId];
        MarketGoods memory mGoods = MarketGoods(marketGoodsId, middleGoodId, mid.middleGoodsName, block.timestamp, false, true);

        // 添加订单
        User storage user = userMapping[msg.sender];
        user.goodsList.push(marketGoodsId);

        // 关系绑定
        markGoodsIdToMarketGoodsMapping[marketGoodsId] = mGoods;
        markGoodsIdTomarkAddressMapping[marketGoodsId] = msg.sender;
        return true;
    }
 
    // 取消订单
    function cancelMiddleGoods(uint256 marketGoodsId)
    public
    isMarket(msg.sender) 
    isYourMiddGoods(msg.sender, marketGoodsId)
    returns (bool)
    {
        User storage user = userMapping[msg.sender];

        for (uint256 i = 0; i < user.goodsList.length; i++) {
            if (user.goodsList[i] == middleGoodsId) {
                user.goodsList[i] = user.goodsList[user.goodsList.length];
                user.goodsList.pop();
            }
        }
        delete markGoodsIdToMarketGoodsMapping[marketGoodsId];
        delete markGoodsIdTomarkAddressMapping[marketGoodsId];

        return true;
    }


    // 收货
    function receiptMiddleGoods(uint256 marketGoodsId)
    public
    isMarket(msg.sender) 
    isYourMiddGoods(msg.sender, marketGoodsId)
    returns (bool)
    {
        MarketGoods storage marketGoods = markGoodsIdToMarketGoodsMapping[marketGoodsId];
        Goods storage goods = goodsMapping[middlePassGoodsMapping[marketGoods.middleGoodsId].goodsId]; 

        User storage user = userMapping[msg.sender];
        goods.traceAddress.push(msg.sender);
        goods.traceName.push(user.userName);
        goods.traceTimeStamp.push(block.timestamp);

        emit marketReceiptEvent(marketGoodsId, marketGoods.middleGoodsName, goods.traceAddress[goods.number - 1], goods.traceTimeStamp[goods.number]);
        return true;

    }

    // 用户在超市购买时查看商品溯源信息
    function traceability(uint256 marketGoodsId)
    public 
    view 
    returns (
        uint256 id, // 商品id
        string memory goodsName, // 商品名称
        address[] memory traceAddressList, // 商品流通溯源 存储经手用户地址
        string[] memory traceName, // 商品流通溯源 存储经手用户名称
        uint256[] memory traceTimeStamp, // 商品流通溯源 经手时间戳
        bool isPass // 是否通过质监局检查
        )
        {
           MarketGoods storage market = markGoodsIdToMarketGoodsMapping[marketGoodsId];
           Goods storage goods = goodsMapping[middlePassGoodsMapping[market.middleGoodsId].goodsId];
           return (marketGoodsId, goods.goodsName, goods.traceAddress, goods.traceName, goods.traceTimeStamp, goods.isPass);
        }
}
```
## 四, 合约测试
### (1) 我们先注册四个用户 分别是 producer middleman inspect market 对应的就是生产商 中间商 质检局 超市 (注册必须要用部署合约的用户地址 我在其做了限制)。
![image](https://user-images.githubusercontent.com/103564714/193643339-ccbe29b1-7e5f-43f7-b55f-4b79912db7f5.png)
### (2) 此时中间商可以在链下对生产商上架的商品进行购买, 在链上记录其购买信息 生成对应订单 将生产商和中间商绑定 (调用buyFromProducer方法 需使用中间商地址) 购买成功后调用查询信息可以查看订单状态。
![image](https://user-images.githubusercontent.com/103564714/193643407-1d315d67-9d27-46d6-a64b-f41248374533.png)<br>
![image](https://user-images.githubusercontent.com/103564714/193643628-fcecf374-d114-4f00-9a6e-d52c3f98cf13.png)<br>
### (3) 生产商收到订单后可向指定质检局发货(调用toInspect方法 需使用生产商地址 成功后会触发发货事件) 由质检局进行质检
![image](https://user-images.githubusercontent.com/103564714/193644144-1ee969f2-fa5e-4b82-951c-d4961dcb8357.png)
### (4) 质检局调用收货方法后 会更新其订单状态 并将订单与其绑定(调用receipt方法 需要使用质检局地址 成功后也会触发质检局收货事件)
![image](https://user-images.githubusercontent.com/103564714/193644638-30f037f3-d5a7-40f7-b34c-91575f1e2e0d.png)
### (5) 质检局检验成功后即会更改订单状态触发通过事件向中间商发货(这里的发货我想的是审核通过在链下进行操作) 若审核不通过则会取消其对应订单 并触发未通过事件 这时就需要在链下进行处理 向中间商退款 和勒令生产商整改等措施。
![image](https://user-images.githubusercontent.com/103564714/193645463-f85ccfb9-4bb3-42a4-916f-58a44c517e9e.png)
### (6) 中间商调用收货方法后会触发收货事件 更新订单状态(调用middleReceipt方法需要选择中间商地址)
![image](https://user-images.githubusercontent.com/103564714/193645907-0b3d7844-52f7-4519-a554-dcb0089deb86.png)
### (7) 中间商收货后的商品可以进行上架(调用putIsPassGoods方法需要使用中间商地址 详情信息可记录在链下)
![image](https://user-images.githubusercontent.com/103564714/193646176-2128e6c3-0006-42b2-8fe1-6219ea56de0e.png)
### (8) 此时超市就可以购买中间商的物品 购买后会生成新的订单 该订单与中间商购买生产商时的订单一 一对应 可进行关联查询(调用buyMiddleGoods方法需要使用超市地址 购买详情信息可记录在链下)
![image](https://user-images.githubusercontent.com/103564714/193646494-0784b09e-e9d4-4e8d-8f12-64f9ac65cb81.png)
### (9) 超市购买后中间商即可发货(这里的发货我也选择在链下进行处理)，超市收货后则会触发收货事件 并合并完整的溯源信息 (调用receiptMiddleGoods方法需使用超市地址)
![image](https://user-images.githubusercontent.com/103564714/193647762-635a7bd1-bc4c-45d7-a150-39cd999becc1.png)
### (10) 超市收货后即可进行溯源信息的查询 包括 商品的id 名称 经手的用户地址,名称 时间戳 是否通过质检等信息
![image](https://user-images.githubusercontent.com/103564714/193648039-1862afa9-ad34-475a-8386-6e9057b248fe.png)

## 五，总结
### 本篇文章讲述了我对商品溯源的一些想法，合约还有很多需要改进的地方，比如一些发货的功能我选择在链下处理，但这其实可以引入物流公司的角色将物流的实时信息也存储在链上,我只是简单的抛砖引玉了一下，更完善的解决方案大家可以根据我这个去拓展~















