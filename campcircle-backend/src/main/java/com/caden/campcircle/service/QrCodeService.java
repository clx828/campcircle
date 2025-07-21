package com.caden.campcircle.service;

import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateRequest;
import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateResponse;

/**
 * 二维码服务
 *
 * @author caden
 */
public interface QrCodeService {

    /**
     * 生成小程序二维码
     *
     * @param request 二维码生成请求
     * @return 二维码响应（包含Base64编码）
     */
    QrCodeGenerateResponse generateQrCode(QrCodeGenerateRequest request);

    /**
     * 生成小程序二维码（简化版本）
     *
     * @param scene 场景参数
     * @param page  跳转页面路径
     * @return 二维码Base64编码
     */
    String generateQrCodeBase64(String scene, String page);

    /**
     * 生成小程序二维码（完整参数版本）
     *
     * @param scene     场景参数
     * @param page      跳转页面路径
     * @param width     二维码宽度
     * @param autoColor 是否自动配置线条颜色
     * @param checkPath 是否检查页面存在
     * @return 二维码响应（包含Base64编码）
     */
    QrCodeGenerateResponse generateQrCode(String scene, String page, Integer width, Boolean autoColor, Boolean checkPath);

    /**
     * 生成小程序二维码字节数组
     *
     * @param scene     场景参数
     * @param page      跳转页面路径
     * @param width     二维码宽度
     * @param autoColor 是否自动配置线条颜色
     * @param checkPath 是否检查页面存在
     * @return 二维码字节数组
     */
    byte[] generateQrCodeBytes(String scene, String page, Integer width, Boolean autoColor, Boolean checkPath);
}
