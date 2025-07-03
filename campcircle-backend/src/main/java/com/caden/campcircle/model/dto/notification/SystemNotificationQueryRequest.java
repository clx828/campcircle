package com.caden.campcircle.model.dto.notification;

import com.caden.campcircle.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询系统通知请求
 *
 * @author caden
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SystemNotificationQueryRequest extends PageRequest implements Serializable {

    /**
     * 通知类型:1点赞,2评论,3关注,4系统
     */
    private Integer notificationType;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 关联ID(帖子ID等)
     */
    private Long relatedId;

    private static final long serialVersionUID = 1L;
}
