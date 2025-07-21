package com.caden.campcircle.model.dto.poster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 海报生成请求
 *
 * @author caden
 */
@Data
@ApiModel(description = "海报生成请求")
public class PosterGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", required = true, example = "1234567890")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 小程序跳转页面路径
     */
    @ApiModelProperty(value = "小程序跳转页面路径", example = "pages/userProfile/userProfile")
    private String page;

    /**
     * 海报模板类型（预留字段，可扩展不同模板）
     */
    @ApiModelProperty(value = "海报模板类型", example = "default")
    private String templateType = "default";

    /**
     * 二维码宽度
     */
    @ApiModelProperty(value = "二维码宽度", example = "200")
    private Integer qrCodeWidth = 200;
}
