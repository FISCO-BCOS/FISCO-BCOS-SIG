# 任务47: Task挑战赛, 版权保护校验解决方案。
### 作者: 深职院-符博<br>

当今社会大多数人版权意识薄弱，这导致侵权事件时常发生，而我们的原作者在维权的过程也难免遇到很多问题，就比如作品没有办法确权，取证难，司法诉讼时间长等问题。而区块链特有的不可篡改、可追溯校验的技术特性,就很适合解决版权保护的发展瓶颈。所以区块链技术在一定程度上是能够助推司法诉讼，助力原作者举证维权，减少电子证据取证难、易消亡、易篡改、技术依赖性强等问题的存在，在知识产权保护等领域发挥它的技术作用。下面就来看看本文的重点---->版权保护校验解决方案的智能合约。<br>

ps: 真正的区块链实战开发是要结合链上和链下的，本文将业务都写在了合约里，是为了方便大家理解阅读

# 一, 合约解析 (本文共有四个合约)
![image](https://user-images.githubusercontent.com/103564714/189173733-c48965cf-7fde-4ec6-92a1-9225f08f7324.png)


# 1, DataSource.sol<br>
第一个合约是用来定义合约中的实体 常量 映射
我在其都给好了注释
```Solidity
// SPDX-License-Identifier: SimPL-2.0
pragma solidity >=0.5.0;
// 此合约用来 定义 实体类, 映射, 常量。


contract DataSource {
    
    // 用于举报状态的枚举
    enum ReportStatus{WAIT_STATUS, NOW_STATUS, END_STATUS} // 0: 待受理 1: 受理中 2: 已结案
    
    // 普通用户
    struct User {
        address id;  // 用户id address
        string userName; // 用户名
        uint256 amount;  // 金额
        uint256[] propertys; // 持有的产权
        bool validate; // 校验
    }
    
    
    // 法院
    struct Court {
        address id;	// id
        string courtName; 	// 法院名称
        uint256[] reportPropertys; // 接收到被侵权的产权id 
        bool validate; // 校验
    } 

    
    
    // 产权
    struct Property {
        uint256 id;  // 产权id
        string propertyName; 	//  产权名称
        string describe;	// 描述
        string document;  //  证明文件图片地址 因为我们现在的图片和文件都是存储在云上的 如腾讯云 阿里云等 所以这里存储用的是图片或者文件的地址 "https://xxxx,https://xxxx" ps: 这里可以用string[]数组进行存储 但因为遇到了一些小bug所以我这里选择用字符串拼接。
        uint256 authenticationTime;	// 鉴权时间戳
        bool isSell; 	// 是否正在售卖
        bool isReport; // 是否正在处理举报侵权
        bool validate; // 校验
    }
    
    // 产权售卖详情
    struct PropertySell {
        uint256 id;  // 产权id
        address userId; // 所属人地址
        string propertyName; //  产权名称
        string describe;	// 描述
        uint256 authenticationTime;	// 鉴权时间戳
        uint256 sellTime; // 上架时间
        uint256 price; // 价格
    }
    
    
    // 举报信息详情
    struct ReportInfo {
        uint256 id; // 产权id
        address reporterAddress; // 举报人地址
        string telephone; // 举报人电话/ 联系方式
        string email; // 举报人邮件
        string information; // 举报材料图片地址 "https://xxxx, https://xxxx" 说明同上
        uint256 reportTime; // 举报时间
        ReportStatus status; // 处理状态
    }
    
    
    // 用户id => 用户
    mapping(address => User) internal userMapping;
    
    // 产权id => 产权
    mapping(uint256 => Property) internal propertyMapping;
    
    // 产权id => 用户地址
    mapping(uint256 => address) internal propertyIdToUserAddressMapping;
    
    // 法院id => 法院
    mapping(address => Court) internal courtMapping;
    
    // 产权id => 产权售卖信息
    mapping(uint256 => PropertySell) internal propertySellMapping;
    
    // 产权id => 举报信息
    mapping(uint256 => ReportInfo) internal reportInfoMapping;
    
    // 产权id => 法院地址 (用于受理侵权)
    mapping(uint256 => address) internal propertyIdToCourtAddressMapping;
     
    // 判断是否是法院
    mapping(address => bool) internal isCourtMapping;
    
    
    
    
    // 存储全部产权id
    uint256[] internal PropertyIds;
    
    // 存储全部产权售卖id
    uint256[] internal PropertySellIds;
    
    // 存储全部法院地址
    address[] internal CourtIds;
    
    // 部署合约的用户地址
    address internal Deployer;
    
    // 产权id 自增
    uint256 internal propertyId = 0;
    
}
```

# 2,AppraisalBasic.sol<br>
本合约用于基本的用户法院注册，版权认证，信息查询。此合约继承了DataSource.sol 我在其也都给好了注释
```Solidity
// SPDX-License-Identifier: SimPL-2.0
pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;
// 引入合约
import "./DataSource.sol"; 
contract AppraisalBasic is DataSource {
    
    // 初始化 用于后续校验
    constructor()  {
        Deployer = msg.sender;
    }
    
    // 提交到售卖行需判断产权是自己的
     modifier myOwner(address userId, uint256 id) {
        require(propertyIdToUserAddressMapping[id] == userId, "Property is does owner");
        _;
    }
     
    // 重要操作只允许合约部署者调用
     modifier isDeployer() {
        require(tx.origin == Deployer, "No permission to call");
        _;
    }
    
    
    // 判断用户是否存在
     modifier userExist(address id) {
        require(userMapping[id].validate, "User does not exist");
        _;
    }
    
    // 用户注册时判断是否注册过
     modifier userNotExist(address id) {
        require(!userMapping[id].validate, "User already exists");
        _;
    }
    
     
    // 判断法院是否存在
     modifier courtExist(address id) {
        require(courtMapping[id].validate, "Court does not exist");
        _;
    }
    
    // 法院注册时判断是否注册过
     modifier courtNotExist(address id) {
        require(!courtMapping[id].validate, "Court already exists");
        _;
    }
    
    // 判断产权是否存在
    modifier propertyExist(uint256 id) {
        require(propertyMapping[id].validate, "Property does not exist");
        _;
    }
    
    
    // 产权注册时判断是否存在
    modifier propertyNotExist(uint256 id) {
        require(!propertyMapping[id].validate, "Property already exists");
        _;
    }
    // 判断是否是法院
    modifier isCourt(address id) {
        require(isCourtMapping[id], "Not a court");
        _;
    }
    
    
    // 产权注册事件
    event RegisteredProperty(uint256 id,  // 用户id
        string propertyName, 	//  产权名称
        string describe,	// 描述
        string document,  //  证明文件图片地址 https://xxxx
        uint256 authenticationTime	// 鉴权时间戳)
        );
     
    
    // 用户注册
    /* address id;  // 用户id address
        string userName; // 用户名
        uint256 amount;  // 金额
        uint256[] property; // 持有的产权
        bool validate; // 校验*/
    function userRegister (address id, string memory userName) 
    public 
    userNotExist(id) 
    returns (bool)
    {
        // 初始化用户所拥有产权数组
        uint256[] memory propertys = new uint256[](0);
        // 初始金额为0
        User memory user = User(id, userName, 500, propertys, true);
        // 用户id => 用户
        userMapping[id] = user;
        return true;
    }
    
    // 法院注册
    function courtRegister (address id, string memory courtName) 
    public 
    courtNotExist(id) 
    returns (bool)
    {
        uint256[] memory reportPropertys = new uint256[](0);
        Court memory court = Court(id, courtName, reportPropertys, true);
        courtMapping[id] = court;
        isCourtMapping[id] = true;
        CourtIds.push(id);
        return true;
    }
    
        /*产权注册
/*      uint256 id;  // 用户id
        string propertyName; 	//  产权名称
        string describe;	// 描述
        string[] document;  //  证明文件图片地址 https://xxxx
        uint256 authenticationTime;	// 鉴权时间戳
        bool isSell; 	// 是否正在售卖
        bool isReport; // 是否正在处理举报侵权
        bool validate;*/
    function registeredProperty(
    string memory propertyName, 
    string memory describe, 
    string memory document, 
    address userId)
    public 
    propertyNotExist(propertyId) 
    userExist(userId) 
    isDeployer
    returns (bool)
    {   
        
        User storage user = userMapping[userId];
        
        Property memory property = Property(propertyId, propertyName, describe, document, block.timestamp, false, false ,true);
        
        // 产权id 与 实体映射
        propertyMapping[propertyId] = property;
        // 与用户地址绑定
        propertyIdToUserAddressMapping[propertyId] = userId;
        // 添加到存储全部产权id的数组
        PropertyIds.push(propertyId);
        
        // 添加到用户所有产权数组里
        user.propertys.push(propertyId);
        // 提交事件
        emit RegisteredProperty(propertyId, propertyName, describe, document, property.authenticationTime);
        // 产权id自增
        propertyId++;
        return true;
    }
    
    // 产权注销
    function cancellation(uint256 propertyId, address userId)
    public
    propertyExist(propertyId) 
    userExist(userId)
    myOwner(userId, propertyId)
    isDeployer
    returns (bool)
    {
        User storage user = userMapping[userId];
        
        // 删除用户该产权
        for (uint256 i = 0; i < user.propertys.length; i++) {
            if(user.propertys[i] == propertyId) {
                user.propertys[i] = user.propertys[user.propertys.length - 1];
                // 删除最后一个元素
                user.propertys.pop();
                break;
            }
        }
            
        // 在存储全部产权的数组里删除该产权
        for (uint256 i = 0; i < PropertyIds.length; i++) {
            if(PropertyIds[i] == propertyId) {
                PropertyIds[i] = PropertyIds[PropertyIds.length - 1];
                // 删除最后一个元素
                PropertyIds.pop();
                break;
            }
        }
        
        // 删除相关映射
        delete propertyMapping[propertyId];
        delete propertyIdToUserAddressMapping[propertyId];
        return true;
    }
    
    
    // 用户查询自己所有产权
    function getOwnerPropertys(address userId) 
    public
    view
    userExist(userId)
    returns(
        uint256[] memory ids,
        string[] memory propertyNameList,
        string[] memory describeList,
        string[] memory documentList,
        uint256[] memory authenticationTimeList
        )
    {
        User storage user = userMapping[userId];
      
        if (user.propertys.length > 0) {
        ids = user.propertys;
         // 初始化返回数组
        propertyNameList = new string[](user.propertys.length);
        describeList = new string[](user.propertys.length);
        documentList = new string[](user.propertys.length);
        authenticationTimeList = new uint256[](user.propertys.length);
        
        for (uint256 i = 0; i < user.propertys.length; i++) {
            Property storage p = propertyMapping[user.propertys[i]];
            propertyNameList[i] = p.propertyName;
            describeList[i] = p.describe;
            documentList[i] = p.document;
            authenticationTimeList[i] = p.authenticationTime;
        }
      }
      
    }
    
    
    
    // 根据产权id查询对应用户地址
    function getPropertysBelongToUser(uint256 propertyId)
    public
    view
    propertyExist(propertyId)
    returns (address)
    {
        address userId = propertyIdToUserAddressMapping[propertyId];
        return userId;
    }
    
    
    // 法院查询所有产权
    function getAllProperty(address courtId) 
    public 
    view 
    isCourt(courtId)
    returns(
        uint256[] memory ids,
        string[] memory propertyNameList,
        string[] memory describeList,
        string[] memory documentList,
        uint256[] memory authenticationTimeList
        )
    {
        if (PropertyIds.length > 0) {
            
         ids = PropertyIds;
         propertyNameList = new string[](PropertyIds.length);
         describeList = new string[](PropertyIds.length);
         documentList = new string[](PropertyIds.length);
         authenticationTimeList = new uint256[](PropertyIds.length);
        
    
         for (uint256 i = 0; i < PropertyIds.length; i++) {
            Property storage p = propertyMapping[PropertyIds[i]];
            propertyNameList[i] = p.propertyName;
            describeList[i] = p.describe;
            documentList[i] = p.document;
            authenticationTimeList[i] = p.authenticationTime;
        }
      }
      
    } 
    
    
    // 查询所有法院
    function getAllCourt() 
    public 
    view 
    returns(address[] memory ids, string[] memory courtNameList) 
    {
        if (CourtIds.length > 0) {
          ids = CourtIds;
          courtNameList = new string[](CourtIds.length);
          for (uint256 i = 0; i < CourtIds.length; i++) {
              Court storage c = courtMapping[CourtIds[i]];
              courtNameList[i] = c.courtName;
          }
        }
          
    }
    
    
    // 根据id查询法院详情
    function getCourtById(address courtId) 
    public
    view
    courtExist(courtId)
    returns(Court memory)
    {
        Court storage court = courtMapping[courtId];
        return court;
    }
    
    
    // 根据用户id查询详情
    function getUserById(address userId) 
    public
    view
    userExist(userId)
    returns(User memory)
    {
        User storage user = userMapping[userId];
        return user;
    }
    
}
```
### 这里的功能我们如何结合链上链下呢， 比如用户的注册,法院的注册 还有产权的注册 我们都应该在后端进行校验认证之后才能将其地址注册到合约中去，就比如用户需要提供身份证 手机号等利用腾讯云的服务进行认证，产权注册需要提交版权登记的相关材料在后台进行审核，法院就更特殊了，需要提交相关证明材料，在审核通过和监管部门的确认之后才能将其注册到合约中。我们的地址生成可以用到fisco提供的java-Sdk去生成(下面会放示例代码)。我们用户的产权注册之后，在合约里会存储其信息，我们可以调用合约方法去查询，但我们不能一直依赖于合约，就像我开头说到的，区块链应用开发应该分为链上和链下，一些数据我们可以存到数据库中，关键的信息上链即可。在我们后端可以利用fisco提供的java-Sdk获取到合约调用后的事件，然后将其事件解析后把数据存到数据库里，这样查询的时候就不必再调用合约了，也减轻了我们合约的负担，本次task挑战中我也自定义写了一篇如何用java-Sdk去解析合约事件的文章https://github.com/WeBankBlockchain/WeBASE-Doc/pull/466 ，大家有兴趣可以看看。

# 利用fisco提供的java-Sdk随机生成地址
```PowerShell
// 随机生成地址
    public String generateAddress() {
        // 创建非国密类型的CryptoSuite
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        // 随机生成非国密公私钥对
        CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair();
        // 获取账户地址
        String accountAddress = cryptoKeyPair.getAddress();
        return accountAddress;
    }
```

# 3, AppraisalSale.sol
本合约用于用户的产权赠送转移，相关信息查询。此合约继承了AppraisalBasic.sol 我在其也都给好了注释
```Solidity
pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;
import "./AppraisalBasic.sol";

contract AppraisalSale is AppraisalBasic{
    
     // 产权在售卖行
     modifier isSale(uint256 id) {
        require(propertyMapping[id].isSell, "Property is does Sell");
        _;
    }
    
    // 产权不在售卖行
     modifier isNotSale(uint256 id) {
        require(!propertyMapping[id].isSell, "Property is Sell");
        _;
    }
    
    // 产权不在处理举报侵权
     modifier isNotReport(uint256 id) {
        require(!propertyMapping[id].isReport, "Property is Report");
        _;
    }
    
    
    // 提交售卖事件
    event submitToSellEvent (uint256 id,
        address userId, // 所属人地址
        string propertyName, //  产权名称
        string describe,	// 描述
        uint256 authenticationTime,	// 鉴权时间戳
        uint256 sellTime, // 上架时间
        uint256 price // 价格
        );
    
    // 购买事件
    event buyPropertyEvent(uint256 id,  // 用户id
         address buyerAddress,
         address sellerAddress,
         uint256 price,
         uint256 buyTime
        );
    
    

    
    // 用户将产权提交到售卖行进行售卖
    /* uint256 id;  // 产权id
        address userId; // 所属人地址
        string propertyName; //  产权名称
        string describe;	// 描述
        uint256 authenticationTime;	// 鉴权时间戳
        uint256 sellTime; // 上架时间
        uint256 price; // 价格*/
    function submitToSell(uint256 id, uint256 price, address userId)
    public
    userExist(userId)
    myOwner(userId, id) 
    isNotSale(id)
    isNotReport(id)
    isDeployer
    returns (bool)
    {   
        // 设置为正在售卖
        propertyMapping[id].isSell = true;
        Property storage p = propertyMapping[id];
        
        // 售卖信息结构体
        PropertySell memory pSall = PropertySell
        (id, userId, p.propertyName, p.describe, 
        p.authenticationTime, block.timestamp, price);
        
        propertySellMapping[id] = pSall;
        
        // 将产权id存储到售卖id数组里
        PropertySellIds.push(id);
        
        // 提交事件
        emit submitToSellEvent(id, userId, p.propertyName, p.describe, p.authenticationTime, pSall.sellTime, price);
        return true;
    }

    // 用户取消售卖
    function cancelSell(uint256 id, address userId) 
    public
    userExist(userId)
    myOwner(userId, id)
    isSale(id)
    isDeployer
    returns (bool)
    {
        propertyMapping[id].isSell = false;
        
        delete propertySellMapping[id];
        
        for (uint256 i = 0; i < PropertySellIds.length; i++) {
            if (PropertySellIds[i] == id) {
                PropertySellIds[i] = PropertySellIds[PropertySellIds.length - 1];
                PropertySellIds.pop();
            }
        }
        
        return true;
    }
    
    
    // 用户购买
    function buyProperty(uint256 id, address buyerAddress, address sellerAddress) 
    public
    userExist(buyerAddress)
    isSale(id)
    myOwner(sellerAddress, id)
    isDeployer
    returns (bool)
    {
       require(buyerAddress != sellerAddress, "Can't buy your own Property");
        
       PropertySell storage pSell = propertySellMapping[id];
       User storage buyer = userMapping[buyerAddress];
       
       User storage seller = userMapping[sellerAddress];
       
       require(buyer.amount >= pSell.price, "balance is not enough");
       
       // 交易转账
       buyer.amount -= pSell.price;
       seller.amount += pSell.price;
       
       // 版权转移
       buyer.propertys.push(pSell.id);
       
       // 所属人转移
       propertyIdToUserAddressMapping[pSell.id] = buyerAddress;
       
       // 提交事件
       emit buyPropertyEvent(id, buyerAddress, sellerAddress, pSell.price, block.timestamp);
       
       for (uint256 i = 0; i < seller.propertys.length; i++) {
           if (seller.propertys[i] == id) {
               seller.propertys[i] = seller.propertys[seller.propertys.length - 1];
               seller.propertys.pop();
           }
       }
       
       // 设置状态 删除映射
       propertyMapping[id].isSell = false;
       delete propertySellMapping[id];
       
       // 删除存储在售卖数组里的id
       for (uint256 i = 0; i < PropertySellIds.length; i++) {
            if (PropertySellIds[i] == id) {
                PropertySellIds[i] = PropertySellIds[PropertySellIds.length - 1];
                PropertySellIds.pop();
            }
        }
        
        return true;
    }
    
    // 查询正在售卖的所有
    function getAllSell() 
    public
    view 
    returns 
    (   uint256[] memory ids,  // 产权id
        address[] memory userIdList, // 所属人地址
        string[] memory propertyNameList, //  产权名称
        string[] memory describeList,	// 描述
        uint256[] memory authenticationTimeList,	// 鉴权时间戳
        uint256[] memory sellTimeList, // 上架时间
        uint256[] memory priceList // 价格)
    )
    {
        if (PropertySellIds.length > 0) {
            ids = PropertySellIds;
            userIdList = new address[](PropertySellIds.length);
            propertyNameList = new string[](PropertySellIds.length);
            describeList = new string[](PropertySellIds.length);
            authenticationTimeList = new uint256[](PropertySellIds.length);
            sellTimeList = new uint256[](PropertySellIds.length);
            priceList = new uint256[](PropertySellIds.length);
            

            for (uint256 i = 0; i < PropertySellIds.length; i++) {
                PropertySell storage p = propertySellMapping[PropertySellIds[i]];
                userIdList[i] = p.userId;
                propertyNameList[i] = p.propertyName;
                describeList[i] = p.describe;
                authenticationTimeList[i] = p.authenticationTime;
                sellTimeList[i] = p.sellTime;
                priceList[i] = p.price;
            }
        }
        
    }
    
    // 查询自己正在发售的
    function getOwnerSell(address userId) 
    public
    view
    userExist(userId)
    returns
    (   uint256[] memory ids,  // 产权id
        address[] memory userIdList, // 所属人地址
        string[] memory propertyNameList, //  产权名称
        string[] memory describeList,	// 描述
        uint256[] memory authenticationTimeList,	// 鉴权时间戳
        uint256[] memory sellTimeList, // 上架时间
        uint256[] memory priceList // 价格
    )
    {
        User storage user = userMapping[userId];
        
        uint256 n = 0;
        ids = new uint256[](user.propertys.length);
        
        for (uint256 i = 0; i < user.propertys.length; i++) {
            if (propertyMapping[user.propertys[i]].isSell) {
                ids[n] = user.propertys[i];
                n++;
            }
        }
        
        if (ids.length > 0) {
            userIdList = new address[](ids.length);
            propertyNameList = new string[](ids.length);
            describeList = new string[](ids.length);
            authenticationTimeList = new uint256[](ids.length);
            sellTimeList = new uint256[](ids.length);
            priceList = new uint256[](ids.length);
            
             for (uint256 i = 0; i < ids.length; i++) {
                PropertySell storage p = propertySellMapping[ids[i]];
                userIdList[i] = p.userId;
                propertyNameList[i] = p.propertyName;
                describeList[i] = p.describe;
                authenticationTimeList[i] = p.authenticationTime;
                sellTimeList[i] = p.sellTime;
                priceList[i] = p.price;
            }
        }
    }
    
    // 转赠
    function gift(address formAddress, address toAddress, uint256 id) 
    public
    userExist(formAddress)
    userExist(toAddress)
    myOwner(formAddress, id)
    isNotSale(id)
    isNotReport(id)
    isDeployer
    {
        User storage formUser = userMapping[formAddress];
        User storage toUser = userMapping[toAddress];
        
        // 删除赠送人存储的产权id
        for (uint256 i = 0; i < formUser.propertys.length; i++) {
            if (formUser.propertys[i] == id) {
                formUser.propertys[i] = formUser.propertys[formUser.propertys.length - 1];
                formUser.propertys.pop();
            }
        }
        
        // 接收人新增产权id
        toUser.propertys.push(id);
        
        // 映射更换
        propertyIdToUserAddressMapping[id] = toUser.id;
    }
    
}

```
### 添加售卖转赠也是为了丰富内容，在实际开发中，售卖或者转赠都涉及到实际金额(交易金额，税务金额), 在此合约我只是模拟了交易，真正的交易(税务扣除)还是要在后端进行，交易成功后在合约中再进行产权的转移。


# AppraisalReport.sol
此合约继承了AppraisalSale.sol 最后部署的时候 部署此合约就可以了
合约包含了 举报侵权，法院受理，查询等功能
```Solidity
// SPDX-License-Identifier: SimPL-2.0
pragma solidity >=0.5.0;
pragma experimental ABIEncoderV2;
// 引入合约

import "./AppraisalSale.sol";
contract AppraisalReport is AppraisalSale{
    
    
     // 产权正在受理/待受理侵权中
     modifier isReport(uint256 id) {
        require(propertyMapping[id].isReport, "Property is does report");
        _;
    }
     
    //  // 产权未正在受理/待受理侵权中
    //  modifier isNotReport1(uint256 id) {
    //     require(!propertyMapping[id].isReport, "Property is report");
    //     _;
    // }
    
    // 产权待受理
     modifier waitReport(uint256 id) {
        require(reportInfoMapping[id].status == ReportStatus.WAIT_STATUS, "Property does WAIT_STATUS");
        _;
    }
    
    // 产权正在受理
     modifier nowReport(uint256 id) {
        require(reportInfoMapping[id].status == ReportStatus.NOW_STATUS, "Property does NOW_STATUS");
        _;
    }
    
    // 法院受理侵权时校验
     modifier courtAcceptanceCheck(uint256 id, address courtId) {
        require(propertyIdToCourtAddressMapping[id] == courtId, "No permission to accept");
        _; 

    }
     
    
    // 举报事件
    event reportEvent (uint256 id,
        address reporterAddress, // 举报人地址
        string telephone, // 举报人电话/ 联系方式
        string email, // 举报人邮件
        string information, // 举报材料图片地址 "https://xxxx, https://xxxx"
        uint256 reportTime // 举报时间*/
        );
    
    // 法院受理事件
    event acceptanceEvent (
        uint256 id, // 受理被侵权产权id
        address courtId, // 法院地址
        address userId, // 举报人地址(产权所属人)
        uint256 acceptanceTime // 受理时间
        );
    
    // 结案事件
    event endEvent (
        uint256 id, // 受理被侵权产权id
        address courtId, // 法院地址
        address userId, // 举报人地址(产权所属人)
        uint256 acceptanceTime, // 结案时间
        string result // 结案书 (图片地址 https://xxxx)
        );
    
    
    // 向指定法院举报侵权
    /* uint256 id; // 产权id
        address reporterAddress; // 举报人地址
        string telephone; // 举报人电话/ 联系方式
        string email; // 举报人邮件
        string information; // 举报材料图片地址 "https://xxxx, https://xxxx"
        uint256 reportTime; // 举报时间*/
    function report(address userId, address courtId ,uint256 id, 
    string memory telephone, string memory email, string memory information) 
    public
    userExist(userId)
    courtExist(courtId)
    myOwner(userId, id)
    isNotSale(id)
    isNotReport(id)
    isDeployer
    returns (bool)
    {
        // 侵权信息结构体
        ReportInfo memory reportInfo = ReportInfo(id, userId, telephone, email, information, block.timestamp, ReportStatus.WAIT_STATUS);
        
        // 产权id => 举报信息
        reportInfoMapping[id] = reportInfo;
        
        // 产权id => 法院地址
        propertyIdToCourtAddressMapping[id] = courtId;
        
        // 添加到法院待受理侵权id数组中
        Court storage court = courtMapping[courtId];
        court.reportPropertys.push(id);
        
        // 产权信息状态更改
        propertyMapping[id].isReport = true;
        
        // 提交事件
        emit reportEvent(id, userId, telephone, email, information, reportInfo.reportTime);
        return true;
    }
    
    // 法院查询已提交的举报
    /* uint256 id;  // 产权id
        string propertyName; 	//  产权名称
        string describe;	// 描述
        string document;  //  证明文件图片地址 "https://xxxx,https://xxxx"
        uint256 authenticationTime;	// 鉴权时间戳
        address reporterAddress; // 举报人地址
        string telephone; // 举报人电话/ 联系方式
        string email; // 举报人邮件
        string information; // 举报材料图片地址 "https://xxxx, https://xxxx"
        uint256 reportTime; // 举报时间*/
        
    function getReportByCourtAddress(address courtId) 
    public
    view
    courtExist(courtId)
    returns
    (
        uint256[] memory ids,  // 产权id
        address[] memory reporterAddressList, // 举报人地址
        string[] memory telephoneList, // 举报人电话/ 联系方式
        string[] memory emailList, // 举报人邮件
        string[] memory informationList, // 举报材料图片地址 "https://xxxx, https://xxxx"
        uint256[] memory reportTimeList, // 举报时间*/*/
        ReportStatus[] memory statusList // 状态
    )
    {
        // 根据courtMapping拿到court 获取其被侵权id数组
        Court storage court = courtMapping[courtId];
  
        if (court.reportPropertys.length > 0) {
            ids = court.reportPropertys;
            // 初始化返回值
            reporterAddressList = new address[](ids.length);
            telephoneList = new string[](ids.length);
            emailList = new string[](ids.length);
            informationList = new string[](ids.length);
            reportTimeList = new uint256[](ids.length);
            statusList = new ReportStatus[](ids.length);
            for (uint256 i = 0; i< ids.length; i++) {
                // 通过mapping获取实体封装对应的值
                (   
                   

                    // 举报详情
                    reporterAddressList[i],
                    telephoneList[i],
                    emailList[i],
                    informationList[i],
                    reportTimeList[i],
                    statusList[i]
                    ) = _getReportInfo(ids[i]);
            }
        }
    }
    
    
    // 法院受理
    function acceptance(uint256 id, address courtId) 
    public
    courtExist(courtId)
    isReport(id)
    courtAcceptanceCheck(id, courtId)
    waitReport(id)
    isDeployer
    returns (bool)
    {
        // 更改状态
        reportInfoMapping[id].status = ReportStatus.NOW_STATUS;
        
        // 提交事件
        emit acceptanceEvent(id, courtId, propertyIdToUserAddressMapping[id], block.timestamp);
        return true;
    }
    
    
    
    // 法院结案
    function end(uint256 id, address courtId, string memory result)
    public
    courtExist(courtId)
    isReport(id)
    courtAcceptanceCheck(id, courtId)
    nowReport(id)
    isDeployer
    returns (bool)
     {
        // 更改状态
        reportInfoMapping[id].status = ReportStatus.END_STATUS;
        
        // 提交事件
        emit endEvent(id, courtId, propertyIdToUserAddressMapping[id], block.timestamp, result);

        
        // 删除法院待受理侵权id数组中的 id
        Court storage court = courtMapping[courtId];
        for (uint256 i = 0; i < court.reportPropertys.length; i++) {
            if (court.reportPropertys[i] == id) {
                court.reportPropertys[i] == court.reportPropertys[court.reportPropertys.length - 1];
                court.reportPropertys.pop();
            }
        }
        // 产权信息状态更改
        propertyMapping[id].isReport = false;
        
        // 删除相关映射
        delete reportInfoMapping[id];
        delete propertyIdToCourtAddressMapping[id];
        return true;
    }
    
    // 用户手动取消举报
     function cancelReport(uint256 id, address userId)
     public
     userExist(userId)
     myOwner(userId, id)
     waitReport(id)
     isDeployer
     returns (bool)
     {
         
        // 删除法院待受理侵权id数组中的 id
        Court storage court = courtMapping[propertyIdToCourtAddressMapping[id]];
        for (uint256 i = 0; i < court.reportPropertys.length; i++) {
            if (court.reportPropertys[i] == id) {
                court.reportPropertys[i] == court.reportPropertys[court.reportPropertys.length - 1];
                court.reportPropertys.pop();
            }
        }
        // 产权信息状态更改
        propertyMapping[id].isReport = false;
        
        // 删除相关映射
        delete reportInfoMapping[id];
        delete propertyIdToCourtAddressMapping[id];
        return true; 
     }
     
     
     // 用户查询自己举报的信息
     function getReportByUserId(address userId)
     public
     view
     userExist(userId)
     returns
      (
        uint256[] memory ids,  // 产权id
        address[] memory reporterAddressList, // 举报人地址
        string[] memory telephoneList, // 举报人电话/ 联系方式
        string[] memory emailList, // 举报人邮件
        string[] memory informationList, // 举报材料图片地址 "https://xxxx, https://xxxx"
        uint256[] memory reportTimeList, // 举报时间*/*/
        ReportStatus[] memory statusList // 状态
    )
    {
        User storage user = userMapping[userId];
        
        uint256 n = 0;
        if (user.propertys.length > 0) {
            ids = new uint256[](user.propertys.length);
            for (uint256 i = 0; i < user.propertys.length; i++) {
                if (propertyMapping[user.propertys[i]].isReport) {
                    ids[n] = user.propertys[i];
                    n++;
                }
            }
            
            if (ids.length > 0) {
                reporterAddressList = new address[](ids.length);
                telephoneList = new string[](ids.length);
                emailList = new string[](ids.length);
                informationList = new string[](ids.length);
                reportTimeList = new uint256[](ids.length);
                statusList = new ReportStatus[](ids.length);
                for (uint256 i = 0; i< ids.length; i++) {
                    // 通过mapping获取实体封装对应的值
                    (  
                        // 举报详情
                        reporterAddressList[i],
                        telephoneList[i],
                        emailList[i],
                        informationList[i],
                        reportTimeList[i],
                        statusList[i]
                     ) = _getReportInfo(ids[i]);
                }
            }
        }
    }
    
    function _getReportInfo(uint256 id)
    private  
    view 
    returns
    (
        address reporterAddress,
        string memory telephone,
        string memory email,
        string memory information,
        uint256 reportTime,
        ReportStatus status
    )
    {
        ReportInfo storage r = reportInfoMapping[id];
        // 举报详情
        reporterAddress = r.reporterAddress; 
        telephone = r.telephone; 
        email = r.email; 
        information = r.information; 
        reportTime = r.reportTime; 
        status = r.status;
    }

}
```
### 此合约中的举报侵权, 是根据用于已上链的产权去举报的，提交到法院后，法院可以查询其链上信息进行快速确权，从而提高受理案件的速度。在案件办理的个个阶段都会有事件提交，可以通过解析事件获取数据并存储。在区块链及监管部门的加持下，更快的帮助创作者维权。

# 二， 合约测试
# 1, 首先我们先注册两个用户和一个法院
![image](https://user-images.githubusercontent.com/103564714/188062930-72fdcbe2-3dd0-4a63-9965-d8be3d439b32.png)
![image](https://user-images.githubusercontent.com/103564714/188063011-bd791a28-adf5-4e60-8195-1cacd85ef6bb.png)
![image](https://user-images.githubusercontent.com/103564714/188063071-2b68e248-6d49-4cb7-8658-71e5af983103.png)

### 可以看到注册都注册成功了，这里为了方便测试 我在合约里给两个用户的初始值余额都是500
![image](https://user-images.githubusercontent.com/103564714/188063034-6e772e96-be2a-4653-ba33-8983ccbe4e58.png)

# 2, 注册产权查询
![image](https://user-images.githubusercontent.com/103564714/188063548-9caa1317-327f-45a2-a04d-f8b879f430fc.png)
![image](https://user-images.githubusercontent.com/103564714/188063557-06208070-35fc-41b6-b8e1-bff3fbd1fdf0.png)<br>
### 可以看到已经注册成功。
![image](https://user-images.githubusercontent.com/103564714/188063598-00d9f7c0-f74d-451f-bf8b-09405ab6800a.png)<br>
### 然后我们可以去查询一下自己的产权<br>
![image](https://user-images.githubusercontent.com/103564714/188063652-0bd17ce6-c4e7-430a-8208-4292fd043f69.png)<br>
### 可以看到查询是没有问题的<br>
![image](https://user-images.githubusercontent.com/103564714/188063663-793ac0ba-f9a3-4b4d-97df-0750c9dd7900.png)<br>

# 3, 产权售卖
### 我们将用户Const的产权提交售卖<br>
![image](https://user-images.githubusercontent.com/103564714/188063963-84ef410d-7e1a-451c-9f00-cdd1368ce155.png)<br>
### 成功后会触发提交事件<br>
![image](https://user-images.githubusercontent.com/103564714/188064055-96bc02d2-911b-4123-9810-db04aceed246.png)<br>

### 调用查询全部售卖方法测试，可以看到正在售卖的产权信息<br>
![image](https://user-images.githubusercontent.com/103564714/188064161-e16a9c33-d768-494a-93ae-225c845ea907.png)<br>
![image](https://user-images.githubusercontent.com/103564714/188064352-63c611d0-8a70-4d97-956d-db0a34423a26.png)

# 4, 产权购买
### 这里测试用户Fb购买用户Const的产权<br>
![image](https://user-images.githubusercontent.com/103564714/188064511-eab9bb74-bb0f-464d-9669-503e2a2c57d6.png)<br>
### 成功后触发提交事件<br>
![image](https://user-images.githubusercontent.com/103564714/188064528-39415ecd-e24c-4059-9c35-0450491484a9.png)<br>
### 然后我们可以查询一下二者拥有的产权和余额<br>
### Fb的产权<br>
![image](https://user-images.githubusercontent.com/103564714/188064683-039cefe9-4d40-4e8d-b38d-4d6c346c355d.png)<br>
### Const的产权<br>
![image](https://user-images.githubusercontent.com/103564714/188064713-1945b702-a089-4d5d-8c62-e4ed10e475f6.png)<br>
### 二者的余额情况
![image](https://user-images.githubusercontent.com/103564714/188064740-3006c30e-48f5-4f58-a7f3-8922f0a3f1d1.png)
![image](https://user-images.githubusercontent.com/103564714/188064758-547aa3ea-5dc4-41a6-b416-21dbd7c323ea.png)
### 转赠方法大家可以自行测试一下

# 5, 投诉与受理
### 这里测试Fb向指定法院举报自己id为0的产权被侵权<br>
![image](https://user-images.githubusercontent.com/103564714/188065297-26390d69-4eb4-4f13-a80c-85af9fe43d61.png)<br>
### 举报成功后会触发举报相应的事件<br>
![image](https://user-images.githubusercontent.com/103564714/188131859-6b7983fd-ed0c-4b32-a078-6be38cdb8147.png)<br>
### 指定法院可以查看到相关详情 并受理案件<br>
![image](https://user-images.githubusercontent.com/103564714/188132013-e02a99da-aab3-4321-8e17-f5f48c3e832b.png)<br>
### 受理成功后会触发受理相应的事件<br>
![image](https://user-images.githubusercontent.com/103564714/188132246-b9ca8ad1-8caf-42a9-aa64-944bd756825d.png)<br>
### 最后的结案 法院会提交结案书 也会触发结案相应的事件<br>
![image](https://user-images.githubusercontent.com/103564714/188133744-c82f45a3-a6fc-4a21-a5fe-01d358863af0.png)<br>

![image](https://user-images.githubusercontent.com/103564714/188132326-d51312b1-00c8-4292-afe8-915cefb9c3e3.png)<br>

# 三， 总结
### 区块链技术其实并不能保证链下数据的真实性可靠性，只能保证信息一旦上链，就是全网一致并且难以篡改的。在用户产权注册的时候，我们必须在有关部门的监管下对其进行审核认证，才能保证链下和链上数据一致可靠，我也相信在未来的区块链发展中，会有更多可靠可信的链和可靠的解决方案出现在我们的生活中，如FISCO-BCOS的分布式身份解决方案WeIdentity, 其衍生出来的区块链应用则能够方便，改善我们的生活。

















