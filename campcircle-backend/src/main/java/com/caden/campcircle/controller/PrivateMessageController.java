package com.caden.campcircle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.message.PrivateMessageQueryRequest;
import com.caden.campcircle.model.dto.message.PrivateMessageSendRequest;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ChatVO;
import com.caden.campcircle.model.vo.PrivateMessageVO;
import com.caden.campcircle.service.PrivateMessageService;
import com.caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 私信消息接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/message")
@Slf4j
@Api(tags = "私信消息管理")
public class PrivateMessageController {

    @Resource
    private UserService userService;

    @Resource
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送私信", notes = "向指定用户发送私信消息")
    public BaseResponse<Long> sendMessage(@RequestBody @ApiParam(value = "私信发送请求", required = true) PrivateMessageSendRequest privateMessageSendRequest, HttpServletRequest request) {
        if (privateMessageSendRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        Long messageId = privateMessageService.sendMessage(privateMessageSendRequest, loginUser);
        return ResultUtils.success(messageId);
    }

    @GetMapping("/get/chat/list")
    @ApiOperation(value = "获取聊天列表", notes = "获取当前用户的所有聊天会话列表")
    public BaseResponse<List<ChatVO>> getChatList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        List<ChatVO> chatList =  privateMessageService.getChatVOList(loginUser);
        return ResultUtils.success(chatList);
    }

    @GetMapping("/get/unread/count")
    @ApiOperation(value = "获取未读消息数量", notes = "获取当前用户的未读私信消息数量")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        Long unreadCount =  privateMessageService.getUnreadCount(loginUser.getId());
        return ResultUtils.success(unreadCount);
    }

    @PostMapping("/mark/as/read")
    @ApiOperation(value = "标记消息为已读", notes = "将指定消息标记为已读状态")
    public BaseResponse<Boolean> markAsRead(@RequestParam @ApiParam(value = "消息ID", required = true) Long Id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean result =  privateMessageService.markAsRead(Id, loginUser.getId());
        return ResultUtils.success(result);
    }
    @PostMapping("/recall")
    @ApiOperation(value = "撤回消息", notes = "撤回已发送的私信消息")
    public BaseResponse<Boolean> recallMessage(@RequestParam @ApiParam(value = "消息ID", required = true) Long Id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        boolean result =  privateMessageService.recallMessage(Id, loginUser.getId());
        return ResultUtils.success(result);
    }
    @PostMapping("/get/chatHistory")
    @ApiOperation(value = "获取聊天记录", notes = "分页获取与指定用户的聊天记录")
    public BaseResponse<Page<PrivateMessageVO>> getChatHistory(@RequestBody @ApiParam(value = "聊天记录查询请求", required = true) PrivateMessageQueryRequest privateMessageQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        if (privateMessageQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (privateMessageQueryRequest.getPageSize() > 30){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页大小不能超过30");
        }
        Page<PrivateMessageVO> result = privateMessageService.getChatHistory(privateMessageQueryRequest, loginUser);
        return ResultUtils.success(result);
    }

}
