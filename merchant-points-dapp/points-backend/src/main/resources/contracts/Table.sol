pragma solidity ^0.4.25;
pragma experimental ABIEncoderV2;

contract TableFactory {
    function openTable(string memory) public view returns (Table) {}
    function createTable(string memory, string memory, string memory) public returns (int256) {}
}

contract Table {
    function select(string memory, Condition) public view returns (Entries) {}
    function insert(string memory, string[] memory) public returns (int256) {}
    function update(string memory, string[] memory, Condition) public returns (int256) {}
    function remove(string memory, Condition) public returns (int256) {}
    function newCondition() public returns (Condition) {}
}

contract Entries {
    function size() public view returns (uint256) {}
    function get(uint256) public returns (Entry) {}
}

contract Entry {
    function getString(string memory) public returns (string memory) {}
    function getInt(string memory) public returns (int256) {}
    function getAddress(string memory) public returns (address) {}
}

contract Condition {
    function EQ(string memory, string memory) public {}
    function NE(string memory, string memory) {}
    function GT(string memory, string memory) {}
    function GE(string memory, string memory) {}
    function LT(string memory, string memory) {}
    function LE(string memory, string memory) {}
    function limit(int256) {}
    function limit(int256, int256) {}
}


