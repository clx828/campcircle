package com.caden.campcircle.model.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import com.caden.campcircle.model.entity.PrivateMessage;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信消息视图对象
 *
 * @author caden
 */
@Data
public class PrivateMessageVO implements Serializable {

    /**
     * id
     */
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
     * 发送者信息
     */
    private UserVO fromUser;

    /**
     * 接收者信息
     */
    private UserVO toUser;

    /**
     * 对象转包装类
     *
     * @param privateMessage
     * @return
     */
    public static PrivateMessageVO objToVo(PrivateMessage privateMessage) {
        if (privateMessage == null) {
            return null;
        }
        PrivateMessageVO privateMessageVO = new PrivateMessageVO();
        BeanUtils.copyProperties(privateMessage, privateMessageVO);
        return privateMessageVO;
    }

    private static final long serialVersionUID = 1L;
}
