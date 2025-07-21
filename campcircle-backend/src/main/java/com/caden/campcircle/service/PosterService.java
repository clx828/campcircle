package com.caden.campcircle.service;

import com.caden.campcircle.model.dto.poster.PosterGenerateRequest;
import com.caden.campcircle.model.dto.poster.PosterGenerateResponse;

/**
 * 海报服务
 *
 * @author caden
 */
public interface PosterService {

    /**
     * 生成用户海报
     *
     * @param request 海报生成请求
     * @return 海报生成响应
     */
    PosterGenerateResponse generateUserPoster(PosterGenerateRequest request);

    /**
     * 生成用户海报（简化版本）
     *
     * @param userId 用户ID
     * @param page 跳转页面路径
     * @return 海报图片URL
     */
    String generateUserPosterUrl(Long userId, String page);

    /**
     * 生成用户海报（完整参数版本）
     *
     * @param userId 用户ID
     * @param page 跳转页面路径
     * @param templateType 模板类型
     * @param qrCodeWidth 二维码宽度
     * @return 海报生成响应
     */
    PosterGenerateResponse generateUserPoster(Long userId, String page, String templateType, Integer qrCodeWidth);
}
