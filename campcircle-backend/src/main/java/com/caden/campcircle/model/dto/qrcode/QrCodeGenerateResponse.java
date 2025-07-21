package com.caden.campcircle.model.dto.qrcode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 二维码生成响应
 *
 * @author caden
 */
@Data
@ApiModel(description = "二维码生成响应")
public class   QrCodeGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 二维码图片的Base64编码
     */
    @ApiModelProperty(value = "二维码图片的Base64编码", required = true)
    private String qrCodeBase64;

    /**
     * 二维码图片的MIME类型
     */
    @ApiModelProperty(value = "二维码图片的MIME类型", example = "image/png")
    private String mimeType;

    /**
     * 二维码图片大小（字节）
     */
    @ApiModelProperty(value = "二维码图片大小（字节）")
    private Long size;
}
