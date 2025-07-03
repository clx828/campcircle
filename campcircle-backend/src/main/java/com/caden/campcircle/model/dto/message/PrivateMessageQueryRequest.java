package com.caden.campcircle.model.dto.message;

import com.caden.campcircle.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询私信请求
 *
 * @author caden
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PrivateMessageQueryRequest extends PageRequest implements Serializable {

    /**
     * 聊天对象用户ID
     */
    private Long chatUserId;

    /**
     * 消息类型:0文本,1图片
     */
    private Integer messageType;

    /**
     * 是否已读
     */
    private Integer isRead;

    private static final long serialVersionUID = 1L;
}
