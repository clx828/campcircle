package com.caden.campcircle.model.dto.systemmessage;

import com.caden.campcircle.common.PageRequest;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统消息查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SystemMessageQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 发送用户 id
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
     * 消息类型：0-系统通知，1-点赞通知，2-收藏通知，3-评论通知，4-关注通知
     */
    private Integer type;

    /**
     * 消息类型列表：支持查询多个类型的消息
     */
    private List<Integer> types;

    /**
     * 关联的帖子ID
     */
    private Long postId;

    /**
     * 关联的评论ID
     */
    private Long commentId;

    /**
     * 状态：1-未读，0-已读
     */
    private Integer status;

    /**
     * 是否为全局消息：0-否，1-是
     */
    private Integer isGlobal;

    private static final long serialVersionUID = 1L;
}
