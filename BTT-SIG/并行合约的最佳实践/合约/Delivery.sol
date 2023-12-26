pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;
// 派送合约
import "./StorageData.sol";
import "./ParallelContract.sol";
contract Delivery  is StorageData,ParallelContract {
    
    // 判断当前是否为快递员
    modifier IsCourier(address courierAddress) {
        require(UserMap[courierAddress].role == 2,"当前用户不是快递员");
        _;
    }
    
    // 用户的注册业务
    function userRegister(address _userAddress) public {
        UserMap[_userAddress].u_address = _userAddress;
        UserMap[_userAddress].role = 1;
    }
    
    // 快递员注册业务
    function courierRegister(address _courierAddress) public {
        UserMap[_courierAddress].u_address = _courierAddress;
        UserMap[_courierAddress].role = 2;
    }
    
    // 签收生成签收记录
    function courierToUserSign(address _from,address _to,uint256 _isSign) public IsCourier(_from) {
        // 生成签收完成的订单记录
        RecordCount++;
        SignRecord storage _signRecord = RecordMap[RecordCount];
        _signRecord.record_id = RecordCount;
        _signRecord.c_address = _from;
        _signRecord.u_address = _to;
        _signRecord.sign_time = block.timestamp;
        
        SignRecordList.push(RecordCount);
        
        if (_isSign == 0){
            // 设置该用户已签收
            UserIsSignStatus[_to] = true;
        }else {
            // 设置用户拒绝签收
            UserIsSignStatus[_to] = false;
            emit Refusal(_to,block.timestamp);
        }
        emit Signed(_to,_isSign);
    }
    
    // 对物流进行售后
    function afterSales(address _from,uint256 _isClaim) public {
        require(UserIsClaimStatus[_from] == false,"当前用户已经申请售后");
        if (_isClaim == 0){
            UserIsClaimStatus[_from] = true;
        }else {
            UserIsClaimStatus[_from] = false;
        }
    }
    
    // 查询所有的签收记录
    function selectSignRecordList() public returns(SignRecord[] memory) {
        SignRecord[] memory recordList = new SignRecord[](SignRecordList.length);
        for (uint i = 0; i < SignRecordList.length; i++){
            recordList[i] = RecordMap[SignRecordList[i]];
        }
        return recordList;
    }
    
    // 声明函数 enableParallel，用于启用并行计算功能
    function enableParallel() public {
        registerParallelFunction("courierToUserSign(address,address,uint256)",2);
        registerParallelFunction("afterSales(address,uint256)",1);
    }
    
    // 声明函数 disableParallel，用于禁用并行计算功能
    function disableParallel() public {
        unregisterParallelFunction("courierToUserSign(address,address,bool)");
        unregisterParallelFunction("afterSales(address,uint256)");
    }
}