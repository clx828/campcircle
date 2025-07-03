package com.caden.campcircle.model.dto.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 发送私信请求
 *
 * @author caden
 */
@Data
public class PrivateMessageSendRequest implements Serializable {

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long toUserId;

    /**
     * 消息内容（文本消息时必填）
     */
    private String content;

    /**
     * 消息类型:0文本,1图片
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 图片URL（图片消息时必填，通过图片上传接口获取）
     */
    private String pictureUrl;

    /**
     * 图片ID（可选，用于关联图片记录）
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
