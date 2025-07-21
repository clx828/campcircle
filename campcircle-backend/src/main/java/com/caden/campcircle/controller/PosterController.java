package com.caden.campcircle.controller;

import com.caden.campcircle.annotation.AuthCheck;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.poster.PosterGenerateRequest;
import com.caden.campcircle.model.dto.poster.PosterGenerateResponse;
import com.caden.campcircle.service.PosterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 海报接口
 *
 * @author caden
 */
@RestController
@RequestMapping("/poster")
@Slf4j
@Api(tags = "海报管理")
public class PosterController {

    @Resource
    private PosterService posterService;

    /**
     * 生成用户海报
     *
     * @param posterGenerateRequest 海报生成请求
     * @return 海报生成响应
     */
    @PostMapping("/generate")
    @ApiOperation(value = "生成用户海报", notes = "根据用户ID和页面路径生成包含用户信息和二维码的海报")
    @AuthCheck(mustRole = "user")
    public BaseResponse<PosterGenerateResponse> generatePoster(@Valid @RequestBody PosterGenerateRequest posterGenerateRequest) {
        ThrowUtils.throwIf(posterGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        
        PosterGenerateResponse response = posterService.generateUserPoster(posterGenerateRequest);
        return ResultUtils.success(response);
    }

    /**
     * 生成用户海报（简化接口）
     *
     * @param userId 用户ID
     * @param page 跳转页面路径
     * @return 海报图片URL
     */
    @GetMapping("/generate/simple")
    @ApiOperation(value = "生成用户海报（简化版）", notes = "通过GET请求快速生成用户海报，返回图片URL")
    @AuthCheck(mustRole = "user")
    public BaseResponse<String> generatePosterSimple(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "跳转页面路径") @RequestParam(required = false, defaultValue = "pages/userProfile/userProfile") String page) {
        
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        
        String posterUrl = posterService.generateUserPosterUrl(userId, page);
        return ResultUtils.success(posterUrl);
    }

    /**
     * 生成用户海报（完整参数）
     *
     * @param userId 用户ID
     * @param page 跳转页面路径
     * @param templateType 模板类型
     * @param qrCodeWidth 二维码宽度
     * @return 海报生成响应
     */
    @GetMapping("/generate/full")
    @ApiOperation(value = "生成用户海报（完整参数）", notes = "通过GET请求生成用户海报，支持自定义模板和二维码大小")
    @AuthCheck(mustRole = "user")
    public BaseResponse<PosterGenerateResponse> generatePosterFull(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "跳转页面路径") @RequestParam(required = false, defaultValue = "pages/userProfile/userProfile") String page,
            @ApiParam(value = "模板类型") @RequestParam(required = false, defaultValue = "default") String templateType,
            @ApiParam(value = "二维码宽度") @RequestParam(required = false, defaultValue = "200") Integer qrCodeWidth) {
        
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        
        PosterGenerateResponse response = posterService.generateUserPoster(userId, page, templateType, qrCodeWidth);
        return ResultUtils.success(response);
    }
}
