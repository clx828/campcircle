package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.mapper.SystemMessageMapper;
import com.caden.campcircle.model.dto.systemmessage.SystemMessageQueryRequest;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.entity.SystemMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.model.vo.SystemMessageVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PostService;
import com.caden.campcircle.service.SystemMessageService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统消息服务实现
 *
 */
@Service
@Slf4j
public class SystemMessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage>
        implements SystemMessageService {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Override
    public void validSystemMessage(SystemMessage systemMessage, boolean add) {
        if (systemMessage == null) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage());
        }
        String title = systemMessage.getTitle();
        String content = systemMessage.getContent();
        Integer type = systemMessage.getType();
        Long toUserId = systemMessage.getToUserId();
        Integer isGlobal = systemMessage.getIsGlobal();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(type == null, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(isGlobal == null, ErrorCode.PARAMS_ERROR);
            
            // 如果不是全局消息，必须指定接收用户
            if (isGlobal != 1) {
                ThrowUtils.throwIf(toUserId == null, ErrorCode.PARAMS_ERROR);
            }
        }
        
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage() + "，标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage() + "，内容过长");
        }
        if (type != null && (type < 0 || type > 4)) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage() + "，消息类型不正确");
        }
    }

    @Override
    public QueryWrapper<SystemMessage> getQueryWrapper(SystemMessageQueryRequest systemMessageQueryRequest) {
        QueryWrapper<SystemMessage> queryWrapper = new QueryWrapper<>();
        if (systemMessageQueryRequest == null) {
            return queryWrapper;
        }
        Long id = systemMessageQueryRequest.getId();
        Long fromUserId = systemMessageQueryRequest.getFromUserId();
        Long toUserId = systemMessageQueryRequest.getToUserId();
        String title = systemMessageQueryRequest.getTitle();
        Integer type = systemMessageQueryRequest.getType();
        Long postId = systemMessageQueryRequest.getPostId();
        Long commentId = systemMessageQueryRequest.getCommentId();
        Integer status = systemMessageQueryRequest.getStatus();
        Integer isGlobal = systemMessageQueryRequest.getIsGlobal();
        String sortField = systemMessageQueryRequest.getSortField();
        String sortOrder = systemMessageQueryRequest.getSortOrder();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(fromUserId != null, "fromUserId", fromUserId);
        queryWrapper.eq(toUserId != null, "toUserId", toUserId);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.eq(type != null, "type", type);
        queryWrapper.eq(postId != null, "postId", postId);
        queryWrapper.eq(commentId != null, "commentId", commentId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(isGlobal != null, "isGlobal", isGlobal);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals("ascend"), sortField);
        // 默认按创建时间倒序排列
        if (StringUtils.isBlank(sortField)) {
            queryWrapper.orderByDesc("createTime");
        }
        return queryWrapper;
    }

    @Override
    public SystemMessageVO getSystemMessageVO(SystemMessage systemMessage, HttpServletRequest request) {
        SystemMessageVO systemMessageVO = new SystemMessageVO();
        BeanUtils.copyProperties(systemMessage, systemMessageVO);
        
        // 设置消息类型描述
        systemMessageVO.setTypeDesc(getTypeDesc(systemMessage.getType()));
        
        // 设置状态描述
        systemMessageVO.setStatusDesc(getStatusDesc(systemMessage.getStatus()));
        
        // 获取发送用户信息
        if (systemMessage.getFromUserId() != null && systemMessage.getFromUserId() > 0) {
            User fromUser = userService.getById(systemMessage.getFromUserId());
            UserVO fromUserVO = userService.getUserVO(fromUser);
            systemMessageVO.setFromUser(fromUserVO);
        }
        
        // 获取接收用户信息
        if (systemMessage.getToUserId() != null) {
            User toUser = userService.getById(systemMessage.getToUserId());
            UserVO toUserVO = userService.getUserVO(toUser);
            systemMessageVO.setToUser(toUserVO);
        }
        
        // 获取关联帖子信息
        if (systemMessage.getPostId() != null) {
            Post post = postService.getById(systemMessage.getPostId());
            if (post != null) {
                PostVO postVO = postService.getPostVO(post, request);
                systemMessageVO.setPost(postVO);
            }
        }
        
        return systemMessageVO;
    }

    @Override
    public Page<SystemMessageVO> getSystemMessageVOPage(Page<SystemMessage> systemMessagePage, HttpServletRequest request) {
        List<SystemMessage> systemMessageList = systemMessagePage.getRecords();
        Page<SystemMessageVO> systemMessageVOPage = new Page<>(systemMessagePage.getCurrent(), systemMessagePage.getSize(), systemMessagePage.getTotal());
        if (systemMessageList.isEmpty()) {
            return systemMessageVOPage;
        }
        
        // 1. 关联查询用户信息
        Set<Long> userIdSet = systemMessageList.stream()
                .flatMap(systemMessage -> {
                    return java.util.stream.Stream.of(systemMessage.getFromUserId(), systemMessage.getToUserId());
                })
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 2. 关联查询帖子信息
        Set<Long> postIdSet = systemMessageList.stream()
                .map(SystemMessage::getPostId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, List<Post>> postIdPostListMap = postService.listByIds(postIdSet).stream()
                .collect(Collectors.groupingBy(Post::getId));
        
        // 3. 填充信息
        List<SystemMessageVO> systemMessageVOList = systemMessageList.stream().map(systemMessage -> {
            SystemMessageVO systemMessageVO = new SystemMessageVO();
            BeanUtils.copyProperties(systemMessage, systemMessageVO);
            
            // 设置消息类型描述
            systemMessageVO.setTypeDesc(getTypeDesc(systemMessage.getType()));
            
            // 设置状态描述
            systemMessageVO.setStatusDesc(getStatusDesc(systemMessage.getStatus()));
            
            // 设置发送用户信息
            if (systemMessage.getFromUserId() != null && systemMessage.getFromUserId() > 0) {
                List<User> userList = userIdUserListMap.get(systemMessage.getFromUserId());
                if (userList != null && !userList.isEmpty()) {
                    UserVO fromUserVO = userService.getUserVO(userList.get(0));
                    systemMessageVO.setFromUser(fromUserVO);
                }
            }
            
            // 设置接收用户信息
            if (systemMessage.getToUserId() != null) {
                List<User> userList = userIdUserListMap.get(systemMessage.getToUserId());
                if (userList != null && !userList.isEmpty()) {
                    UserVO toUserVO = userService.getUserVO(userList.get(0));
                    systemMessageVO.setToUser(toUserVO);
                }
            }
            
            // 设置关联帖子信息
            if (systemMessage.getPostId() != null) {
                List<Post> postList = postIdPostListMap.get(systemMessage.getPostId());
                if (postList != null && !postList.isEmpty()) {
                    PostVO postVO = postService.getPostVO(postList.get(0), request);
                    systemMessageVO.setPost(postVO);
                }
            }
            
            return systemMessageVO;
        }).collect(Collectors.toList());
        
        systemMessageVOPage.setRecords(systemMessageVOList);
        return systemMessageVOPage;
    }

    @Override
    public boolean sendSystemNotification(String title, String content, Long toUserId) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setFromUserId(0L); // 系统发送
        systemMessage.setTitle(title);
        systemMessage.setContent(content);
        systemMessage.setType(0); // 系统通知
        systemMessage.setStatus(0); // 未读
        
        if (toUserId == null) {
            // 全局消息，发送给所有用户
            systemMessage.setIsGlobal(1);
            systemMessage.setToUserId(null);
            return this.save(systemMessage);
        } else {
            // 发送给指定用户
            systemMessage.setIsGlobal(0);
            systemMessage.setToUserId(toUserId);
            return this.save(systemMessage);
        }
    }

    @Override
    public boolean sendThumbNotification(Long fromUserId, Long toUserId, Long postId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setFromUserId(fromUserId);
        systemMessage.setToUserId(toUserId);
        systemMessage.setTitle("点赞通知");
        systemMessage.setContent("有人点赞了您的帖子");
        systemMessage.setType(1); // 点赞通知
        systemMessage.setPostId(postId);
        systemMessage.setStatus(0); // 未读
        systemMessage.setIsGlobal(0);
        
        return this.save(systemMessage);
    }

    @Override
    public boolean sendFavourNotification(Long fromUserId, Long toUserId, Long postId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setFromUserId(fromUserId);
        systemMessage.setToUserId(toUserId);
        systemMessage.setTitle("收藏通知");
        systemMessage.setContent("有人收藏了您的帖子");
        systemMessage.setType(2); // 收藏通知
        systemMessage.setPostId(postId);
        systemMessage.setStatus(0); // 未读
        systemMessage.setIsGlobal(0);
        
        return this.save(systemMessage);
    }

    @Override
    public boolean sendCommentNotification(Long fromUserId, Long toUserId, Long postId, Long commentId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setFromUserId(fromUserId);
        systemMessage.setToUserId(toUserId);
        systemMessage.setTitle("评论通知");
        systemMessage.setContent("有人评论了您的帖子");
        systemMessage.setType(3); // 评论通知
        systemMessage.setPostId(postId);
        systemMessage.setCommentId(commentId);
        systemMessage.setStatus(0); // 未读
        systemMessage.setIsGlobal(0);
        
        return this.save(systemMessage);
    }

    @Override
    public boolean sendFollowNotification(Long fromUserId, Long toUserId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setFromUserId(fromUserId);
        systemMessage.setToUserId(toUserId);
        systemMessage.setTitle("关注通知");
        systemMessage.setContent("有人关注了您");
        systemMessage.setType(4); // 关注通知
        systemMessage.setStatus(0); // 未读
        systemMessage.setIsGlobal(0);
        
        return this.save(systemMessage);
    }

    @Override
    public boolean markAsRead(Long messageId, Long userId) {
        UpdateWrapper<SystemMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId);
        updateWrapper.eq("toUserId", userId);
        updateWrapper.set("status", 1);
        
        return this.update(updateWrapper);
    }

    @Override
    public int markAllAsRead(Long userId, Integer type) {
        UpdateWrapper<SystemMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("toUserId", userId);
        updateWrapper.eq("status", 0); // 只更新未读的
        if (type != null) {
            updateWrapper.eq("type", type);
        }
        updateWrapper.set("status", 1);
        
        return this.getBaseMapper().update(null, updateWrapper);
    }

    @Override
    public long getUnreadCount(Long userId, Integer type) {
        QueryWrapper<SystemMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("toUserId", userId);
        queryWrapper.eq("status", 0); // 未读
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        queryWrapper.eq("isDelete", false);
        
        return this.count(queryWrapper);
    }

    /**
     * 获取消息类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) {
            return "";
        }
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(0, "系统通知");
        typeMap.put(1, "点赞通知");
        typeMap.put(2, "收藏通知");
        typeMap.put(3, "评论通知");
        typeMap.put(4, "关注通知");
        return typeMap.getOrDefault(type, "未知类型");
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return status == 0 ? "未读" : "已读";
    }
}
