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
import com.caden.campcircle.model.vo.UserStatisticsVO;
import com.caden.campcircle.model.vo.FollowVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.service.FollowService;
import com.caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 关注接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/follow")
@Slf4j
@Api(tags = "关注管理")
public class FollowController {

    @Resource
    private FollowService followService;

    @Resource
    private UserService userService;

    /**
     * 关注/取消关注用户
     *
     * @param followUserId 被关注用户id
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/")
    @ApiOperation(value = "关注/取消关注用户", notes = "关注或取消关注指定用户")
    public BaseResponse<Boolean> doFollow(@RequestParam @ApiParam(value = "被关注用户ID", required = true) Long followUserId, HttpServletRequest request) {
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
     * 获取我的统计信息（关注数、粉丝数、获赞数）
     *
     * @param request HTTP请求
     * @return 用户统计信息
     */
    @GetMapping("/get/my/statistics")
    @ApiOperation(value = "获取我的统计信息", notes = "获取当前用户的关注数、粉丝数、获赞数")
    public BaseResponse<UserStatisticsVO> getMyStatistics(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserStatisticsVO userStatistics = followService.getUserStatistics(loginUser.getId());
        return ResultUtils.success(userStatistics);
    }

    /**
     * 获取指定用户的统计信息（关注数、粉丝数、获赞数）
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    @GetMapping("/get/user/statistics")
    @ApiOperation(value = "获取指定用户的统计信息", notes = "获取指定用户的关注数、粉丝数、获赞数")
    public BaseResponse<UserStatisticsVO> getUserStatistics(@RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserStatisticsVO userStatistics = followService.getUserStatistics(userId);
        return ResultUtils.success(userStatistics);
    }

    /**
     * 检查是否关注了指定用户
     *
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 是否关注
     */
    @GetMapping("/check/status")
    @ApiOperation(value = "检查是否关注了指定用户", notes = "检查当前登录用户是否关注了指定用户")
    public BaseResponse<Boolean> checkFollowStatus(@RequestParam @ApiParam(value = "用户ID", required = true) Long userId, HttpServletRequest request) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = followService.hasFollow(loginUser.getId(), userId);
        return ResultUtils.success(result);
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

    // ========== 兼容性接口（已废弃，建议使用新接口） ==========

    /**
     * 获取我的统计信息（兼容性接口）
     * @deprecated 请使用 /get/my/statistics 接口
     */
    @GetMapping("/get/my/num")
    @ApiOperation(value = "获取我的统计信息（已废弃）", notes = "已废弃，请使用 /get/my/statistics 接口")
    @Deprecated
    public BaseResponse<UserStatisticsVO> getFollowNum(HttpServletRequest request) {
        return getMyStatistics(request);
    }

    /**
     * 获取指定用户的统计信息（兼容性接口）
     * @deprecated 请使用 /get/user/statistics 接口
     */
    @GetMapping("/get/user/num")
    @ApiOperation(value = "获取指定用户的统计信息（已废弃）", notes = "已废弃，请使用 /get/user/statistics 接口")
    @Deprecated
    public BaseResponse<UserStatisticsVO> getUserFollowNum(@RequestParam @ApiParam(value = "用户ID", required = true) Long userId) {
        return getUserStatistics(userId);
    }
}