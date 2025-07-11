package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FollowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long followUserId;
    private UserVO followUserVO;
    // 是否相互关注
    private Boolean isMutual;
    private Date createTime;
    private Date updateTime;
}
