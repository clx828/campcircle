package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.PageRequest;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.mapper.FollowMapper;
import com.caden.campcircle.model.entity.*;
import com.caden.campcircle.model.vo.FansVO;
import com.caden.campcircle.model.vo.FollowNum;
import com.caden.campcircle.model.vo.FollowVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.service.FollowService;
import com.caden.campcircle.service.PostService;
import com.caden.campcircle.service.PostThumbService;
import com.caden.campcircle.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务实现
 *
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserService userService;
    @Resource
    private PostThumbService postThumbService;
    @Resource
    private PostService postService;

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

    @Override
    public Page<FollowVO> getListFollowVO(Page<Follow> page, long userId) {
        List<Follow> myFansList = this.list(new QueryWrapper<Follow>().eq("followUserId", userId));
        List<FollowVO> followVOList = page.getRecords().stream().map(follow -> {
            FollowVO followVO = new FollowVO();
            BeanUtils.copyProperties(follow, followVO);
            followVO.setIsMutual(myFansList.stream().anyMatch(myFan -> myFan.getUserId().equals(follow.getFollowUserId())));
            followVO.setFollowUserVO(userService.getUserVO(userService.getById(follow.getFollowUserId())));
            return followVO;
        }).collect(Collectors.toList());
        Page<FollowVO> followVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        followVOPage.setRecords(followVOList);
        return followVOPage;
    }

    @Override
    public Page<FansVO> getListFansVO(Page<Follow> page, long userId) {
        List<Follow> myFollowList = this.list(new QueryWrapper<Follow>().eq("userId", userId));
        List<FansVO> fansVOList = page.getRecords().stream().map(fans -> {
            FansVO fansVO = new FansVO();
            BeanUtils.copyProperties(fans, fansVO);
            fansVO.setFansUserId(fans.getUserId());
            fansVO.setUserId(fans.getFollowUserId());
            fansVO.setIsMutual(myFollowList.stream().anyMatch(myFollow -> myFollow.getFollowUserId().equals(fans.getUserId())));
            fansVO.setFansUserVO(userService.getUserVO(userService.getById(fans.getUserId())));
            return fansVO;
        }).collect(Collectors.toList());
        Page<FansVO> fansVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        fansVOPage.setRecords(fansVOList);
        return fansVOPage;
    }

    @Override
    public FollowNum getFollowNum(Long id) {
        FollowNum followNum = new FollowNum();
        followNum.setFollowNum(this.count(new QueryWrapper<Follow>().eq("userId", id)));
        followNum.setFansNum(this.count(new QueryWrapper<Follow>().eq("followUserId", id)));
        followNum.setThumbNum(postThumbService.count(new QueryWrapper<PostThumb>().eq("userId", id)));
        return followNum;
    }

    @Override
    public Page<PostVO> listMyFollowPost(PageRequest pageRequest, HttpServletRequest request) {
        //先获取所有的关注用户的 id
        User loginUser = userService.getLoginUser(request);
        List<Long> followUserIdList = this.list(new QueryWrapper<Follow>().eq("userId", loginUser.getId())).stream().map(Follow::getFollowUserId).collect(Collectors.toList());
        if (followUserIdList.isEmpty()) {
            return new Page<>();
        }
        Page<Post> postPage = postService.page(new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()), new QueryWrapper<Post>().in("userId", followUserIdList));
        Page<PostVO> postVOList = postService.getPostVOPage(postPage, request);
        return postVOList;
    }
}