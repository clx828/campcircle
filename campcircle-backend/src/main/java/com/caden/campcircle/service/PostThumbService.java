package com.caden.campcircle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.entity.User;

/**
 * 帖子点赞服务
 *
 
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);

    Page<Post> listThumbPostByPage(Page<Post> page, QueryWrapper<Post> queryWrapper, Long thumbUserId);
}
