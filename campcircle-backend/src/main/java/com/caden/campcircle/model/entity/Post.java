package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子
 *
 
 */
@TableName(value = "post")
@Data
public class Post implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表 json
     */
    private String tags;

    /**
     * 图片列表
     */
    private String pictureList;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;
    /**
     * 评论数
     */
    private Integer commentNum;
    /**
     * 浏览数
     */
    private Integer viewNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 置顶过期时间
     */
    private Date topExpireTime;

    /**
     * 热度分数
     */
    private Double hotScore;

    /**
     * 最后热度更新时间
     */
    private Date lastHotUpdateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}