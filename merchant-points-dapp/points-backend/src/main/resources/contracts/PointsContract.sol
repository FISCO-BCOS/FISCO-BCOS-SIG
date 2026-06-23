pragma solidity ^0.4.25;

contract PointsContract {
    address public owner;
    mapping(address => uint256) public balances;

    event PointsIssued(address indexed to, uint256 amount, string metadata, uint256 timestamp);
    event PointsTransferred(address indexed from, address indexed to, uint256 amount, uint256 timestamp);
    event PointsConsumed(address indexed from, uint256 amount, string metadata, uint256 timestamp);

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner");
        _;
    }

    constructor() public {
        owner = msg.sender;
    }

    function issuePoints(address to, uint256 amount, string metadata)
        public onlyOwner returns (bool)
    {
        require(amount > 0, "Amount must be positive");
        require(to != address(0), "Invalid recipient");
        uint256 newBalance = balances[to] + amount;
        require(newBalance >= balances[to], "Overflow");
        balances[to] = newBalance;
        emit PointsIssued(to, amount, metadata, now);
        return true;
    }

    function transfer(address to, uint256 amount) public returns (bool) {
        require(to != address(0), "Invalid recipient");
        require(to != msg.sender, "Cannot transfer to yourself");
        require(amount > 0, "Amount must be positive");
        require(balances[msg.sender] >= amount, "Insufficient balance");
        uint256 toNewBalance = balances[to] + amount;
        require(toNewBalance >= balances[to], "Overflow");
        balances[msg.sender] -= amount;
        balances[to] = toNewBalance;
        emit PointsTransferred(msg.sender, to, amount, now);
        return true;
    }

    function consume(uint256 amount, string metadata) public returns (bool) {
        require(amount > 0, "Amount must be positive");
        require(balances[msg.sender] >= amount, "Insufficient balance");
        balances[msg.sender] -= amount;
        emit PointsConsumed(msg.sender, amount, metadata, now);
        return true;
    }

    function getBalance(address account) public view returns (uint256) {
        return balances[account];
    }

    function getTransferHistory(address, uint256, uint256)
        public view returns (address[] memory froms, address[] memory tos,
                            uint256[] memory amounts, uint256[] memory timestamps)
    {
        froms = new address[](0);
        tos = new address[](0);
        amounts = new uint256[](0);
        timestamps = new uint256[](0);
    }
}