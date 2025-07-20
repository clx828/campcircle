package com.caden.campcircle.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.annotation.AuthCheck;
import com.caden.campcircle.common.*;
import com.caden.campcircle.constant.UserConstant;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.post.*;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.HotPostVO;
import com.caden.campcircle.model.vo.MyPostNumVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PostService;
import com.caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/post")
@Slf4j
@Api(tags = "帖子管理")
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param postAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        List<String> tags = postAddRequest.getTags();
        List<String> pictureList = postAddRequest.getPictureList();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        if( pictureList != null){
            post.setPictureList(JSONUtil.toJsonStr(pictureList));
        }
        postService.validPost(post, true);
        User loginUser = userService.getLoginUser(request);
        post.setUserId(loginUser.getId());
        post.setFavourNum(0);
        post.setThumbNum(0);
        // 如果没有设置isPublic，默认为公开
        if (post.getIsPublic() == null) {
            post.setIsPublic(1);
        }
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = postService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param postUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest request) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前 用户
        User loginUser = userService.getLoginUser(request);
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tags = postUpdateRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可修改
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(@RequestParam(required = false, defaultValue = "0") Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 检查帖子是否公开，如果不公开，只有帖子作者和管理员可以查看
        User loginUser = userService.getLoginUserPermitNull(request);
        if (post.getIsPublic() != null && post.getIsPublic() == 0) {
            // 帖子不公开，需要验证权限
            if (loginUser == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该帖子不公开");
            }
            // 不是帖子作者且不是管理员，无权查看
            if (!post.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "该帖子不公开");
            }
        }

        return ResultUtils.success(postService.getPostVO(post, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 如果不是管理员，只能查看公开的帖子
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null || !userService.isAdmin(loginUser)) {
            // 强制设置为只查询公开的帖子
            postQueryRequest.setIsPublic(1);
        }

        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
                                                         HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        postQueryRequest.setUserId(loginUser.getId());
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<PostVO>> searchPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 如果不是管理员，只能搜索公开的帖子
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null || !userService.isAdmin(loginUser)) {
            // 强制设置为只搜索公开的帖子
            postQueryRequest.setIsPublic(1);
        }

        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 编辑（用户）
     *
     * @param postEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest postEditRequest, HttpServletRequest request) {
        if (postEditRequest == null || postEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tags = postEditRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        User loginUser = userService.getLoginUser(request);
        long id = postEditRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/my/postNum")
    public BaseResponse<MyPostNumVO> getMyPostNum( HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        MyPostNumVO postNum = postService.getMyPostNum(loginUser.getId());
        return ResultUtils.success(postNum);
    }

    /**
     * 置顶/取消置顶帖子
     *
     * @param postTopRequest
     * @param request
     * @return
     */
    @PostMapping("/top")
    public BaseResponse<Boolean> topPost(@RequestBody PostTopRequest postTopRequest, HttpServletRequest request) {
        if (postTopRequest == null || postTopRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getId() <= 0){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long postId = postTopRequest.getPostId();
        Integer topTimeHours = postTopRequest.getTopTimeHours();
        
        // 计算过期时间
        Date topExpireTime = null;
        if (topTimeHours != null && topTimeHours > 0) {
            // 当前时间加上指定的小时数
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, topTimeHours);
            topExpireTime = calendar.getTime();
        }
        
        boolean result = postService.topPost(postId, topExpireTime);
        return ResultUtils.success(result);
    }
    @GetMapping("/get/hot/post/list")
    public BaseResponse<List<HotPostVO>> getHotPostList(@RequestParam(defaultValue = "10") Integer limit,
                                                        HttpServletRequest request) {
        return ResultUtils.success(postService.getHotPostList(limit));
    }

    @GetMapping("/search/by/keyword")
    @ApiOperation(value = "根据关键词搜索笔记", notes = "根据关键词搜索笔记")
    public BaseResponse<Page<PostVO>> searchUserByKeyword(@ApiParam(value = "关键词", required = true) PageSearchByKeyWord pageSearchByKeyWord, HttpServletRequest request) {
        if (pageSearchByKeyWord == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        return ResultUtils.success(postService.listPostVOByPage(pageSearchByKeyWord, request));
    }
}
