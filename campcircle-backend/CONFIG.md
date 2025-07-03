# 配置文件说明

## 概述

为了保护敏感信息（如数据库密码、API密钥等）不被提交到远程仓库，本项目采用了以下配置策略：

## 配置文件结构

```
src/main/resources/
├── application.yml                    # 主配置文件（公共配置，可提交）
├── application-local.yml              # 本地开发配置（敏感信息，不提交）
└── application-local.yml.template     # 本地配置模板（可提交）
```

## 配置方式

### 方式1：使用 application-local.yml（推荐）

1. **复制模板文件**
   ```bash
   cp src/main/resources/application-local.yml.template src/main/resources/application-local.yml
   ```

2. **修改配置**
   编辑 `application-local.yml` 文件，填入真实的配置信息：
   ```yaml
   spring:
     datasource:
       password: your_real_db_password
   
   cos:
     client:
       SecretId: your_real_secret_id
       SecretKey: your_real_secret_key
   
   jwt:
     secret: your_real_jwt_secret
   ```

3. **启动应用**
   应用会自动加载 `application-local.yml` 配置文件

### 方式2：使用环境变量

设置以下环境变量：

```bash
# 数据库配置
export DB_PASSWORD=your_db_password
export DB_URL=jdbc:mysql://localhost:3306/camp_circle
export DB_USERNAME=root

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# 微信配置
export WX_MP_TOKEN=your_wx_mp_token
export WX_MP_AES_KEY=your_wx_mp_aes_key
export WX_MP_APP_ID=your_wx_mp_app_id
export WX_MP_SECRET=your_wx_mp_secret
export WX_OPEN_APP_ID=your_wx_open_app_id
export WX_OPEN_APP_SECRET=your_wx_open_app_secret
export WX_MINIAPP_APP_ID=your_wx_miniapp_app_id
export WX_MINIAPP_APP_SECRET=your_wx_miniapp_app_secret

# 对象存储配置
export COS_HOST=https://your-bucket.cos.region.myqcloud.com
export COS_SECRET_ID=your_cos_secret_id
export COS_SECRET_KEY=your_cos_secret_key
export COS_REGION=ap-guangzhou
export COS_BUCKET=your-bucket-name

# JWT配置
export JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long

# RocketMQ配置
export ROCKETMQ_NAME_SERVER=localhost:9876
```

### 方式3：IDE环境变量配置

在 IntelliJ IDEA 中：
1. 打开 Run/Debug Configurations
2. 选择你的应用配置
3. 在 Environment variables 中添加上述环境变量

## 配置项说明

### 数据库配置
- `DB_URL`: 数据库连接地址
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码

### Redis配置
- `REDIS_HOST`: Redis服务器地址
- `REDIS_PORT`: Redis端口
- `REDIS_PASSWORD`: Redis密码（可选）

### 微信配置
- `WX_MP_*`: 微信公众号相关配置
- `WX_OPEN_*`: 微信开放平台配置
- `WX_MINIAPP_*`: 微信小程序配置

### 对象存储配置
- `COS_HOST`: 腾讯云COS访问域名
- `COS_SECRET_ID`: 腾讯云API密钥ID
- `COS_SECRET_KEY`: 腾讯云API密钥Key
- `COS_REGION`: 存储桶地域
- `COS_BUCKET`: 存储桶名称

### JWT配置
- `JWT_SECRET`: JWT签名密钥（建议至少32位字符）

### RocketMQ配置
- `ROCKETMQ_NAME_SERVER`: RocketMQ Name Server地址

## 安全注意事项

1. **永远不要将敏感信息提交到代码仓库**
2. **定期更换密钥和密码**
3. **使用强密码和复杂的JWT密钥**
4. **在生产环境中使用环境变量而不是配置文件**
5. **限制配置文件的访问权限**

## 部署说明

### 开发环境
使用 `application-local.yml` 文件

### 测试环境
使用环境变量或 `application-test.yml` 文件

### 生产环境
**必须**使用环境变量，不要使用配置文件存储敏感信息

## 故障排除

### 配置文件未生效
1. 检查 `spring.profiles.active` 是否设置为 `local`
2. 确认 `application-local.yml` 文件存在且格式正确
3. 检查环境变量是否正确设置

### 启动失败
1. 检查数据库连接配置
2. 确认Redis服务是否启动
3. 验证所有必需的配置项是否已设置
