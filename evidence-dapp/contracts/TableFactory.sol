pragma solidity ^0.4.25;

contract TableFactory {
    function openTable(string) public view returns (Table);
    function createTable(string, string, string) public returns (int256);
}

contract Table {
    function select(string, Condition) public view returns (Entries);
    function insert(string, Entry) public returns (int256);
    function update(string, Entry, Condition) public returns (int256);
    function newEntry() public view returns (Entry);
    function newCondition() public view returns (Condition);
}

contract Entry {
    function set(string, string) public;
    function set(string, int256) public;
    function getString(string) public view returns (string);
    function getInt(string) public view returns (int256);
}

contract Condition {
    function EQ(string, string) public;
    function EQ(string, int256) public;
    function GT(string, int256) public;
    function GE(string, int256) public;
    function LT(string, int256) public;
    function LE(string, int256) public;
    function limit(int256) public;
    function limit(int256, int256) public;
}

contract Entries {
    function get(int256) public view returns (Entry);
    function size() public view returns (int256);
}
