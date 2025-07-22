# 用户背景字段扩展文档

## 概述
本次扩展为用户背景信息添加了URL字段，并完善了现有的性别和学校字段在各个层级的支持。

## 修改内容

### 1. 数据库表结构修改
- **文件**: `sql/create_table.sql`
- **新增字段**: `backgroundUrl varchar(1024) NULL COMMENT '用户背景URL'`
- **迁移脚本**: `sql/migration/add_background_url_to_user.sql`

### 2. 实体类修改
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/entity/User.java`
- **修改内容**:
  - 新增 `backgroundUrl` 字段
  - 将 `gender` 字段类型从 `int` 改为 `Integer` 以保持一致性

### 3. VO类修改
#### UserVO
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/vo/UserVO.java`
- **新增字段**: `gender`, `school`, `backgroundUrl`

#### LoginUserVO
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/vo/LoginUserVO.java`
- **新增字段**: `gender`, `school`, `backgroundUrl`

### 4. DTO类修改
#### UserUpdateMyRequest
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/dto/user/UserUpdateMyRequest.java`
- **新增字段**: `gender`, `school`, `backgroundUrl`

#### UserUpdateRequest
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/dto/user/UserUpdateRequest.java`
- **新增字段**: `gender`, `school`, `backgroundUrl`

#### UserAddRequest
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/dto/user/UserAddRequest.java`
- **新增字段**: `userProfile`, `gender`, `school`, `backgroundUrl`

#### UserQueryRequest
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/model/dto/user/UserQueryRequest.java`
- **新增字段**: `gender`, `school`

### 5. 服务层修改
- **文件**: `campcircle-backend/src/main/java/com/caden/campcircle/service/impl/UserServiceImpl.java`
- **修改内容**:
  - `getQueryWrapper` 方法新增对 `gender` 和 `school` 字段的查询支持
  - `listUserVOByPage` 方法的关键词搜索新增对 `school` 字段的支持

## 字段说明

### gender (性别)
- **类型**: `Integer`
- **取值**: 
  - `0`: 未知
  - `1`: 男
  - `2`: 女
- **数据库类型**: `tinyint`

### school (学校)
- **类型**: `String`
- **长度**: 最大256字符
- **数据库类型**: `varchar(256)`

### backgroundUrl (用户背景URL)
- **类型**: `String`
- **长度**: 最大1024字符
- **数据库类型**: `varchar(1024)`
- **用途**: 存储用户个人资料页面的背景图片URL

## 使用示例

### 更新个人信息
```java
UserUpdateMyRequest request = new UserUpdateMyRequest();
request.setUserName("张三");
request.setGender(1); // 男
request.setSchool("清华大学");
request.setBackgroundUrl("https://example.com/background.jpg");
```

### 查询用户
```java
UserQueryRequest queryRequest = new UserQueryRequest();
queryRequest.setGender(1); // 查询男性用户
queryRequest.setSchool("清华大学"); // 查询清华大学的用户
```

### 关键词搜索
现在支持通过学校名称进行用户搜索：
```java
PageSearchByKeyWord searchRequest = new PageSearchByKeyWord();
searchRequest.setKeyWord("清华"); // 会搜索用户名、简介、学校中包含"清华"的用户
```

## 测试
- **测试文件**: `campcircle-backend/src/test/java/com/caden/campcircle/model/UserBackgroundFieldsTest.java`
- **测试内容**: 验证所有类中背景字段的正确性和BeanUtils复制功能

## 数据库迁移
执行以下SQL脚本来为现有数据库添加新字段：
```sql
-- 添加用户背景URL字段
ALTER TABLE user ADD COLUMN backgroundUrl varchar(1024) NULL COMMENT '用户背景URL' AFTER school;
```

## 注意事项
1. 所有背景字段都是可选的（nullable）
2. `gender` 字段使用 `Integer` 类型以支持 null 值
3. `backgroundUrl` 字段长度为1024字符，足够存储大部分URL
4. 现有的BeanUtils.copyProperties调用会自动复制新字段
5. 查询功能已扩展支持新的背景字段
