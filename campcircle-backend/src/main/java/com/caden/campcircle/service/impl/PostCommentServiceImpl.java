package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.mapper.PostCommentMapper;
import com.caden.campcircle.model.dto.postComment.PostCommentAddRequest;
import com.caden.campcircle.model.dto.postComment.PostCommentQueryRequest;
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
import java.util.*;
import java.util.function.Function;
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

        // 检查帖子是否公开，如果不公开，只有帖子作者可以评论
        if (post.getIsPublic() != null && post.getIsPublic() == 0) {
            // 帖子不公开，只有作者本人可以评论
            if (!post.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该帖子不公开，无法评论");
            }
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
    public Page<PostCommentVO> listCommentByPage(PostCommentQueryRequest postCommentQueryRequest) {
        // 1. 分页查询一级评论
        Page<PostComment> commentPage = this.page(
                new Page<>(postCommentQueryRequest.getCurrent(), postCommentQueryRequest.getPageSize()),
                new QueryWrapper<PostComment>()
                        .eq("postId", postCommentQueryRequest.getPostId())
                        .eq("level", 1)
                        .orderByDesc("createTime"));

        List<PostComment> commentList = commentPage.getRecords();
        if (commentList.isEmpty()) {
            return  new Page<>(postCommentQueryRequest.getCurrent(), postCommentQueryRequest.getPageSize(),0);
        }
        //查询所有二级评论（回复的评论）
        List<PostComment> replyList = this.list(
                new QueryWrapper<PostComment>()
                        .eq("postId", postCommentQueryRequest.getPostId())
                        .eq("level", 2)
                        .orderByAsc("createTime"));

        // 3. 使用Set收集所有用户ID，自动去重
        Set<Long> userIdSet = new HashSet<>();

        // 添加评论用户ID
        commentList.stream()
                .map(PostComment::getUserId)
                .forEach(userIdSet::add);

        // 添加回复用户ID和被回复用户ID
        replyList.forEach(reply -> {
            userIdSet.add(reply.getUserId());
            if (reply.getReplyUserId() != null) {
                userIdSet.add(reply.getReplyUserId());
            }
        });

        // 4. 批量查询用户信息
        Map<Long, UserVO> userMap = userService.listByIds(new ArrayList<>(userIdSet))
                .stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toMap(UserVO::getId, Function.identity()));

        // 5. 组装评论视图
        List<PostCommentVO> commentVOList = commentList.stream().map(comment -> {
            PostCommentVO commentVO = new PostCommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            commentVO.setUser(userMap.get(comment.getUserId()));

            // 设置回复列表
            List<PostCommentVO> replyVOList = replyList.stream()
                    .filter(reply -> Objects.equals(reply.getParentId(), comment.getId()))
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

        // 6. 组装分页结果
        Page<PostCommentVO> commentVOPage = new Page<>(commentPage.getCurrent(),
                commentPage.getSize(), commentPage.getTotal());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }
}