package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.mapper.PostCommentMapper;
import com.caden.campcircle.model.dto.postComment.PostCommentAddRequest;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.entity.PostComment;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.model.vo.PostCommentVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PostCommentService;
import com.caden.campcircle.service.PostService;
import com.caden.campcircle.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 *
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addComment(PostCommentAddRequest postCommitAddRequest, User loginUser) {
        PostComment postComment = new PostComment();
        // 1. 校验帖子是否存在
        Post post = postService.getById(postCommitAddRequest.getPostId());
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        }
        // 2. 校验用户是否存在
        if (loginUser == null|| loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 3. 如果是回复评论，校验父评论是否存在
        if (postCommitAddRequest.getParentId() == null || postCommitAddRequest.getParentId() <= 0) {
            postComment.setLevel(1);
        }else {
            PostComment parentComment = this.getById(postCommitAddRequest.getParentId());
            if (parentComment == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "父评论不存在");
            }
            postComment.setLevel(2);

        }

        BeanUtils.copyProperties(postCommitAddRequest, postComment);
        postComment.setUserId(loginUser.getId());
        // 4. 保存评论
        boolean result = this.save(postComment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论失败");
        }
        // 5. 更新帖子评论数
        postService.update()
                .eq("id", postComment.getPostId())
                .setSql("commentNum = commentNum + 1")
                .update();
        return postComment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(long id, long userId) {
        // 1. 校验评论是否存在
        PostComment postComment = this.getById(id);
        if (postComment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        // 2. 校验是否是评论作者
        if (!postComment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }
        // 3. 删除评论
        boolean result = this.removeById(id);
        if (result) {
            // 4. 更新帖子评论数
            postService.update()
                    .eq("id", postComment.getPostId())
                    .setSql("commentNum = commentNum - 1")
                    .update();
        }
        return result;
    }

    @Override
    public Page<PostCommentVO> listCommentByPage(long postId, long current, long pageSize) {
        // 1. 分页查询一级评论
        Page<PostComment> commentPage = this.page(
                new Page<>(current, pageSize),
                new QueryWrapper<PostComment>()
                        .eq("postId", postId)
                        .isNull("parentId")
                        .orderByDesc("createTime"));
        // 2. 获取所有评论的用户id
        List<PostComment> commentList = commentPage.getRecords();
        List<Long> userIdList = commentList.stream()
                .map(PostComment::getUserId)
                .collect(Collectors.toList());
        // 3. 查询所有评论的回复
        List<PostComment> replyList = this.list(
                new QueryWrapper<PostComment>()
                        .eq("postId", postId)
                        .in("parentId", commentList.stream().map(PostComment::getId).collect(Collectors.toList()))
                        .orderByAsc("createTime"));
        // 4. 获取回复的用户id
        userIdList.addAll(replyList.stream()
                .map(PostComment::getUserId)
                .collect(Collectors.toList()));
        userIdList.addAll(replyList.stream()
                .map(PostComment::getReplyUserId)
                .filter(id -> id != null)
                .collect(Collectors.toList()));
        // 5. 查询用户信息
        Map<Long, UserVO> userMap = userService.listByIds(userIdList).stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toMap(UserVO::getId, userVO -> userVO));
        // 6. 组装评论视图
        List<PostCommentVO> commentVOList = commentList.stream().map(comment -> {
            PostCommentVO commentVO = new PostCommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            commentVO.setUser(userMap.get(comment.getUserId()));
            // 设置回复列表
            List<PostCommentVO> replyVOList = replyList.stream()
                    .filter(reply -> reply.getParentId().equals(comment.getId()))
                    .map(reply -> {
                        PostCommentVO replyVO = new PostCommentVO();
                        BeanUtils.copyProperties(reply, replyVO);
                        replyVO.setUser(userMap.get(reply.getUserId()));
                        replyVO.setReplyUser(userMap.get(reply.getReplyUserId()));
                        return replyVO;
                    })
                    .collect(Collectors.toList());
            commentVO.setChildren(replyVOList);
            return commentVO;
        }).collect(Collectors.toList());
        // 7. 组装分页结果
        Page<PostCommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(),
                commentPage.getTotal());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }
}