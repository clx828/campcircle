package com.caden.campcircle.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.constant.MessageConstant;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.mapper.PrivateMessageMapper;
import com.caden.campcircle.model.dto.message.PrivateMessageQueryRequest;
import com.caden.campcircle.model.dto.message.PrivateMessageSendRequest;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ChatListVO;
import com.caden.campcircle.model.vo.PrivateMessageVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PrivateMessageService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {

    @Resource
    private UserService userService;

    @Override
    public Long sendMessage(PrivateMessageSendRequest privateMessageSendRequest, User loginUser) {
        Long toUserId = privateMessageSendRequest.getToUserId();
        String content = privateMessageSendRequest.getContent();
        String pictureUrl = privateMessageSendRequest.getPictureUrl();
        Integer messageType = privateMessageSendRequest.getMessageType();
        Long fromUserId = loginUser.getId();
        ThrowUtils.throwIf(toUserId == null || toUserId <= 0, ErrorCode.PARAMS_ERROR);
        //查询这个接收的用户是否存在
        User toUser = userService.getById(toUserId);
        ThrowUtils.throwIf(toUser == null, ErrorCode.PARAMS_ERROR, "目标用户不存在");
        ThrowUtils.throwIf(fromUserId.equals(toUserId), ErrorCode.PARAMS_ERROR, "不能给自己发消息");
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setFromUserId(fromUserId);
        privateMessage.setToUserId(toUserId);
        privateMessage.setContent(content);
        privateMessage.setMessageType(messageType);
        privateMessage.setPictureUrl(pictureUrl);
        privateMessage.setIsRead(0);
        boolean save = this.save(privateMessage);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "消息发送失败");

        log.info("私信消息保存成功: {}", privateMessage.getId());

        // TODO: 实现消息的实时推送
        // 1. WebSocket推送：如果用户在线，通过WebSocket实时推送消息
        // 2. 服务端事件推送(SSE)：作为WebSocket的备选方案
        // 3. 移动端推送：集成极光推送、友盟推送等第三方推送服务
        // 实现示例：
        // if (webSocketService.isUserOnline(toUserId)) {
        //     webSocketService.pushMessage(toUserId, messageVO);
        // } else {
        //     // 用户离线，发送推送通知
        //     pushNotificationService.sendMessageNotification(toUserId, fromUser.getUserName(), content);
        // }

        // TODO: 如果离线则使用微信的活动订阅功能
        // 1. 微信小程序订阅消息：用户授权后可发送模板消息
        // 2. 微信公众号模板消息：关注公众号的用户可接收模板消息
        // 3. 企业微信应用消息：企业内部用户消息推送
        // 实现思路：
        // - 用户首次使用时引导订阅消息模板
        // - 在用户离线且有新消息时，调用微信API发送订阅消息
        // - 消息内容包括：发送者昵称、消息预览、跳转链接等
        // 实现示例：
        // if (!webSocketService.isUserOnline(toUserId)) {
        //     User toUser = userService.getById(toUserId);
        //     if (StringUtils.isNotBlank(toUser.getOpenId())) {
        //         wxMpService.sendSubscribeMessage(toUser.getOpenId(),
        //             buildMessageTemplate(fromUser.getUserName(), content));
        //     }
        // }

        return privateMessage.getId();
    }

    @Override
    public Page<PrivateMessageVO> getChatHistory(PrivateMessageQueryRequest privateMessageQueryRequest, User loginUser) {
        Long chatUserId = privateMessageQueryRequest.getChatUserId();
        Long loginUserId = loginUser.getId();

        // 参数校验
        ThrowUtils.throwIf(chatUserId == null || chatUserId <= 0, ErrorCode.PARAMS_ERROR, "聊天对象ID不能为空");
        ThrowUtils.throwIf(chatUserId.equals(loginUserId), ErrorCode.PARAMS_ERROR, "不能查看与自己的聊天记录");

        // 验证聊天对象是否存在
        User chatUser = userService.getById(chatUserId);
        ThrowUtils.throwIf(chatUser == null, ErrorCode.PARAMS_ERROR, "聊天对象不存在");

        // 构建查询条件
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq("from_user_id", loginUserId).eq("to_user_id", chatUserId)
                .or()
                .eq("from_user_id", chatUserId).eq("to_user_id", loginUserId)
        );

        // 添加其他查询条件
        Integer messageType = privateMessageQueryRequest.getMessageType();
        if (messageType != null) {
            queryWrapper.eq("message_type", messageType);
        }

        Integer isRead = privateMessageQueryRequest.getIsRead();
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }

        // 按时间倒序排列
        queryWrapper.orderByDesc("create_time");

        // 分页查询
        long current = privateMessageQueryRequest.getCurrent();
        long size = privateMessageQueryRequest.getPageSize();
        Page<PrivateMessage> privateMessagePage = this.page(new Page<>(current, size), queryWrapper);

        // 转换为VO
        return getPrivateMessageVOPage(privateMessagePage);
    }

    @Override
    public List<ChatListVO> getChatList(User loginUser) {
        Long loginUserId = loginUser.getId();

        // 使用Mapper中的自定义方法获取聊天列表
        List<ChatListVO> chatListVOList = this.baseMapper.getChatList(loginUserId);

        if (CollUtil.isEmpty(chatListVOList)) {
            return chatListVOList;
        }

        // 获取所有聊天对象的用户ID
        Set<Long> userIds = chatListVOList.stream()
                .map(ChatListVO::getChatUserId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充用户信息
        chatListVOList.forEach(chatListVO -> {
            User chatUser = userMap.get(chatListVO.getChatUserId());
            if (chatUser != null) {
                UserVO userVO = userService.getUserVO(chatUser);
                chatListVO.setChatUser(userVO);
            }
        });

        return chatListVOList;
    }

    @Override
    public Boolean recallMessage(Long messageId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR, "消息ID不能为空");

        // 查询消息
        PrivateMessage privateMessage = this.getById(messageId);
        ThrowUtils.throwIf(privateMessage == null, ErrorCode.NOT_FOUND_ERROR, "消息不存在");

        // 权限校验：只有发送者可以撤回消息
        Long loginUserId = loginUser.getId();
        ThrowUtils.throwIf(!privateMessage.getFromUserId().equals(loginUserId),
                ErrorCode.NO_AUTH_ERROR, "只能撤回自己发送的消息");

        // 检查消息是否已经撤回
        ThrowUtils.throwIf(MessageConstant.MESSAGE_RECALLED.equals(privateMessage.getIsRecalled()),
                ErrorCode.PARAMS_ERROR, "消息已经撤回");

        // 检查撤回时间限制（例如：只能撤回2分钟内的消息）
        long currentTime = System.currentTimeMillis();
        long messageTime = privateMessage.getCreateTime().getTime();
        long timeDiff = currentTime - messageTime;
        long maxRecallTime = 2 * 60 * 1000; // 2分钟

        ThrowUtils.throwIf(timeDiff > maxRecallTime, ErrorCode.PARAMS_ERROR, "超过撤回时间限制");

        // 更新消息状态为已撤回
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId)
                .eq("from_user_id", loginUserId)
                .eq("is_recalled", MessageConstant.MESSAGE_NOT_RECALLED);
        updateWrapper.set("is_recalled", MessageConstant.MESSAGE_RECALLED);
        updateWrapper.set("update_time", new Date());

        boolean result = this.update(updateWrapper);

        if (result) {
            log.info("消息撤回成功，messageId: {}, userId: {}", messageId, loginUserId);

            // TODO: 通知接收者消息已撤回
            // messagePushService.pushMessageRecall(privateMessage.getToUserId(), messageId);
        }

        return result;
    }

    @Override
    public Boolean markAsRead(Long fromUserId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(fromUserId == null || fromUserId <= 0, ErrorCode.PARAMS_ERROR, "发送者ID不能为空");

        Long loginUserId = loginUser.getId();
        ThrowUtils.throwIf(fromUserId.equals(loginUserId), ErrorCode.PARAMS_ERROR, "不能标记自己发送的消息为已读");

        // 验证发送者是否存在
        User fromUser = userService.getById(fromUserId);
        ThrowUtils.throwIf(fromUser == null, ErrorCode.PARAMS_ERROR, "发送者不存在");

        // 使用Mapper中的自定义方法标记消息为已读
        Integer updateCount = this.baseMapper.markAsRead(fromUserId, loginUserId);

        if (updateCount > 0) {
            log.info("标记消息为已读成功，fromUserId: {}, toUserId: {}, 更新数量: {}",
                    fromUserId, loginUserId, updateCount);
        }

        return updateCount > 0;
    }

    @Override
    public Integer getUnreadCount(User loginUser) {
        Long loginUserId = loginUser.getId();

        // 查询当前用户接收到的所有未读消息数量
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("toUserId", loginUserId)
                .eq("isRead", MessageConstant.MESSAGE_UNREAD)
                .eq("isRecalled", MessageConstant.MESSAGE_NOT_RECALLED);

        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public PrivateMessageVO getPrivateMessageVO(PrivateMessage privateMessage) {
        if (privateMessage == null) {
            return null;
        }

        // 基础属性复制
        PrivateMessageVO privateMessageVO = new PrivateMessageVO();
        BeanUtils.copyProperties(privateMessage, privateMessageVO);

        // 填充发送者信息
        Long fromUserId = privateMessage.getFromUserId();
        if (fromUserId != null) {
            User fromUser = userService.getById(fromUserId);
            if (fromUser != null) {
                UserVO fromUserVO = userService.getUserVO(fromUser);
                privateMessageVO.setFromUser(fromUserVO);
            }
        }

        // 填充接收者信息
        Long toUserId = privateMessage.getToUserId();
        if (toUserId != null) {
            User toUser = userService.getById(toUserId);
            if (toUser != null) {
                UserVO toUserVO = userService.getUserVO(toUser);
                privateMessageVO.setToUser(toUserVO);
            }
        }

        return privateMessageVO;
    }

    @Override
    public Page<PrivateMessageVO> getPrivateMessageVOPage(Page<PrivateMessage> privateMessagePage) {
        List<PrivateMessage> privateMessageList = privateMessagePage.getRecords();
        Page<PrivateMessageVO> privateMessageVOPage = new Page<>(
                privateMessagePage.getCurrent(),
                privateMessagePage.getSize(),
                privateMessagePage.getTotal()
        );

        if (CollUtil.isEmpty(privateMessageList)) {
            return privateMessageVOPage;
        }

        // 获取所有相关的用户ID
        Set<Long> userIds = privateMessageList.stream()
                .flatMap(message -> List.of(message.getFromUserId(), message.getToUserId()).stream())
                .collect(Collectors.toSet());

        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO并填充用户信息
        List<PrivateMessageVO> privateMessageVOList = privateMessageList.stream().map(privateMessage -> {
            PrivateMessageVO privateMessageVO = new PrivateMessageVO();
            BeanUtils.copyProperties(privateMessage, privateMessageVO);

            // 填充发送者信息
            User fromUser = userMap.get(privateMessage.getFromUserId());
            if (fromUser != null) {
                UserVO fromUserVO = userService.getUserVO(fromUser);
                privateMessageVO.setFromUser(fromUserVO);
            }

            // 填充接收者信息
            User toUser = userMap.get(privateMessage.getToUserId());
            if (toUser != null) {
                UserVO toUserVO = userService.getUserVO(toUser);
                privateMessageVO.setToUser(toUserVO);
            }

            return privateMessageVO;
        }).collect(Collectors.toList());

        privateMessageVOPage.setRecords(privateMessageVOList);
        return privateMessageVOPage;
    }
}
