package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.mapper.FollowMapper;
import com.caden.campcircle.model.entity.Follow;
import com.caden.campcircle.model.entity.PostFavour;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.service.FollowService;
import com.caden.campcircle.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 关注服务实现
 *
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doFollowInner(long userId, long followUserId) {
        // 1. 校验不能关注自己
        if (userId == followUserId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }

        // 2. 校验用户是否存在
        User followUser = userService.getById(followUserId);
        if (followUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 3. 检查是否已经关注
        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("userId", userId).eq("followUserId", followUserId);
        Follow existingFollow = this.getOne(followQueryWrapper);

        if (existingFollow != null) {
            // 已关注，执行取消关注
            boolean removeResult = this.remove(followQueryWrapper);
            if (removeResult) {
                // 更新计数：关注数-1，粉丝数-1
                userService.update()
                        .eq("id", userId)
                        .setSql("followNum = followNum - 1")
                        .update();
                userService.update()
                        .eq("id", followUserId)
                        .setSql("fansNum = fansNum - 1")
                        .update();
            }
            return removeResult;
        } else {
            // 未关注，执行关注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);

            boolean saveResult = this.save(follow);
            if (saveResult) {
                // 更新计数：关注数+1，粉丝数+1
                userService.update()
                        .eq("id", userId)
                        .setSql("followNum = followNum + 1")
                        .update();
                userService.update()
                        .eq("id", followUserId)
                        .setSql("fansNum = fansNum + 1")
                        .update();
            }
            return saveResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unFollow(long userId, long followUserId) {
        // 1. 校验是否已关注
        if (!hasFollow(userId, followUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未关注");
        }
        // 2. 删除关注记录
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("followUserId", followUserId);
        boolean result = this.remove(queryWrapper);
        if (result) {
            // 3. 更新用户关注数和粉丝数
            userService.update()
                    .eq("id", userId)
                    .setSql("followNum = followNum - 1")
                    .update();
            userService.update()
                    .eq("id", followUserId)
                    .setSql("fansNum = fansNum - 1")
                    .update();
        }
        return result;
    }

    @Override
    public boolean hasFollow(long userId, long followUserId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("followUserId", followUserId);
        return this.count(queryWrapper) > 0;
    }
}