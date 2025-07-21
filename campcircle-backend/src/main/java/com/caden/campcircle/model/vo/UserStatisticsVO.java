package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计信息VO
 * 包含关注数、粉丝数、获赞数等统计数据
 */
@Data
public class UserStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 粉丝数
     */
    private Long followersCount;

    /**
     * 获赞数
     */
    private Long likesCount;

}
