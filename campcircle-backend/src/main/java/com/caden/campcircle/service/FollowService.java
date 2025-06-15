package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.entity.Follow;

/**
 * 关注服务
 *
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注用户
     *
     * @param userId       当前用户id
     * @param followUserId 被关注用户id
     * @return 关注结果
     */
    boolean doFollowInner(long userId, long followUserId);

    /**
     * 取消关注
     *
     * @param userId       当前用户id
     * @param followUserId 被关注用户id
     * @return 取消关注结果
     */
    boolean unFollow(long userId, long followUserId);

    /**
     * 是否已关注
     *
     * @param userId       当前用户id
     * @param followUserId 被关注用户id
     * @return 是否已关注
     */
    boolean hasFollow(long userId, long followUserId);
}