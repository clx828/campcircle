package com.caden.campcircle.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.model.entity.PostThumb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞数据库操作
 *
 
 */
public interface PostThumbMapper extends BaseMapper<PostThumb> {

    /**
     * 分页查询收藏帖子列表
     *
     * @param page
     * @param queryWrapper
     * @param thumbUserId
     * @return
     */
    Page<Post> listThumbPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
                                   long thumbUserId);
}




