package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 系统消息
 *
 */
@TableName(value = "system_message")
@Data
public class SystemMessage implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发送用户 id（系统消息时为0，表示系统发送）
     */
    private Long fromUserId;

    /**
     * 接收用户 id
     */
    private Long toUserId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：0-系统通知，1-点赞通知，2-收藏通知，3-评论通知，4-关注通知
     */
    private Integer type;

    /**
     * 关联的帖子ID（如果是帖子相关的通知）
     */
    private Long postId;

    /**
     * 关联的评论ID（如果是评论相关的通知）
     */
    private Long commentId;

    /**
     * 状态：0-未读，1-已读
     */
    private Integer status;

    /**
     * 是否为全局消息：0-否，1-是（发送给所有用户）
     */
    private Integer isGlobal;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
