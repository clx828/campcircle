package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统通知
 *
 * @author caden
 */
@TableName(value = "system_notification")
@Data
public class SystemNotification implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型:1点赞,2评论,3关注,4系统
     */
    private Integer notificationType;

    /**
     * 目标用户ID(0表示全体用户)
     */
    private Long targetUserId;

    /**
     * 关联ID(帖子ID等)
     */
    private Long relatedId;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
