package com.caden.campcircle.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.PageRequest;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.entity.Follow;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.FansVO;
import com.caden.campcircle.model.vo.FollowNum;
import com.caden.campcircle.model.vo.FollowVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.service.FollowService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 关注接口
 *
 */
@RestController
@RequestMapping("/follow")
@Slf4j
public class FollowController {

    @Resource
    private FollowService followService;

    @Resource
    private UserService userService;

    /**
     * 关注/取消关注用户
     *
     * @param followUserId 被关注用户id
     * @param request
     * @return
     */
    @PostMapping("/")
    public BaseResponse<Boolean> doFollow( @RequestParam Long followUserId, HttpServletRequest request) {
        if (followUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = followService.doFollowInner(loginUser.getId(), followUserId);
        return ResultUtils.success(result);
    }

    /**
     * 是否已关注
     *
     * @param followUserId 被关注用户id
     * @param request
     * @return
     */
    @GetMapping("/has")
    public BaseResponse<Boolean> hasFollow(long followUserId, HttpServletRequest request) {
        if (followUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = followService.hasFollow(loginUser.getId(), followUserId);
        return ResultUtils.success(result);
    }
    /**
     * 获取已关注列表
     *
     * @param request
     * @return
     */
    @GetMapping("/my/follow/list")
    public BaseResponse<Page<FollowVO>> listMyFollow(PageRequest pageRequest, HttpServletRequest request) {
        if (pageRequest == null || pageRequest.getCurrent() < 1 || pageRequest.getPageSize() < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (pageRequest.getPageSize() > 100){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误，不能超过20条");
        }
        User loginUser = userService.getLoginUser(request);
        //查询顺序需要时间的倒序
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.orderByDesc("createTime");
        Page<Follow> followPage = followService.page(new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()), queryWrapper);
        Page<FollowVO> result = followService.getListFollowVO(followPage, loginUser.getId());
        return ResultUtils.success(result);
    }
    /**
     * 获取粉丝列表
     *
     * @param request
     * @return
     */
    @GetMapping("/my/fans/list")
    public BaseResponse<Page<FansVO>> listMyFans(PageRequest pageRequest, HttpServletRequest request) {
        if (pageRequest == null || pageRequest.getCurrent() < 1 || pageRequest.getPageSize() < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (pageRequest.getPageSize() > 100){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误，不能超过20条");
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followUserId", loginUser.getId());
        queryWrapper.orderByDesc("createTime");
        Page<Follow> fansPage = followService.page(new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize()),queryWrapper);
        Page<FansVO> result = followService.getListFansVO(fansPage, loginUser.getId());
        return ResultUtils.success(result);
    }
    /**
     * 获取关注数和粉丝数
     *
     * @param request
     * @return
     */
    @GetMapping("/get/my/num")
    public BaseResponse<FollowNum> getFollowNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        FollowNum followNum = followService.getFollowNum(loginUser.getId());
        return ResultUtils.success(followNum);
    }

    /**
     * 获取关注用户的帖子列表
     *
     * @param request
     * @param pageRequest
     * @return
     */
    @GetMapping("/my/follow/post/list")
    public BaseResponse<Page<PostVO>> listMyFollowPost(PageRequest pageRequest, HttpServletRequest request) {
        if (pageRequest == null || pageRequest.getCurrent() < 1 || pageRequest.getPageSize() < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (pageRequest.getPageSize() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误，不能超过20条");
        }
        User loginUser = userService.getLoginUser(request);
        Page<PostVO> result = followService.listMyFollowPost(pageRequest, request);
        return ResultUtils.success(result);
    }
}