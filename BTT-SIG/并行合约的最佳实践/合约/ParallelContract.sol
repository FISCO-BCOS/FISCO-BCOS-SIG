pragma solidity ^0.4.25;
// 预编译合约 ParallelConfigPrecompiled
contract ParallelConfigPrecompiled {
    // 注册并行函数
    // 参数1：并行函数地址
    // 参数2：并行函数名称
    // 参数3：致命区间值，用于限制并行计算区间大小
    function registerParallelFunctionInternal(address, string, uint256) public returns (int);
    
    // 注销并行函数
    // 参数1：并行函数地址
    // 参数2：并行函数名称
    function unregisterParallelFunctionInternal(address, string) public returns (int);    
}
 
// 并行合约基类
contract ParallelContract {
    // 实例化并使用 ParallelConfigPrecompiled 合约
    ParallelConfigPrecompiled precompiled = ParallelConfigPrecompiled(0x1006);
    
    // 注册并行函数，注册后可以在并行计算中使用
    // 参数1：并行函数名称
    // 参数2：致命区间值，用于限制并行计算区间大小
    function registerParallelFunction(string functionName, uint256 criticalSize) public 
    {
        precompiled.registerParallelFunctionInternal(address(this), functionName, criticalSize);
    }
    
    // 注销并行函数，在停用并行计算前建议注销所有已注册的并行函数
    // 参数1：并行函数名称
    function unregisterParallelFunction(string functionName) public
    {
        precompiled.unregisterParallelFunctionInternal(address(this), functionName);
    }
    
    // 启用并行计算功能
    function enableParallel() public;
    
    // 关闭并行计算功能
    function disableParallel() public;
}