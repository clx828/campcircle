# API日志记录使用说明

## 概述

本项目使用AOP（面向切面编程）技术实现API请求日志的自动记录，支持灵活的配置和敏感信息脱敏。

## 功能特性

### 1. 自动日志记录
- 自动拦截所有Controller方法
- 记录请求参数、响应结果、执行时间
- 记录用户信息、IP地址、异常信息
- 异步保存，不影响业务性能

### 2. 灵活配置
- 支持@ApiLog注解自定义配置
- 可控制是否记录参数、响应、异常
- 支持长度截断，避免数据过大
- 支持敏感信息脱敏

### 3. 统计分析
- 提供丰富的统计查询接口
- 支持按时间、用户、接口等维度分析
- 错误统计、慢请求统计
- 按小时统计请求量

## 使用方式

### 1. 默认记录（无需配置）

所有Controller方法都会自动记录日志：

```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    @GetMapping("/list")
    public BaseResponse<List<User>> getUserList() {
        // 这个方法会自动记录日志
        return ResultUtils.success(userList);
    }
}
```

### 2. 自定义配置

使用@ApiLog注解进行自定义配置：

```java
@PostMapping("/login")
@ApiLog(
    value = "用户登录",
    operationType = "LOGIN",
    ignoreSensitive = true,
    sensitiveParams = {"password", "token"}
)
public BaseResponse<String> login(@RequestParam String username,
                                 @RequestParam String password) {
    // 密码参数会被自动脱敏
    return ResultUtils.success("登录成功");
}
```

### 3. 不记录敏感响应

```java
@GetMapping("/sensitive-data")
@ApiLog(
    value = "获取敏感数据",
    logParams = true,
    logResult = false  // 不记录响应结果
)
public BaseResponse<SensitiveData> getSensitiveData() {
    // 响应结果不会被记录到日志中
    return ResultUtils.success(sensitiveData);
}
```

## @ApiLog注解参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 操作描述 |
| operationType | String | "" | 操作类型 |
| logParams | boolean | true | 是否记录请求参数 |
| logResult | boolean | true | 是否记录响应结果 |
| logException | boolean | true | 是否记录异常信息 |
| maxParamLength | int | 5000 | 参数最大长度 |
| maxResultLength | int | 5000 | 响应最大长度 |
| ignoreSensitive | boolean | true | 是否忽略敏感参数 |
| sensitiveParams | String[] | {"password", "pwd", "token", "secret", "key"} | 敏感参数名称 |

## 配置示例

### 1. 登录接口
```java
@ApiLog(
    value = "用户登录",
    operationType = "LOGIN",
    ignoreSensitive = true,
    sensitiveParams = {"password", "captcha"}
)
```

### 2. 查询接口
```java
@ApiLog(
    value = "查询用户列表",
    operationType = "QUERY",
    maxResultLength = 10000
)
```

### 3. 文件上传接口
```java
@ApiLog(
    value = "文件上传",
    operationType = "UPLOAD",
    logParams = false,  // 不记录文件内容
    maxResultLength = 1000
)
```

### 4. 敏感操作接口
```java
@ApiLog(
    value = "删除用户",
    operationType = "DELETE",
    logParams = true,
    logResult = false  // 不记录删除结果
)
```

## 日志查询

### 1. 分页查询日志
```http
POST /api/api-log/list
Content-Type: application/json

{
    "current": 1,
    "pageSize": 10,
    "requestUrl": "/user/login",
    "startTime": "2023-01-01 00:00:00",
    "endTime": "2023-12-31 23:59:59"
}
```

### 2. 获取统计信息
```http
GET /api/api-log/statistics?startTime=2023-01-01 00:00:00&endTime=2023-12-31 23:59:59
```

### 3. 获取最频繁访问的接口
```http
GET /api/api-log/top-apis?startTime=2023-01-01 00:00:00&endTime=2023-12-31 23:59:59&limit=10
```

## 性能考虑

### 1. 异步处理
- 日志记录使用异步处理，不阻塞业务线程
- 配置了专门的线程池处理日志任务

### 2. 长度限制
- 默认限制参数和响应长度为5000字符
- 可通过注解参数调整限制

### 3. 敏感信息脱敏
- 自动识别敏感参数并脱敏
- 避免记录完整的密码、token等信息

### 4. 定时清理
- 自动清理30天前的日志数据
- 避免数据库存储过多历史数据

## 注意事项

1. **权限控制**：日志查询接口需要管理员权限
2. **数据安全**：敏感信息会自动脱敏，但仍需注意数据安全
3. **存储空间**：定期清理历史日志，避免占用过多存储空间
4. **性能影响**：虽然使用异步处理，但大量请求仍可能影响性能

## 排除规则

以下请求不会被记录：
- 静态资源请求（/static/, /css/, /js/, /images/）
- 健康检查接口（/health, /actuator/）
- Swagger相关接口（/swagger, /api-docs, /webjars/）
- 日志查询接口本身（避免递归记录）

## 扩展功能

### 1. 自定义排除规则
可以在AOP类中修改`shouldLogRequest`方法来自定义排除规则。

### 2. 添加更多统计维度
可以在Mapper中添加更多统计查询方法。

### 3. 集成监控系统
可以将日志数据推送到监控系统进行实时分析。
