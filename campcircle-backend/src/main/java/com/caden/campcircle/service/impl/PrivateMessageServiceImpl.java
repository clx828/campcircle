package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.constant.MessageConstant;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.mapper.PrivateMessageMapper;
import com.caden.campcircle.model.dto.message.PrivateMessageQueryRequest;
import com.caden.campcircle.model.dto.message.PrivateMessageSendRequest;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ChatVO;
import com.caden.campcircle.model.vo.PrivateMessageVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PrivateMessageService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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



    /**
     * 获取私信列表
     *
     * @param loginUser 当前用户
     * @return 私信列表
     */
    @Override
    public List<ChatVO> getChatVOList(User loginUser) {
        List<Long> userIds = this.getBaseMapper().getChatUserIdList(loginUser.getId());
        if (userIds == null || userIds.isEmpty()){
            return new ArrayList<>();
        }
        log.info("userIds: {}", userIds);
        // 批量查询用户信息
        List<UserVO> userVOList = userService.getUserVO(userService.listByIds(userIds));
       List<ChatVO> chatVOList = userVOList.stream()
                .map(userVO ->{
                    ChatVO chatVO = new ChatVO();
                    chatVO.setChatUserId(userVO.getId());
                    chatVO.setChatUser(userVO);
                    chatVO.setLastMessage(this.getBaseMapper().getLatestMessage( userVO.getId(),loginUser.getId()));
                    chatVO.setUnreadCount(this.getBaseMapper().getUnreadCountByUser(userVO.getId(),loginUser.getId()));
                    return chatVO;
                }).collect(Collectors.toList());
       return chatVOList;
    }

    @Override
    public boolean markAsRead(Long Id, Long loginUserId) {
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",Id)
                .eq("toUserId",loginUserId)
                .set("isRead", MessageConstant.MESSAGE_READ);
        return this.update(updateWrapper);
    }

    @Override
    public boolean recallMessage(Long Id, Long loginUserId) {
        // 查询消息
        PrivateMessage privateMessage = this.getById(Id);
        if (privateMessage == null){
            throw  new BusinessException(ErrorCode.NOT_FOUND_ERROR,"消息不存在");
        }
        if (!privateMessage.getFromUserId().equals(loginUserId)){
            throw  new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        //如果消息发送超过2分钟则不能撤回
        if (System.currentTimeMillis() - privateMessage.getCreateTime().getTime() > 2 * 60 * 1000){
            throw  new BusinessException(ErrorCode.OPERATION_ERROR,"消息发送超过2分钟，不能撤回");
        }
        privateMessage.setIsRecalled(MessageConstant.MESSAGE_RECALLED);
        return this.updateById(privateMessage);
    }

    @Override
    public Long getUnreadCount(Long loginUserId) {
        return this.getBaseMapper().getAllUnreadCount(loginUserId);
    }

    @Override
    public Page<PrivateMessageVO> getChatHistory(PrivateMessageQueryRequest privateMessageQueryRequest, User loginUser) {
        Page<PrivateMessage> page = new Page<>(privateMessageQueryRequest.getCurrent(), privateMessageQueryRequest.getPageSize());
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fromUserId", loginUser.getId())
                .eq("toUserId", privateMessageQueryRequest.getChatUserId())
                .or()
                .eq("fromUserId", privateMessageQueryRequest.getChatUserId())
                .eq("toUserId", loginUser.getId())
                .orderByDesc("createTime");

        Page<PrivateMessage> privateMessagePage = this.page(page, queryWrapper);

        if(privateMessagePage.getTotal() == 0){
            return new Page<>();
        }

        // 构建用户VO的Map，私聊只涉及两个用户
        Map<Long, UserVO> userVOMap = new HashMap<>();
        userVOMap.put(loginUser.getId(), userService.getUserVO(loginUser));
        userVOMap.put(privateMessageQueryRequest.getChatUserId(),
                userService.getUserVO(userService.getById(privateMessageQueryRequest.getChatUserId())));

        // 转换为VO
        List<PrivateMessageVO> privateMessageVOList = privateMessagePage.getRecords().stream()
                .map(privateMessage -> {
                    PrivateMessageVO privateMessageVO = PrivateMessageVO.objToVo(privateMessage);
                    privateMessageVO.setFromUser(userVOMap.get(privateMessage.getFromUserId()));
                    privateMessageVO.setToUser(userVOMap.get(privateMessage.getToUserId()));
                    return privateMessageVO;
                })
                .collect(Collectors.toList());

        Page<PrivateMessageVO> privateMessageVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        privateMessageVOPage.setRecords(privateMessageVOList);

        // 将对方发送给当前用户的未读消息标记为已读
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("fromUserId", privateMessageQueryRequest.getChatUserId())
                .eq("toUserId", loginUser.getId())
                .eq("isRead", MessageConstant.MESSAGE_UNREAD)
                .set("isRead", MessageConstant.MESSAGE_READ);
        this.update(updateWrapper);

        log.info("用户 {} 查看与用户 {} 的聊天记录，已将未读消息标记为已读",
                loginUser.getId(), privateMessageQueryRequest.getChatUserId());

        return privateMessageVOPage;

    }
}
