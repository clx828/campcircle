package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.common.PageRequest;
import com.caden.campcircle.model.entity.Follow;
import com.caden.campcircle.model.vo.FansVO;
import com.caden.campcircle.model.vo.UserStatisticsVO;
import com.caden.campcircle.model.vo.FollowVO;
import com.caden.campcircle.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * 获取关注列表
     *
     * @param userId 当前用户id
     * @return 关注列表
     */
    Page<FollowVO> getListFollowVO(Page<Follow> page,long userId);

    /**
     * 获取粉丝列表
     *
     * @param userId 当前用户id
     * @return 粉丝列表
     */
    Page<FansVO> getListFansVO(Page<Follow> page, long userId);

    /**
     * 获取用户统计信息（关注数、粉丝数、获赞数）
     *
     * @param id 用户id
     * @return 用户统计信息
     */
    UserStatisticsVO getUserStatistics(Long id);

    /**
     * 获取用户统计信息（兼容性方法）
     * @deprecated 请使用 getUserStatistics 方法
     */
    @Deprecated
    default UserStatisticsVO getFollowNum(Long id) {
        return getUserStatistics(id);
    }

    Page<PostVO> listMyFollowPost(PageRequest pageRequest, HttpServletRequest request);
}