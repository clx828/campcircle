package com.caden.campcircle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.dto.postComment.PostCommentAddRequest;
import com.caden.campcircle.model.dto.postComment.PostCommentQueryRequest;
import com.caden.campcircle.model.entity.PostComment;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.PostCommentVO;
import com.caden.campcircle.service.PostCommentService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论接口
 *
 */
@RestController
@RequestMapping("/post/comment")
@Slf4j
public class PostCommentController {

    @Resource
    private PostCommentService postCommentService;

    @Resource
    private UserService userService;

    /**
     * 添加评论
     *
     * @param postCommentAddRequest 评论信息
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody PostCommentAddRequest postCommentAddRequest, HttpServletRequest request) {
        if (postCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long commentId = postCommentService.addComment(postCommentAddRequest,  loginUser);
        return ResultUtils.success(commentId);
    }

    /**
     * 删除评论
     *
     * @param id      评论id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = postCommentService.deleteComment(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 分页获取评论列表
     * @param postCommentQueryRequest
     * @return
     */
    /**
     * 分页获取评论列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<PostCommentVO>> listCommentByPage(PostCommentQueryRequest postCommentQueryRequest) {
        if (postCommentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<PostCommentVO> commentVOPage = postCommentService.listCommentByPage(postCommentQueryRequest);
        return ResultUtils.success(commentVOPage);
    }
}