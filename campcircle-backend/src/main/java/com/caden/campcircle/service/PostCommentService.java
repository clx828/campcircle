package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.postComment.PostCommentAddRequest;
import com.caden.campcircle.model.entity.PostComment;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.model.vo.PostCommentVO;

/**
 * 评论服务
 *
 */
public interface PostCommentService extends IService<PostComment> {

    /**
     * 添加评论
     *
     * @param postCommentAddRequest 评论信息
     * @return 评论id
     */
    long addComment(PostCommentAddRequest postCommentAddRequest, User loginUser);

    /**
     * 删除评论
     *
     * @param id     评论id
     * @param userId 当前用户id
     * @return 是否成功
     */
    boolean deleteComment(long id, long userId);

    /**
     * 分页获取评论列表
     *
     * @param postId   帖子id
     * @param current  当前页码
     * @param pageSize 每页条数
     * @return 评论列表
     */
    Page<PostCommentVO> listCommentByPage(long postId, long current, long pageSize);
}