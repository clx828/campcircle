package com.caden.campcircle.model.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import com.caden.campcircle.model.entity.SystemNotification;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统通知视图对象
 *
 * @author caden
 */
@Data
public class SystemNotificationVO implements Serializable {

    /**
     * id
     */
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

    /**
     * 关联帖子信息（如果有）
     */
    private PostVO relatedPost;

    /**
     * 对象转包装类
     *
     * @param systemNotification
     * @return
     */
    public static SystemNotificationVO objToVo(SystemNotification systemNotification) {
        if (systemNotification == null) {
            return null;
        }
        SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
        BeanUtils.copyProperties(systemNotification, systemNotificationVO);
        return systemNotificationVO;
    }

    private static final long serialVersionUID = 1L;
}
