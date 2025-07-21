package com.caden.campcircle.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.config.WxMaConfiguration;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateRequest;
import com.caden.campcircle.model.dto.qrcode.QrCodeGenerateResponse;
import com.caden.campcircle.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Base64;

/**
 * 二维码服务实现
 *
 * @author caden
 */
@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    @Resource
    private WxMaConfiguration wxMaConfiguration;

    @Override
    public QrCodeGenerateResponse generateQrCode(QrCodeGenerateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        try {
            // 获取微信小程序服务
            WxMaService wxMaService = wxMaConfiguration.getWxMaService();
            
            // 设置小程序码参数
            String scene = request.getScene();
            String page = request.getPage();
            Integer width = request.getWidth();
            Boolean autoColor = request.getAutoColor();
            Boolean checkPath = request.getCheckPath();

            log.info("生成二维码请求参数: scene={}, page={}, width={}, autoColor={}, checkPath={}", 
                    scene, page, width, autoColor, checkPath);

            // 生成小程序码
            byte[] qrCodeBytes = wxMaService.getQrcodeService().createWxaCodeUnlimitBytes(
                    scene, page, autoColor, null, width, false, null, checkPath);

            ThrowUtils.throwIf(qrCodeBytes == null || qrCodeBytes.length == 0, 
                    ErrorCode.SYSTEM_ERROR, "生成二维码失败");

            // 转换为Base64编码
            String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeBytes);

            // 构建响应
            QrCodeGenerateResponse response = new QrCodeGenerateResponse();
            response.setQrCodeBase64(qrCodeBase64);
            response.setMimeType("image/png");
            response.setSize((long) qrCodeBytes.length);

            log.info("二维码生成成功，大小: {} bytes", qrCodeBytes.length);

            return response;

        } catch (WxErrorException e) {
            log.error("微信接口调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成二维码失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("生成二维码异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成二维码失败");
        }
    }

    @Override
    public String generateQrCodeBase64(String scene, String page) {
        QrCodeGenerateRequest request = new QrCodeGenerateRequest();
        request.setScene(scene);
        request.setPage(page);
        request.setWidth(280); // 默认宽度
        request.setAutoColor(false); // 默认不自动配色
        request.setCheckPath(false); // 默认不检查路径
        
        QrCodeGenerateResponse response = generateQrCode(request);
        return response.getQrCodeBase64();
    }

    @Override
    public QrCodeGenerateResponse generateQrCode(String scene, String page, Integer width, Boolean autoColor, Boolean checkPath) {
        QrCodeGenerateRequest request = new QrCodeGenerateRequest();
        request.setScene(scene);
        request.setPage(page);
        request.setWidth(width != null ? width : 280);
        request.setAutoColor(autoColor != null ? autoColor : false);
        request.setCheckPath(checkPath != null ? checkPath : false);
        
        return generateQrCode(request);
    }

    @Override
    public byte[] generateQrCodeBytes(String scene, String page, Integer width, Boolean autoColor, Boolean checkPath) {
        ThrowUtils.throwIf(scene == null || scene.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "场景参数不能为空");

        try {
            // 获取微信小程序服务
            WxMaService wxMaService = wxMaConfiguration.getWxMaService();

            log.info("生成二维码字节数组请求参数: scene={}, page={}, width={}, autoColor={}, checkPath={}", 
                    scene, page, width, autoColor, checkPath);

            // 设置默认值
            Integer finalWidth = width != null ? width : 280;
            Boolean finalAutoColor = autoColor != null ? autoColor : false;
            Boolean finalCheckPath = checkPath != null ? checkPath : false;

            // 生成小程序码
            byte[] qrCodeBytes = wxMaService.getQrcodeService().createWxaCodeUnlimitBytes(
                    scene, page, finalAutoColor, null, finalWidth, false, null, finalCheckPath);

            ThrowUtils.throwIf(qrCodeBytes == null || qrCodeBytes.length == 0, 
                    ErrorCode.SYSTEM_ERROR, "生成二维码失败");

            log.info("二维码字节数组生成成功，大小: {} bytes", qrCodeBytes.length);

            return qrCodeBytes;

        } catch (WxErrorException e) {
            log.error("微信接口调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成二维码失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("生成二维码异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成二维码失败");
        }
    }
}
