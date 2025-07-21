package com.caden.campcircle.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.manager.FilePictureUpload;
import com.caden.campcircle.model.dto.poster.PosterGenerateRequest;
import com.caden.campcircle.model.dto.poster.PosterGenerateResponse;
import com.caden.campcircle.model.entity.Picture;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.service.PictureService;
import com.caden.campcircle.service.PosterService;
import com.caden.campcircle.service.QrCodeService;
import com.caden.campcircle.service.UserService;
import com.caden.campcircle.utils.PosterUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 海报服务实现
 *
 * @author caden
 */
@Service
@Slf4j
public class PosterServiceImpl implements PosterService {

    @Resource
    private UserService userService;

    @Resource
    private QrCodeService qrCodeService;

    @Resource
    private PictureService pictureService;

    @Override
    public PosterGenerateResponse generateUserPoster(PosterGenerateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(request.getUserId() == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        try {
            // 获取用户信息
            User user = userService.getById(request.getUserId());
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

            // 生成场景参数
            String scene = "userId=" + request.getUserId();
            String page = StrUtil.isNotBlank(request.getPage()) ? request.getPage() : "pages/userProfile/userProfile";

            log.info("开始生成用户海报: userId={}, scene={}, page={}", request.getUserId(), scene, page);

            // 生成二维码
            String qrCodeBase64 = qrCodeService.generateQrCodeBase64(scene, page);
            ThrowUtils.throwIf(StrUtil.isBlank(qrCodeBase64), ErrorCode.SYSTEM_ERROR, "生成二维码失败");

            // 生成海报
            Integer qrCodeWidth = request.getQrCodeWidth() != null ? request.getQrCodeWidth() : 200;
            BufferedImage posterImage = PosterUtils.generateUserPoster(
                    user.getUserName(),
                    user.getUserProfile(),
                    user.getUserAvatar(),
                    qrCodeBase64,
                    qrCodeWidth
            );

            ThrowUtils.throwIf(posterImage == null, ErrorCode.SYSTEM_ERROR, "生成海报失败");

            // 保存海报到临时文件
            File tempFile = null;
            try {
                tempFile = createTempPosterFile(posterImage);
                // 上传海报图片
                MultipartFile multipartFile = createMultipartFile(tempFile);
                LoginUserVO loginUserVO = createSystemLoginUser(user);
                Picture picture = pictureService.uploadPicture(multipartFile, loginUserVO);

                // 构建响应
                PosterGenerateResponse response = new PosterGenerateResponse();
                response.setPosterUrl(picture.getPictureUrl());
                response.setPictureId(picture.getId());
                response.setWidth(PosterUtils.POSTER_WIDTH);
                response.setHeight(PosterUtils.POSTER_HEIGHT);
                response.setFileSize(tempFile.length());

                // 设置用户信息
                PosterGenerateResponse.UserInfo userInfo = new PosterGenerateResponse.UserInfo();
                userInfo.setUserId(user.getId());
                userInfo.setUserName(user.getUserName());
                userInfo.setUserAvatar(user.getUserAvatar());
                userInfo.setUserProfile(user.getUserProfile());
                response.setUserInfo(userInfo);

                log.info("用户海报生成成功: userId={}, posterUrl={}", request.getUserId(), picture.getPictureUrl());

                return response;

            } finally {
                // 清理临时文件
                if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                    log.warn("删除临时海报文件失败: {}", tempFile.getAbsolutePath());
                }
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成用户海报失败: userId={}", request.getUserId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成海报失败");
        }
    }

    @Override
    public String generateUserPosterUrl(Long userId, String page) {
        PosterGenerateRequest request = new PosterGenerateRequest();
        request.setUserId(userId);
        request.setPage(page);
        request.setTemplateType("default");
        request.setQrCodeWidth(200);

        PosterGenerateResponse response = generateUserPoster(request);
        return response.getPosterUrl();
    }

    @Override
    public PosterGenerateResponse generateUserPoster(Long userId, String page, String templateType, Integer qrCodeWidth) {
        PosterGenerateRequest request = new PosterGenerateRequest();
        request.setUserId(userId);
        request.setPage(page);
        request.setTemplateType(templateType);
        request.setQrCodeWidth(qrCodeWidth);

        return generateUserPoster(request);
    }

    /**
     * 创建临时海报文件
     */
    private File createTempPosterFile(BufferedImage posterImage) throws IOException {
        String tempFileName = String.format("poster_%s_%s.png", 
                DateUtil.formatDate(new Date()), UUID.randomUUID().toString());
        File tempFile = File.createTempFile("poster_", ".png");
        
        PosterUtils.saveImageToFile(posterImage, tempFile);
        
        log.debug("创建临时海报文件: {}, 大小: {} bytes", tempFile.getAbsolutePath(), tempFile.length());
        
        return tempFile;
    }

    /**
     * 创建MultipartFile对象
     */
    private MultipartFile createMultipartFile(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);

            return new CustomMultipartFile(
                    "poster",
                    file.getName(),
                    "image/png",
                    bytes
            );
        }
    }

    /**
     * 自定义MultipartFile实现
     */
    private static class CustomMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }

    /**
     * 创建系统用户登录信息（用于上传图片）
     */
    private LoginUserVO createSystemLoginUser(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUserName(user.getUserName());
        loginUserVO.setUserAvatar(user.getUserAvatar());
        loginUserVO.setUserProfile(user.getUserProfile());
        loginUserVO.setUserRole(user.getUserRole());
        loginUserVO.setCreateTime(user.getCreateTime());
        return loginUserVO;
    }
}
