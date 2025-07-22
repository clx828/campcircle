package com.caden.campcircle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.annotation.AuthCheck;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.DeleteRequest;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.constant.UserConstant;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.systemmessage.SystemMessageAddRequest;
import com.caden.campcircle.model.dto.systemmessage.SystemMessageQueryRequest;
import com.caden.campcircle.model.dto.systemmessage.SystemMessageUpdateRequest;
import com.caden.campcircle.model.entity.SystemMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.SystemMessageVO;
import com.caden.campcircle.service.SystemMessageService;
import com.caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统消息接口
 *
 */
@RestController
@RequestMapping("/system-message")
@Slf4j
@Api(tags = "系统消息管理")
public class SystemMessageController {

    @Resource
    private SystemMessageService systemMessageService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建系统消息（管理员发送通知）
     *
     * @param systemMessageAddRequest 系统消息创建请求
     * @param request HTTP请求
     * @return 消息ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "管理员发送通知", notes = "管理员创建系统消息，可发送给指定用户或全体用户")
    public BaseResponse<Long> addSystemMessage(@RequestBody @ApiParam(value = "系统消息信息", required = true) SystemMessageAddRequest systemMessageAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(systemMessageAddRequest == null, ErrorCode.PARAMS_ERROR);
        
        SystemMessage systemMessage = new SystemMessage();
        BeanUtils.copyProperties(systemMessageAddRequest, systemMessage);
        systemMessage.setFromUserId(0L); // 系统发送
        systemMessage.setStatus(0); // 未读
        
        // 数据校验
        systemMessageService.validSystemMessage(systemMessage, true);
        
        // 写入数据库
        boolean result = systemMessageService.save(systemMessage);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        return ResultUtils.success(systemMessage.getId());
    }

    /**
     * 删除系统消息
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "删除系统消息", notes = "管理员删除系统消息")
    public BaseResponse<Boolean> deleteSystemMessage(@RequestBody @ApiParam(value = "删除请求", required = true) DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        SystemMessage oldSystemMessage = systemMessageService.getById(id);
        ThrowUtils.throwIf(oldSystemMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = systemMessageService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新系统消息（仅管理员可用）
     *
     * @param systemMessageUpdateRequest 系统消息更新请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "更新系统消息", notes = "管理员更新系统消息信息")
    public BaseResponse<Boolean> updateSystemMessage(@RequestBody @ApiParam(value = "系统消息更新信息", required = true) SystemMessageUpdateRequest systemMessageUpdateRequest) {
        if (systemMessageUpdateRequest == null || systemMessageUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SystemMessage systemMessage = new SystemMessage();
        BeanUtils.copyProperties(systemMessageUpdateRequest, systemMessage);
        // 参数校验
        systemMessageService.validSystemMessage(systemMessage, false);
        long id = systemMessageUpdateRequest.getId();
        // 判断是否存在
        SystemMessage oldSystemMessage = systemMessageService.getById(id);
        ThrowUtils.throwIf(oldSystemMessage == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = systemMessageService.updateById(systemMessage);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取系统消息（封装类）
     *
     * @param id 消息ID
     * @return 系统消息信息
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据ID获取系统消息", notes = "根据消息ID获取系统消息详细信息")
    public BaseResponse<SystemMessageVO> getSystemMessageVOById(@ApiParam(value = "消息ID", required = true) long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SystemMessage systemMessage = systemMessageService.getById(id);
        ThrowUtils.throwIf(systemMessage == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(systemMessageService.getSystemMessageVO(systemMessage, request));
    }

    /**
     * 分页获取系统消息列表（按时间最近的顺序）
     *
     * @param systemMessageQueryRequest 查询请求
     * @param request HTTP请求
     * @return 系统消息分页列表
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取系统消息", notes = "分页查询系统消息列表，按时间最近的顺序排列")
    public BaseResponse<Page<SystemMessageVO>> listSystemMessageVOByPage(@RequestBody @ApiParam(value = "查询条件", required = true) SystemMessageQueryRequest systemMessageQueryRequest,
            HttpServletRequest request) {
        long current = systemMessageQueryRequest.getCurrent();
        long size = systemMessageQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 如果不是管理员，只能查看自己的消息
        if (!userService.isAdmin(loginUser)) {
            systemMessageQueryRequest.setToUserId(loginUser.getId());
        }
        
        Page<SystemMessage> systemMessagePage = systemMessageService.page(new Page<>(current, size),
                systemMessageService.getQueryWrapper(systemMessageQueryRequest));
        return ResultUtils.success(systemMessageService.getSystemMessageVOPage(systemMessagePage, request));
    }

    /**
     * 获取我的消息列表
     *
     * @param systemMessageQueryRequest 查询请求
     * @param request HTTP请求
     * @return 我的消息分页列表
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "获取我的消息", notes = "获取当前用户的消息列表，按时间最近的顺序排列")
    public BaseResponse<Page<SystemMessageVO>> listMySystemMessageVOByPage(@RequestBody @ApiParam(value = "查询条件", required = true) SystemMessageQueryRequest systemMessageQueryRequest,
            HttpServletRequest request) {
        if (systemMessageQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        systemMessageQueryRequest.setToUserId(loginUser.getId());
        long current = systemMessageQueryRequest.getCurrent();
        long size = systemMessageQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<SystemMessage> systemMessagePage = systemMessageService.page(new Page<>(current, size),
                systemMessageService.getQueryWrapper(systemMessageQueryRequest));
        return ResultUtils.success(systemMessageService.getSystemMessageVOPage(systemMessagePage, request));
    }

    // endregion

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 是否标记成功
     */
    @PostMapping("/mark-read/{messageId}")
    @ApiOperation(value = "标记消息为已读", notes = "将指定消息标记为已读状态")
    public BaseResponse<Boolean> markAsRead(@PathVariable @ApiParam(value = "消息ID", required = true) Long messageId, HttpServletRequest request) {
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        boolean result = systemMessageService.markAsRead(messageId, loginUser.getId());
        
        return ResultUtils.success(result);
    }

    /**
     * 批量标记消息为已读
     *
     * @param type 消息类型（可选）
     * @param request HTTP请求
     * @return 标记成功的数量
     */
    @PostMapping("/mark-all-read")
    @ApiOperation(value = "批量标记消息为已读", notes = "将指定类型的所有未读消息标记为已读，不指定类型则标记所有未读消息")
    public BaseResponse<Integer> markAllAsRead(@RequestParam(required = false) @ApiParam(value = "消息类型") Integer type, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int count = systemMessageService.markAllAsRead(loginUser.getId(), type);
        
        return ResultUtils.success(count);
    }

    /**
     * 获取未读消息数量
     *
     * @param request HTTP请求
     * @return 未读消息统计
     */
    @GetMapping("/unread-count")
    @ApiOperation(value = "获取未读消息数量", notes = "获取当前用户各类型未读消息的数量统计")
    public BaseResponse<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        
        Map<String, Long> unreadCount = new HashMap<>();
        unreadCount.put("total", systemMessageService.getUnreadCount(userId, null));
        unreadCount.put("system", systemMessageService.getUnreadCount(userId, 0));
        unreadCount.put("thumb", systemMessageService.getUnreadCount(userId, 1));
        unreadCount.put("favour", systemMessageService.getUnreadCount(userId, 2));
        unreadCount.put("comment", systemMessageService.getUnreadCount(userId, 3));
        unreadCount.put("follow", systemMessageService.getUnreadCount(userId, 4));
        
        return ResultUtils.success(unreadCount);
    }

    /**
     * 发送系统通知（管理员专用）
     *
     * @param title 消息标题
     * @param content 消息内容
     * @param toUserId 接收用户ID（可选，为空时发送给所有用户）
     * @param request HTTP请求
     * @return 是否发送成功
     */
    @PostMapping("/send-notification")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "发送系统通知", notes = "管理员发送系统通知，可指定用户或发送给所有用户")
    public BaseResponse<Boolean> sendSystemNotification(
            @RequestParam @ApiParam(value = "消息标题", required = true) String title,
            @RequestParam @ApiParam(value = "消息内容", required = true) String content,
            @RequestParam(required = false) @ApiParam(value = "接收用户ID") Long toUserId,
            HttpServletRequest request) {
        
        ThrowUtils.throwIf(title == null || title.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "标题不能为空");
        ThrowUtils.throwIf(content == null || content.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "内容不能为空");
        
        boolean result = systemMessageService.sendSystemNotification(title, content, toUserId);
        
        return ResultUtils.success(result);
    }
}
