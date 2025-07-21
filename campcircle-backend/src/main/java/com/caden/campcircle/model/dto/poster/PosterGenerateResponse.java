package com.caden.campcircle.model.dto.poster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 海报生成响应
 *
 * @author caden
 */
@Data
@ApiModel(description = "海报生成响应")
public class PosterGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 海报图片URL
     */
    @ApiModelProperty(value = "海报图片URL", required = true)
    private String posterUrl;

    /**
     * 海报图片ID（数据库记录ID）
     */
    @ApiModelProperty(value = "海报图片ID")
    private Long pictureId;

    /**
     * 海报宽度
     */
    @ApiModelProperty(value = "海报宽度", example = "750")
    private Integer width;

    /**
     * 海报高度
     */
    @ApiModelProperty(value = "海报高度", example = "1334")
    private Integer height;

    /**
     * 海报文件大小（字节）
     */
    @ApiModelProperty(value = "海报文件大小（字节）")
    private Long fileSize;

    /**
     * 用户信息
     */
    @ApiModelProperty(value = "用户信息")
    private UserInfo userInfo;

    @Data
    @ApiModel(description = "用户信息")
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "用户ID")
        private Long userId;

        @ApiModelProperty(value = "用户昵称")
        private String userName;

        @ApiModelProperty(value = "用户头像")
        private String userAvatar;

        @ApiModelProperty(value = "用户简介")
        private String userProfile;
    }
}
