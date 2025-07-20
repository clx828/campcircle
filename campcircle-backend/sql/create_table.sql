# 数据库初始化

-- 创建库
create database if not exists camp_circle;

-- 切换库
use camp_circle;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userStatus  tinyint  default 1                 not null comment '状态：0-待审核，1-正常，2-封禁',
-- 增加性别字段
    gender      tinyint                            null comment '性别：0-未知，1-男，2-女',
-- 增加学校字段
    school      varchar(256)                       null comment '学校',
-- 增加粉丝数和关注数字段
    followNum   int      default 0                 not null comment '关注数',
    fansNum     int      default 0                 not null comment '粉丝数',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    -- 增加评论数字段
    commentNum  int      default 0                 not null comment '评论数',
    -- 增加浏览数字段
    viewNum     int      default 0                 not null comment '浏览数',
    -- 增加是否匿名字段
    isAnonymous tinyint  default 0                 not null comment '是否匿名',
    -- 增加帖子状态字段
    status      tinyint  default 1                 not null comment '状态：0-待审核，1-已发布，2-已隐藏',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;
ALTER TABLE post ADD COLUMN  pictureList VARCHAR(255)  COMMENT '图片ID列表';
ALTER TABLE post DROP COLUMN title;

-- 在 post 表中添加置顶相关字段
ALTER TABLE post ADD COLUMN isTop TINYINT DEFAULT 0 NOT NULL COMMENT '是否置顶：0-否，1-是';
ALTER TABLE post ADD COLUMN topExpireTime DATETIME NULL COMMENT '置顶过期时间';
# 是否公开
ALTER TABLE post ADD COLUMN isPublic TINYINT DEFAULT 1 NOT NULL COMMENT '是否公开：0-否，1-是';

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 图片表
CREATE TABLE IF NOT EXISTS picture (
   id          BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
   pictureUrl  VARCHAR(512)                       NOT NULL COMMENT '图片链接',
   userId  BIGINT                             NOT NULL COMMENT '上传用户ID',
   createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
   updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   isDelete    TINYINT  DEFAULT 0                  NOT NULL COMMENT '是否删除',
-- 创建索引
   INDEX idx_uploaderId (userId)
) COMMENT '图片' COLLATE = utf8mb4_unicode_ci;

-- 消息表
create table if not exists message
(
    id         bigint auto_increment comment 'id' primary key,
    fromUserId bigint                             not null comment '发送用户 id',
    toUserId   bigint                             not null comment '接收用户 id',
    content    text                               not null comment '消息内容',
    type       tinyint                            not null comment '消息类型：0-系统消息，1-私信，2-点赞通知，3-评论通知，4-关注通知',
    status     tinyint  default 0                 not null comment '状态：0-未读，1-已读',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_fromUserId (fromUserId),
    index idx_toUserId (toUserId)
) comment '消息' collate = utf8mb4_unicode_ci;

-- 关注表
create table if not exists follow
(
    id              bigint auto_increment comment 'id' primary key,
    userId          bigint                             not null comment '关注用户 id',
    followUserId    bigint                             not null comment '被关注用户 id',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId),
    index idx_followUserId (followUserId)
) comment '关注' collate = utf8mb4_unicode_ci;

-- 评论表
create table if not exists post_comment
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '评论用户 id',
    content    text                               not null comment '评论内容',
    parentId   bigint                             null comment '父评论 id，为空则是一级评论',
    replyUserId bigint                            null comment '回复的用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '评论' collate = utf8mb4_unicode_ci;
alter table post_comment
    add column level tinyint default 1 not null comment '评论层级：1一级评论，2二级评论';

use camp_circle;
-- 私信消息表
CREATE TABLE private_message
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    fromUserId  BIGINT NOT NULL COMMENT '发送者ID',
    toUserId    BIGINT NOT NULL COMMENT '接收者ID',
    content     TEXT COMMENT '消息内容',
    messageType TINYINT  DEFAULT 0 COMMENT '消息类型:0文本,1图片',
    pictureUrl  VARCHAR(500) COMMENT '图片URL',
    isRead      TINYINT  DEFAULT 0 COMMENT '是否已读',
    isRecalled  TINYINT  DEFAULT 0 COMMENT '是否已撤回:0否,1是',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_from_to_time (fromUserId, toUserId, createTime),
    INDEX idx_to_read (toUserId, isRead)

);

-- 系统通知表
CREATE TABLE system_notification
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    title            VARCHAR(100) NOT NULL COMMENT '通知标题',
    content          TEXT         NOT NULL COMMENT '通知内容',
    notificationType TINYINT      NOT NULL COMMENT '通知类型:1点赞,2评论,3关注,4系统',
    targetUserId     BIGINT COMMENT '目标用户ID(0表示全体用户)',
    relatedId        BIGINT COMMENT '关联ID(帖子ID等)',
    isRead           TINYINT  DEFAULT 0 COMMENT '是否已读',
    createTime       DATETIME DEFAULT CURRENT_TIMESTAMP
);

use camp_circle;
CREATE TABLE `api_request_log`
(
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `traceId`        VARCHAR(64)  DEFAULT NULL COMMENT '链路追踪ID',
    `requestMethod`  VARCHAR(10)     NOT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
    `requestUrl`     VARCHAR(255)    NOT NULL COMMENT '请求URI',
    `requestParams`  TEXT COMMENT 'URL参数（GET）',
    `requestBody`    LONGTEXT COMMENT '请求体内容（POST JSON等）',
    `requestHeaders` TEXT COMMENT '请求头信息（JSON格式）',
    `clientIp`       VARCHAR(45)  DEFAULT NULL COMMENT '客户端IP地址',
    `userAgent`      VARCHAR(255) DEFAULT NULL COMMENT 'User-Agent信息',
    `responseStatus` INT          DEFAULT NULL COMMENT '响应HTTP状态码',
    `responseBody`   LONGTEXT COMMENT '响应内容（JSON）',
    `exceptionInfo`  TEXT COMMENT '异常信息（如有）',
    `durationMs`     INT          DEFAULT NULL COMMENT '请求耗时（毫秒）',
    `userId`         BIGINT       DEFAULT NULL COMMENT '用户ID（如已登录）',
    `createTime`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    INDEX `idx_trace_id` (`traceId`),
    INDEX `idx_uri_time` (`requestUrl`, `createTime`),
    INDEX `idx_user_time` (`userId`, `createTime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接口请求日志表';
-- 添加发送者到接收者的联合索引
ALTER TABLE private_message ADD INDEX idx_from_to (fromUserId, toUserId);

-- 添加接收者到发送者的联合索引
ALTER TABLE private_message ADD INDEX idx_to_from (toUserId, fromUserId);

-- 添加热度分数字段
ALTER TABLE post ADD COLUMN hotScore DECIMAL(10,4) DEFAULT 0.0000 NOT NULL COMMENT '热度分数';
ALTER TABLE post ADD COLUMN lastHotUpdateTime DATETIME NULL COMMENT '最后热度更新时间';

-- 为热度分数添加索引，用于热门排序
ALTER TABLE post ADD INDEX idx_hot_score (hotScore DESC, createTime DESC);
