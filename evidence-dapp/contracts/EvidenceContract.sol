pragma solidity ^0.4.25;

import "./TableFactory.sol";

contract EvidenceContract {
    string constant TABLE_NAME = "t_evidence_v3";
    uint256 evidenceCount;

    event EvidenceSaved(string hash, string metadata, string owner, uint256 timestamp);
    event EvidenceVerified(string hash, uint256 verifyCount, uint256 timestamp);

    TableFactory tableFactory;

    constructor(address tableFactoryAddress) public {
        tableFactory = TableFactory(tableFactoryAddress);
        tableFactory.createTable(TABLE_NAME, "hash", "metadata,owner,timestamp,verify_count,status");
    }

    function saveEvidence(string hash, string metadata) public returns (int256) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Entry entry = table.newEntry();
        entry.set("hash", hash);
        entry.set("metadata", metadata);
        entry.set("owner", toString(msg.sender));
        entry.set("timestamp", uint256ToString(now));
        entry.set("verify_count", "0");
        entry.set("status", "1");

        int256 count = table.insert(hash, entry);
        if (count > 0) {
            evidenceCount += 1;
            emit EvidenceSaved(hash, metadata, toString(msg.sender), now);
        }
        return count;
    }

    function getEvidence(string hash) public view returns (string metadata, string owner, string timestamp, string verifyCount) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("hash", hash);

        Entries entries = table.select(hash, condition);
        if (entries.size() > 0) {
            Entry entry = entries.get(0);
            metadata = entry.getString("metadata");
            owner = entry.getString("owner");
            timestamp = entry.getString("timestamp");
            verifyCount = entry.getString("verify_count");
        }
    }

    function verifyEvidence(string hash) public returns (bool, string) {
        Table table = tableFactory.openTable(TABLE_NAME);
        Condition condition = table.newCondition();
        condition.EQ("hash", hash);

        Entries entries = table.select(hash, condition);
        if (entries.size() == 0) {
            return (false, "");
        }

        Entry entry = entries.get(0);
        uint256 currentCount = stringToUint256(entry.getString("verify_count"));
        uint256 newCount = currentCount + 1;

        Entry updateEntry = table.newEntry();
        updateEntry.set("verify_count", uint256ToString(newCount));

        table.update(hash, updateEntry, condition);

        emit EvidenceVerified(hash, newCount, now);
        return (true, uint256ToString(newCount));
    }

    function getEvidenceCount() public view returns (uint256) {
        return evidenceCount;
    }

    function toString(address x) internal pure returns (string) {
        bytes memory s = new bytes(42);
        s[0] = "0";
        s[1] = "x";
        for (uint256 i = 0; i < 20; i++) {
            byte b = byte(uint8(uint256(x) / (2 ** (8 * (19 - i)))));
            byte hi = byte(uint8(b) / 16);
            byte lo = byte(uint8(b) - 16 * uint8(hi));
            s[2 + 2 * i] = char(hi);
            s[3 + 2 * i] = char(lo);
        }
        return string(s);
    }

    function char(byte b) internal pure returns (byte c) {
        if (b < 10) return byte(uint8(b) + 0x30);
        else return byte(uint8(b) + 0x57);
    }

    function uint256ToString(uint256 v) internal pure returns (string) {
        if (v == 0) return "0";
        uint256 maxlength = 100;
        bytes memory reversed = new bytes(maxlength);
        uint256 i = 0;
        while (v != 0) {
            uint256 remainder = v % 10;
            v = v / 10;
            reversed[i++] = byte(48 + remainder);
        }
        bytes memory s = new bytes(i);
        for (uint256 j = 0; j < i; j++) {
            s[j] = reversed[i - 1 - j];
        }
        return string(s);
    }

    function stringToUint256(string s) internal pure returns (uint256) {
        bytes memory b = bytes(s);
        uint256 result = 0;
        for (uint256 i = 0; i < b.length; i++) {
            if (b[i] >= 48 && b[i] <= 57) {
                result = result * 10 + (uint256(b[i]) - 48);
            }
        }
        return result;
    }
}
