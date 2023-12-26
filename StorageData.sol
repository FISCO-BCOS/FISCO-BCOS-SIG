pragma solidity ^0.4.25;
// 数据结构合约 定义公共参数
contract StorageData {
    // 快递员
    struct User{
        address u_address;  // 用户的地址
        uint    role;       // 角色
    }
    
    // 签收记录
    struct SignRecord{
        uint record_id;
        address c_address;
        address u_address;
        uint256 sign_time;
    }
    
    // 签收记录ID
    uint256 public RecordCount;
    // 签收的记录数组
    uint256[] public SignRecordList;
    
    
    // 用户签收的状态
    mapping(address => bool) public UserIsSignStatus;
    // 用户售后申请
    mapping(address => bool) public UserIsClaimStatus;
    // 用户注册
    mapping(address => User) public UserMap;
    // 生成记录
    mapping(uint256 => SignRecord) public RecordMap;
    
    
    // 用户注册的事件
    event Registered(address indexed _userAddress,uint indexed _role);
    
    
    // 用户签收的事件
    event Signed(address indexed _userAddress,uint256 indexed _isSign);
    
    
    // 用户拒收事件
    event Refusal(address indexed _userAddress,uint indexed _time);
    
}