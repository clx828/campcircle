package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信消息
 *
 * @author caden
 */
@TableName(value = "private_message")
@Data
public class PrivateMessage implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long fromUserId;

    /**
     * 接收者ID
     */
    private Long toUserId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型:0文本,1图片
     */
    private Integer messageType;

    /**
     * 图片URL
     */
    private String pictureUrl;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 是否已撤回:0否,1是
     */
    private Integer isRecalled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
