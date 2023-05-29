# fbbatis
FBbatis 是一款针对 Fisco bcos 预编译合约 + Springboot 的数据库 ORM 框架。借鉴于 MyBatis plus 的使用习惯进行开发。
> 地址：https://github.com/idefa/fbbatis

## Fisco内置两种表合约说明
FISCO BCOS 2.0受以太坊内置合约启发，实现了一套预编译合约框架，FISCO BCOS平台目前内置两类预编译合约接口，KVTable合约的读写接口和Table合约的CRUD接口。

* Table的CRUD接口一个key下可以有多条记录，使用时会进行数据批量操作，包括批量写入和范围查询。对应此特性，推荐使用关系型数据库MySQL作为后端数据库。 
* KVTtable的get/set接口时，推荐使用RocksDB作为后端数据库，因RocksDB是Key-Value存储的非关系型数据库，使用KVTable接口时单key操作效率更高

## fbbatis操作类说明
fbbatis也提供两种对应的操作，模拟关系型的Table和非关系型的KVTable操作，
* DataTable类对应Table的CRUD接口，当选择Table类型时候，key就是table的名称，支持增删改查。
* DataSet类对应KVTtable的get/set接口，当选择KVTable的时候，模拟非关系型数据库操作

### sdk配置说明
```yaml
server:
  servlet:
    context-path: /api/bcos
  port: 8023

sdk:
  corePoolSize: 50
  maxPoolSize: 100
  queueCapacity: 100
  ip: 127.0.0.1 # 节点IP
  channelPort: 20200 # 节点端口
  certPath: conf  # 证书地址
```

### 实体映射
```java
@TableName(name = "tb_config",title = "配置表")
@PrimaryKey(name = "name")
@Data
public class ConfigInfo {

    @Column(name = "name",title = "配置名")
    private String name;

    @Column(name = "value",title = "配置值")
    private String value;

    @Column(name = "category",title = "分组")
    private String category;
}
```

### DataTable相关Api
```java
//关系型操作类
public class DataTable<T> {
    // 创建表
    public Object create(int groupId, String fromAddress);
    // 增
    public Object insert(int groupId, String fromAddress,T entity);
    // 改
    public Object update(int groupId,String fromAddress,QueryWrapper<T> queryWrapper,Map<String, Object> columnMap);
    // 删
    public Object remove(int groupId, String fromAddress,QueryWrapper<T> queryWrapper);
    // 查
    public Object select(int groupId,QueryWrapper<T> queryWrapper);
}
```

### DataSet相关Api
```java
//非关系型操作类
public class DataSet<T> {
    // 创建表
    public Object create(int groupId, String fromAddress);
    // 保存实体
    public Object save(int groupId, String fromAddress,T entity)
    // 根据key更新
    public Object updateByKey(int groupId, String fromAddress, String key,Map<String, Object> columnMap);
    // 根据key删除
    public Object removeByKey(int groupId, String fromAddress,String key);
    // 根据key获取
    public Object getByKey(int groupId,String key);
}
```

## 测试示例
### DataTable测试示例
```java
// 自定义Service
public class PatientService extends DataTable<Patient> {}
//初始化表
paientService.create(1,null);
//新增
Patient patient=new Patient();
//patient.setPhone("")
paientService.insert(1,null,patient);
//查询
QueryWrapper<Patient> patientQuery=new QueryWrapper<Patient>();
patientQuery.eq("patient_name",name);
paientService.select(1,patientQuery);
//删除
QueryWrapper<Patient> patientQuery=new QueryWrapper<Patient>();
patientQuery.eq("patient_name",name);
paientService.remove(1,null,patientQuery);
//修改
QueryWrapper<Patient> patientQuery=new QueryWrapper<Patient>();
patientQuery.eq("patient_name",name);
JSONObject  jsonObject = JSONObject.parseObject("修改的Json体");
Map<String,Object> map = (Map<String,Object>)jsonObject;
paientService.update(1,null,patientQuery,map);
```
### DataSet测试示例
```java
// 自定义Service
public class ConfigService extends DataSet<ConfigInfo>{}
//初始化表
configService.create(1,null);
//保存
ConfigInfo config=new ConfigInfo();
configService.save(1,null,config);
//查询
configService.getByKey(1,key);
//删除
configService.removeByKey(1,null,key);
//更新
JSONObject jsonObject = JSONObject.parseObject(value);
Map<String,Object> map = (Map<String,Object>)jsonObject;
configService.updateByKey(1,null,key,map);
```

### 相关资料
* [智能合约](https://github.com/FISCO-BCOS/FISCO-BCOS-DOC/blob/release-2/2.x/docs/manual/smart_contract.md)
* [FISCO BCOS CRUD使用指南](https://github.com/FISCO-BCOS/FISCO-BCOS-DOC/blob/release-2/2.x/docs/articles/3_features/33_storage/crud_guidance.md)
* [使用预编译合约](https://github.com/FISCO-BCOS/FISCO-BCOS-DOC/blob/release-2/2.x/docs/manual/precompiled_contract.md)
