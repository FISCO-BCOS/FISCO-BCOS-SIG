# ​FISCOBCOS使用caliper压测报错合集

作者 : 湖南财政经济学院 刘赛花 
       重庆电子工程职业学院 向键雄


## 前言

压测无非就是说一个区块链网络中最大的阈值，那么为什么要压测呢？如果你是简单的进行一个单机四节点哈哈哈哈哈那么我劝你就别搞个压测了，但是如果你要进行开发，要把它做出来让大家同时使用，那么我劝你最好还是压测一下。


## 正文

### 错一：

```

caliper遇到 error [caliper] [bind] Failed to execute “npm“ with return code 1.Command failed

这是我在bind的时候遇到的报错这里我们只需要完整版在这里

 npm rebuild
 
npx caliper bind --caliper-bind-sut fisco-bcos --caliper-bind-sdk latest

```

### 错二：

```

这个报错我是在启动HELLO WORLD的时候遇到的，我们只需要进行docker重启就可以了

sudo service docker start
Compiling error: 
docker: Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?.
See 'docker run --help'.

2022.09.20-02:58:19.973 error [caliper] [installSmartContract.js] 	Deploying error: undefined
2022.09.20-02:58:19.973 error [caliper] [installSmartContract.js] 	Failed to install smart contract helloworld, path=/home/fisco/benchmarks/caliper-benchmarks/src/fisco-bcos/helloworld/HelloWorld.sol

```

### 错三：

```

Command 'npm' not found

这个是npm init的时候，我们就需要重新下载一下npm

sudo apt install npm

```


### 错四：

```

"echo \"Error: no test specified\" && exit 1"


 "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "start":"node service.js"
  }

这里我们有两个问题一个是没有用nvm安装nodejs，一个就是指定运行一下js文件就可以了直接在终端执行 node xx.js

```


### 错五：

```

npm WARN benchmarks@1.0.0 No description
npm WARN benchmarks@1.0.0 No repository field.
npm ERR! Linux 5.4.0-124-generic
npm ERR! argv "/usr/bin/node" "/usr/bin/npm" "install" "--only=prod" "@hyperledger/caliper-cli@0.2.0"
npm ERR! node v8.10.0
npm ERR! npm  v3.5.2
npm ERR! path /home/fisco/benchmarks/node_modules/.staging/@types/node-77814c16/package.json
npm ERR! code ENOTDIR
npm ERR! errno -20
npm ERR! syscall open

npm ERR! ENOTDIR: not a directory, open '/home/fisco/benchmarks/node_modules/.staging/@types/node-77814c16/package.json'
npm ERR! 
npm ERR! If you need help, you may report this error at:
npm ERR!     <https://github.com/npm/npm/issues>

npm ERR! Please include the following file with any support request:
npm ERR!     /home/fisco/benchmarks/npm-debug.log
这个问题就是Npm和nodejs版本不对{"node":">=8.10.0","npm":">=5.6.0"} 

```

### 错六：

```

Command 'npx' not found

这个问题就是caliper没有下载完成，需要重新配置环境下载caliper

```


### 错七：

```

Command 'nvm' not found

这个问题就是在nvm安装的时候没有完成指定

curl -o- https://gitee.com/mirrors/nvm/raw/v0.33.2/install.sh | bash

source ~/.$(basename $SHELL)rc
换成国内的之后刷新环境变量就可以了

```


### 错八：

```

 ╭────────────────────────────────────────────────────────────────╮
   │                                                                │
   │      New major version of npm available! 6.13.4 → 8.19.2       │
   │   Changelog: https://github.com/npm/cli/releases/tag/v8.19.2   │
   │               Run npm install -g npm to update!                │
   │                                                                │
   ╰────────────────────────────────────────────────────────────────╯

这个严格来说不算报错，只能说我们修改了原来指定的版本

```


### 错九：

```

这个报错也是在hello world中遇到的

2022.09.20-02:52:38.309 error [caliper] [installSmartContract.js] 	Deploying error: undefined
2022.09.20-02:52:38.310 error [caliper] [installSmartContract.js] 	Failed to install smart contract helloworld, path=/home/fisco/benchmarks/caliper-benchmarks/src/fisco-bcos/helloworld/HelloWorld.sol
这个就是我们没有下载启动docker，需要启动docker服务与下载docker服务并运行才可以

```


### 错十：

```

2 packages are looking for funding
  run `npm fund` for details

found 20 vulnerabilities (10 moderate, 10 high)
  run `npm audit fix` to fix them, or `npm audit` for details


这一类也不算报错，如果后期有报错的运行修复即可

npm audit fix

```

### 错十一：


```

这个问题在绑定的时候较多

npm ERR! Error while executing:
npm ERR! /usr/bin/git ls-remote -h -t https://github.com/frozeman/bignumber.js-nolookahead.git
npm ERR! 
npm ERR! fatal: unable to access 'https://github.com/frozeman/bignumber.js-nolookahead.git/': gnutls_handshake() failed: Error in the pull function.
这个问题是因为我们没有配置git下载git即可

sudo apt install -y git

```

### 错十二：

```

npm ERR! Error while executing:
npm ERR! /usr/bin/git ls-remote -h -t https://github.com/frozeman/bignumber.js-nolookahead.git
npm ERR! 
npm ERR! fatal: unable to access 'https://github.com/frozeman/bignumber.js-nolookahead.git/': Failed to connect to github.com port 443: Connection refused
npm ERR! 
npm ERR! exited with error code: 128

npm ERR! A complete log of this run can be found in:
npm ERR!     /home/fisco/.npm/_logs/2022-09-20T10_46_40_887Z-debug.log
2022.09.20-03:46:40.893 error [caliper] [bind] 	Failed to execute "npm" with return code 1.
Error: Failed to execute "npm" with return code 1.
    at ChildProcess.proc.on (/home/fisco/benchmarks/node_modules/@hyperledger/caliper-cli/lib/utils/cmdutils.js:56:35)
    at emitTwo (events.js:126:13)
    at ChildProcess.emit (events.js:214:7)
    at Process.ChildProcess._handle.onexit (internal/child_process.js:198:12)
Command failed

这里就去下载make和py2.7

sudo apt install -y make 

sudo apt install -y python2.7

sudo apt install -y pip


```


​
