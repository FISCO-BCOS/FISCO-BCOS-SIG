# go第三方库

## go-swagger

### 安装Swagger

#### 安装

```shell
$ go get -u github.com/swaggo/swag/cmd/swag 
# go 1.18版本以后
$ go install github.com/swaggo/swag/cmd/swag@latest
```

#### 检查是否安装成功

```sh
$ swag -v
# swag.exe version v1.8.12
```

#### 安装gin-swagger

```sh
$ go get -u -v github.com/swaggo/gin-swagger
$ go get -u -v github.com/swaggo/files
$ go get -u -v github.com/alecthomas/template
```

### 使用

#### 添加注释

```go
package main

//	@title			测试
//	@version		1.0
//	@description	测试文档
func main() {
	// 创建服务
	ginServer := gin.Default()

	ginServer.Run(":8081")
}
```

```go
//	@Summary	测试用例
//	@Produce	json
//	@Success	200	{object}	string	"请求成功"
//	@Failure	400	{object}	string	"请求错误"
//	@Router		/api/get [get]
func Test(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"message": "swagger 测试成功",
	})
}

//	@Summary		请求用例
//	@Description	接口描述
//	@Tags			接口标签
//	@Produce		json
//	@Param			name	query		string	true	"姓名"
//	@Success		200		{object}	string	"请求成功"
//	@Failure		400		{object}	string	"请求失败"
//	@Router			/api/post [post]
func Post(c *gin.Context) {
	Name := c.Query("name")
	c.JSON(http.StatusOK, gin.H{
		"data":    Name,
		"message": "请求成功",
	})
}
```

#### 生成接口文档数据

格式化swag注解

```sh
$ swag fmt
```

生成文档数据

```sh
$ swag init
# 修改API后重新执行，生成新的文档
```

若不存在错误，生成以下文件夹

```
./docs
├── docs.go
├── swagger.json
└── swagger.yaml
```

#### 引入gin-swagger渲染文档数据

```go
import (
	"github.com/gin-gonic/gin"
	"github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	_ "github/mwqnice/swag/docs" // 千万不要忘了导入把你上一步生成的docs
)
//添加swagger访问路由
r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
```

启动项目，访问http://localhost:8081/swagger/index.html

## Viper

### 安装

```sh
go get github.com/spf13/viper
```

### 把值存入viper

> 建立默认值

```go
// viper设置默认值
viper.SetDefault("fileDir", "./")
```

> 读取配置文件

```go
// 读取配置文件
viper.SetConfigFile("./config.yaml")  // 指定配置文件路径
viper.SetConfigName("config")         // 配置文件名称(无扩展名)
viper.SetConfigType("yaml")           // 如果配置文件的名称中没有扩展名，则需要配置此项
viper.AddConfigPath("/etc/appname/")  // 查找配置文件所在的路径
viper.AddConfigPath("$HOME/.appname") // 多次调用以添加多个搜索路径
viper.AddConfigPath(".")              // 还可以在工作目录中查找配置
err := viper.ReadInConfig()           // 查找并读取配置文件
if err != nil {                       // 处理读取配置文件的错误
    panic(fmt.Errorf("Fatal error config file: %s \n", err))
}
```

> 监控并重新读取配置文件

```go
viper.WatchConfig()
viper.OnConfigChange(func(e fsnotify.Event) {
    // 配置文件发生变更之后会调用的回调函数
    fmt.Println("Config file changed:", e.Name)
})
```

