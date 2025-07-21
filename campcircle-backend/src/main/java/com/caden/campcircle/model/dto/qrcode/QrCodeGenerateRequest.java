package com.caden.campcircle.model.dto.qrcode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 二维码生成请求
 *
 * @author caden
 */
@Data
@ApiModel(description = "二维码生成请求")
public class QrCodeGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景参数，最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~
     * 其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     */
    @ApiModelProperty(value = "场景参数", required = true, example = "id=123")
    @NotBlank(message = "场景参数不能为空")
    private String scene;

    /**
     * 跳转页面路径，必须是已经发布的小程序存在的页面（否则报错），例如 pages/index/index
     * 根路径前不要填加 /，不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
     */
    @ApiModelProperty(value = "跳转页面路径", example = "pages/userProfile/userProfile")
    private String page;

    /**
     * 二维码的宽度，单位 px，最小 280px，最大 1280px
     */
    @ApiModelProperty(value = "二维码宽度", example = "280")
    @Min(value = 280, message = "二维码宽度最小为280px")
    @Max(value = 1280, message = "二维码宽度最大为1280px")
    private Integer width = 280;

    /**
     * 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false
     */
    @ApiModelProperty(value = "是否自动配置线条颜色", example = "false")
    private Boolean autoColor = false;

    /**
     * 检查page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）；为 false 时允许小程序未发布或者 page 不存在， 但page 有数量上限（60000个）请勿滥用，默认true
     */
    @ApiModelProperty(value = "是否检查页面存在", example = "false")
    private Boolean checkPath = false;
}
