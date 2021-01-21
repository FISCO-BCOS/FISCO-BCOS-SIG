# Git使用规范

## 参考资料

- [常用的git命令介绍](https://www.ruanyifeng.com/blog/2015/12/git-cheat-sheet.html)
- [git commit message编写指南](https://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html)
- [git commit规范化插件](https://github.com/commitizen/cz-cli)

## 常用的git命令

```bash
git status
git remote add [上游仓库名一般用upstream] [地址，例如https://github.com/FISCO-BCOS/FISCO-BCOS.git]
git remote -v
git fetch upstream
git checkout
git log
git rebase upstream/dev -i
git commit -m "commit message"
git commit --amend
git diff
git stash/git stash pop
git push
git cherry-pick
git merge
```

## git开发示例

下面以基于[FISCO-BCOS](https://github.com/FISCO-BCOS/FISCO-BCOS)进行进行协作开发为例，介绍git开发的主要步骤和流程

### 同步代码

**步骤1**. 若没有`Fork`过FISCO BCOS项目，则从[FISCO BCOS](https://github.com/FISCO-BCOS/FISCO-BCOS)官方仓库`Fork`代码

**步骤2**. 若没有设置upstream需要设置官方项目为upstream：

```bash
# 将FISCO BCOS官方仓库设置为upstream
git remote add upstream https://github.com/FISCO-BCOS/FISCO-BCOS

# 查看设置情况
git remote -v
```

**步骤3**. 切换到dev分支，并将官方代码同步到本地

```bash
# 切换到dev分支
git checkout dev

# 获取最新的官方代码
git fetch upstream

# 将官方代码的最新修改同步到本地dev分支
git rebase upstream/dev

# 将同步后的本地代码推到个人的远程仓库
git push origin -f
```
  
**步骤4**. 基于dev分支创建feature分支，并在feature分支进行开发:

```bash
# 创建并切换到feature分支
git checkout origin/dev -b feature
```

**步骤5**. 开发过程中使用到的git命令主要包括：

```bash
# 查看本地文件修改状态
git status

# 添加要提交的文件到暂存区
git add 

# 从git中删除文件
git rm 

# 提交暂存区的修改
git commit -m 
git commit --amend

# 将本地提交同步到个人的远程仓库
git push 
```

**步骤6**. 提特性到远程仓库之前，请参考第3步进行代码同步，确保本地代码同步了远程最新代码，主要用到如下命令：

```bash
# 获取远程dev分支最新修改
git fetch upstream dev

# 同步远程dev分支代码
git rebase upstream/dev
```

以上操作完成后，即可提PR到官方仓库，请求将`feature`分支的代码合入到官方仓库的`dev`分支。