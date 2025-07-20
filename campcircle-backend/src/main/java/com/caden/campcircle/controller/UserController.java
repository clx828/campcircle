package com.caden.campcircle.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.annotation.AuthCheck;
import com.caden.campcircle.common.*;
import com.caden.campcircle.config.WxMaConfiguration;
import com. caden.campcircle.config.WxOpenConfig;
import com. caden.campcircle.constant.UserConstant;
import com. caden.campcircle.exception.BusinessException;
import com. caden.campcircle.exception.ThrowUtils;
import com. caden.campcircle.model.dto.user.UserAddRequest;
import com. caden.campcircle.model.dto.user.UserLoginRequest;
import com. caden.campcircle.model.dto.user.UserQueryRequest;
import com. caden.campcircle.model.dto.user.UserRegisterRequest;
import com. caden.campcircle.model.dto.user.UserUpdateMyRequest;
import com. caden.campcircle.model.dto.user.UserUpdateRequest;
import com. caden.campcircle.model.entity.User;
import com. caden.campcircle.model.vo.LoginUserVO;
import com. caden.campcircle.model.vo.UserVO;
import com. caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com. caden.campcircle.service.impl.UserServiceImpl.SALT;

/**
 * 用户接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;
    @Resource
    private WxMaConfiguration wxMaConfiguration;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户通过账号密码注册")
    public BaseResponse<Long> userRegister(@RequestBody @ApiParam(value = "用户注册信息", required = true) UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request HTTP请求
     * @return 登录用户信息
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户通过账号密码登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody @ApiParam(value = "用户登录信息", required = true) UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录（微信开放平台）
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param code 微信授权码
     * @return 登录用户信息
     */
    @GetMapping("/login/wx_open")
    @ApiOperation(value = "微信开放平台登录", notes = "通过微信开放平台授权登录")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam("code") @ApiParam(value = "微信授权码", required = true) String code) {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }

    /**
     * 用户登录（微信小程序）
     *
     * @param request HTTP请求
     * @param code 微信小程序授权码
     * @return 登录用户信息
     */
    @GetMapping("/login/wx_miniapp")
    @ApiOperation(value = "微信小程序登录", notes = "通过微信小程序授权登录")
    public BaseResponse<LoginUserVO> userLoginByWxMiniapp(HttpServletRequest request,
                                                          @RequestParam("code") @ApiParam(value = "微信小程序授权码", required = true) String code) throws WxErrorException {
        log.info("code = {}", code);
        WxMaService wxMaService = wxMaConfiguration.getWxMaService();
        WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
        String openid = session.getOpenid();
        log.info("session = {}", session);
        if (StringUtils.isAnyBlank(openid)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录失败，系统错误");
        }
        return ResultUtils.success(userService.userLoginByMaOpen(session,request));
    }

    /**
     * 用户注销
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "用户退出登录")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求
     * @return 当前登录用户信息
     */
    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户", notes = "获取当前登录用户的详细信息")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest 用户创建请求
     * @param request HTTP请求
     * @return 用户ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "创建用户", notes = "管理员创建新用户（默认密码：12345678）")
    public BaseResponse<Long> addUser(@RequestBody @ApiParam(value = "用户创建信息", required = true) UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        String defaultPassword = "12345678";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "删除用户", notes = "管理员删除指定用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody @ApiParam(value = "删除用户信息", required = true) DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 用户更新请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "更新用户", notes = "管理员更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody @ApiParam(value = "用户更新信息", required = true) UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "根据ID获取用户", notes = "管理员根据用户ID获取用户详细信息")
    public BaseResponse<User> getUserById(@ApiParam(value = "用户ID", required = true) long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户包装类
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据ID获取用户包装类", notes = "获取用户的脱敏信息")
    public BaseResponse<UserVO> getUserVOById(@ApiParam(value = "用户ID", required = true) long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 用户查询请求
     * @param request HTTP请求
     * @return 用户分页列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取用户列表", notes = "管理员分页查询用户列表")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody @ApiParam(value = "用户查询条件", required = true) UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest 用户查询请求
     * @param request HTTP请求
     * @return 用户包装类分页列表
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取用户封装列表", notes = "分页查询用户脱敏信息列表")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody @ApiParam(value = "用户查询条件", required = true) UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 个人信息更新请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/update/my")
    @ApiOperation(value = "更新个人信息", notes = "用户更新自己的个人信息")
    public BaseResponse<Boolean> updateMyUser(@RequestBody @ApiParam(value = "个人信息更新", required = true) UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/search/by/keyword")
    @ApiOperation(value = "根据关键词搜索用户", notes = "根据关键词搜索用户")
    public BaseResponse<Page<UserVO>> searchUserByKeyword(@ApiParam(value = "关键词", required = true)PageSearchByKeyWord pageSearchByKeyWord, HttpServletRequest request) {
        if (pageSearchByKeyWord == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        return ResultUtils.success(userService.listUserVOByPage(pageSearchByKeyWord, request));
    }

}
