package com.caden.campcircle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.common.PageSearchByKeyWord;
import com.caden.campcircle.model.dto.post.PostQueryRequest;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.vo.HotPostVO;
import com.caden.campcircle.model.vo.MyPostNumVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 帖子服务
 *
 
 */
public interface PostService extends IService<Post> {

    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validPost(Post post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    MyPostNumVO getMyPostNum(Long id);

    /**
     * 置顶帖子
     *
     * @param postId
     * @param topExpireTime
     * @return
     */
    boolean topPost(long postId, Date topExpireTime);

    List<HotPostVO> getHotPostList(Integer limit);

    Page<PostVO> listPostVOByPage(PageSearchByKeyWord pageSearchByKeyWord, HttpServletRequest request);
}
