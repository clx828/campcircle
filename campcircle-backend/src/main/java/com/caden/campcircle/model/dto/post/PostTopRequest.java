package com.caden.campcircle.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子置顶请求
 */
@Data
public class PostTopRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long postId;
    
    /**
     * 置顶时长（小时）
     */
    private Integer topTimeHours;

    private static final long serialVersionUID = 1L;
}