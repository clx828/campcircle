package com.caden.campcircle.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天图片上传请求
 *
 * @author caden
 */
@Data
public class ChatPictureUploadRequest implements Serializable {

    /**
     * 聊天对象用户ID（可选，用于生成特定的上传路径）
     */
    private Long chatUserId;

    /**
     * 图片类型：chat（聊天图片）
     */
    private String pictureType = "chat";

    private static final long serialVersionUID = 1L;
}
