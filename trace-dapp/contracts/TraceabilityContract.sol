pragma solidity ^0.6.10;
pragma experimental ABIEncoderV2;

/**
 * @title TraceabilityContract
 * @notice 农产品溯源系统智能合约
 * @dev   用于记录和查询农产品从种植到销售的全链路信息
 *        兼容 FISCO-BCOS 2.x / WeBASE 平台
 */
contract TraceabilityContract {

    // ---------- 数据结构 ----------

    /// @notice 单一溯源记录
    struct TraceRecord {
        string recordNo;       // 记录编号（如 PL20260524001）
        string dataHash;       // 链下数据 SHA256 哈希
        string operator_;      // 操作人用户名
        uint256 timestamp;     // 上链时间戳
    }

    /// @notice 产品溯源聚合数据
    struct ProductTrace {
        bool      exists;                 // 产品是否存在
        string    productNo;              // 产品编号
        uint256   recordCount;            // 溯源记录总数
        TraceRecord planting;             // 种植记录
        bool      plantingExists;
        TraceRecord processing;           // 加工记录
        bool      processingExists;
        TraceRecord testing;              // 检测记录
        bool      testingExists;
        TraceRecord logistics;            // 物流记录
        bool      logisticsExists;
    }

    // ---------- 状态变量 ----------

    mapping(string => ProductTrace) private productTraces;  // productNo -> 产品溯源数据
    mapping(string => uint256)      private scanCounts;     // productNo -> 扫码次数
    string[]                         private allProductNos; // 所有产品编号列表

    // ---------- 事件 ----------

    event TraceRecorded(string indexed productNo, string recordType, string recordNo, uint256 timestamp);
    event ProductCreated(string indexed productNo, uint256 timestamp);
    event Scanned(string indexed productNo, uint256 totalScans);

    // ---------- 修饰器 ----------

    modifier productExists(string memory _productNo) {
        require(productTraces[_productNo].exists, "Product does not exist");
        _;
    }

    // ---------- 写操作 ----------

    /// @notice 创建新产品溯源入口
    /// @param _productNo 产品编号
    function createProduct(string memory _productNo) public returns (bool) {
        require(!productTraces[_productNo].exists, "Product already exists");

        ProductTrace storage trace = productTraces[_productNo];
        trace.exists = true;
        trace.productNo = _productNo;
        trace.recordCount = 0;

        allProductNos.push(_productNo);

        emit ProductCreated(_productNo, block.timestamp);
        return true;
    }

    /// @notice 记录种植信息
    function recordPlanting(
        string memory _plantingNo,
        string memory _productNo,
        string memory _dataHash,
        string memory _operator
    ) public productExists(_productNo) returns (bool) {
        ProductTrace storage trace = productTraces[_productNo];
        require(!trace.plantingExists, "Planting record already exists");

        trace.planting = TraceRecord(_plantingNo, _dataHash, _operator, block.timestamp);
        trace.plantingExists = true;
        trace.recordCount++;

        emit TraceRecorded(_productNo, "planting", _plantingNo, block.timestamp);
        return true;
    }

    /// @notice 记录加工信息
    function recordProcessing(
        string memory _processingNo,
        string memory _productNo,
        string memory _dataHash,
        string memory _operator
    ) public productExists(_productNo) returns (bool) {
        ProductTrace storage trace = productTraces[_productNo];
        require(!trace.processingExists, "Processing record already exists");

        trace.processing = TraceRecord(_processingNo, _dataHash, _operator, block.timestamp);
        trace.processingExists = true;
        trace.recordCount++;

        emit TraceRecorded(_productNo, "processing", _processingNo, block.timestamp);
        return true;
    }

    /// @notice 记录检测信息
    function recordTesting(
        string memory _testingNo,
        string memory _productNo,
        string memory _dataHash,
        string memory _operator
    ) public productExists(_productNo) returns (bool) {
        ProductTrace storage trace = productTraces[_productNo];
        require(!trace.testingExists, "Testing record already exists");

        trace.testing = TraceRecord(_testingNo, _dataHash, _operator, block.timestamp);
        trace.testingExists = true;
        trace.recordCount++;

        emit TraceRecorded(_productNo, "testing", _testingNo, block.timestamp);
        return true;
    }

    /// @notice 记录物流信息
    function recordLogistics(
        string memory _logisticsNo,
        string memory _productNo,
        string memory _dataHash,
        string memory _operator
    ) public productExists(_productNo) returns (bool) {
        ProductTrace storage trace = productTraces[_productNo];
        require(!trace.logisticsExists, "Logistics record already exists");

        trace.logistics = TraceRecord(_logisticsNo, _dataHash, _operator, block.timestamp);
        trace.logisticsExists = true;
        trace.recordCount++;

        emit TraceRecorded(_productNo, "logistics", _logisticsNo, block.timestamp);
        return true;
    }

    /// @notice 扫码记录
    function recordScan(string memory _productNo) public productExists(_productNo) returns (uint256) {
        scanCounts[_productNo]++;
        uint256 total = scanCounts[_productNo];
        emit Scanned(_productNo, total);
        return total;
    }

    // ---------- 读操作 ----------

    /// @notice 获取产品的完整溯源数据（返回 JSON 字符串）
    /// @param _productNo 产品编号
    /// @return JSON string 包含产品所有溯源记录
    function getTraceRecord(string memory _productNo) public view returns (string memory) {
        ProductTrace storage trace = productTraces[_productNo];
        if (!trace.exists) {
            return "";
        }

        return string(abi.encodePacked(
            '{"productNo":"', _productNo, '",',
            '"recordCount":"', uint2str(trace.recordCount), '",',
            '"planting":', traceRecordToJson(trace.planting, trace.plantingExists, '"planting"'), ',',
            '"processing":', traceRecordToJson(trace.processing, trace.processingExists, '"processing"'), ',',
            '"testing":', traceRecordToJson(trace.testing, trace.testingExists, '"testing"'), ',',
            '"logistics":', traceRecordToJson(trace.logistics, trace.logisticsExists, '"logistics"'), ',',
            '"scanCount":"', uint2str(scanCounts[_productNo]), '"}'
        ));
    }

    /// @notice 获取产品的溯源记录数量
    function getTraceCount(string memory _productNo) public view returns (uint256) {
        ProductTrace storage trace = productTraces[_productNo];
        if (!trace.exists) return 0;
        return trace.recordCount;
    }

    /// @notice 获取产品扫码次数
    function getScanCount(string memory _productNo) public view returns (uint256) {
        return scanCounts[_productNo];
    }

    /// @notice 检查产品是否存在
    function productExists_(string memory _productNo) public view returns (bool) {
        return productTraces[_productNo].exists;
    }

    /// @notice 获取所有产品编号列表
    function getAllProductNos() public view returns (string[] memory) {
        return allProductNos;
    }

    // ---------- 工具函数 ----------

    /// @dev 将 TraceRecord 转为 JSON 片段
    function traceRecordToJson(TraceRecord memory record, bool exists, string memory label)
        private pure returns (string memory)
    {
        if (!exists) {
            return string(abi.encodePacked(label, ':null'));
        }
        return string(abi.encodePacked(
            label, ':{',
            '"recordNo":"', record.recordNo, '",',
            '"dataHash":"', record.dataHash, '",',
            '"operator_":"', record.operator_, '",',
            '"timestamp":"', uint2str(record.timestamp), '"}'
        ));
    }

    /// @dev uint256 转 string
    function uint2str(uint256 _i) private pure returns (string memory) {
        if (_i == 0) return "0";
        uint256 j = _i;
        uint256 len;
        while (j != 0) {
            len++;
            j /= 10;
        }
        bytes memory bstr = new bytes(len);
        uint256 k = len;
        while (_i != 0) {
            k = k - 1;
            bstr[k] = bytes1(uint8(48 + _i % 10));
            _i /= 10;
        }
        return string(bstr);
    }
}
