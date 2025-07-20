package com.caden.campcircle.model.dto.post;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 创建请求
 *
 
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 图片列表
     */
    private List<String> pictureList;

    /**
     * 是否公开：0-否，1-是
     */
    private Integer isPublic;

    private static final long serialVersionUID = 1L;
}