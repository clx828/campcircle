package com.caden.campcircle.controller;

import com.caden.campcircle.annotation.AuthCheck;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateRequest;
import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateResponse;
import com.caden.campcircle.service.QrCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 二维码接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/qrcode")
@Slf4j
@Api(tags = "二维码管理")
public class QrCodeController {

    @Resource
    private QrCodeService qrCodeService;

    /**
     * 生成小程序二维码
     *
     * @param qrCodeGenerateRequest 二维码生成请求
     * @return 二维码Base64编码
     */
    @PostMapping("/generate")
    @ApiOperation(value = "生成小程序二维码", notes = "根据场景参数和页面路径生成小程序二维码，返回Base64编码")
    @AuthCheck(mustRole = "user")
    public BaseResponse<QrCodeGenerateResponse> generateQrCode(@Valid @RequestBody QrCodeGenerateRequest qrCodeGenerateRequest) {
        ThrowUtils.throwIf(qrCodeGenerateRequest == null, ErrorCode.PARAMS_ERROR);

        QrCodeGenerateResponse response = qrCodeService.generateQrCode(qrCodeGenerateRequest);
        return ResultUtils.success(response);
    }
}
