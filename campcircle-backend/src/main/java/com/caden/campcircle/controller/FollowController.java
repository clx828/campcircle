package com.caden.campcircle.controller;

import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.service.FollowService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
}