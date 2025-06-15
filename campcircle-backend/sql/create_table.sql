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
ALTER TABLE post ADD COLUMN pictureList VARCHAR(255)  COMMENT '图片ID列表';
ALTER TABLE post DROP COLUMN title;

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