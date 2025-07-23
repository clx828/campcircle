package com.caden.campcircle.model.dto.systemmessage;

import java.io.Serializable;
import lombok.Data;

/**
 * 系统消息更新请求
 *
 */
@Data
public class SystemMessageUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 状态：1-未读，0-已读
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
