package com.caden.campcircle.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.config.CosClientConfig;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource 输入源
     * @param uploadPathPrefix 上传路径前缀
     * @return 上传结果
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验图片
        validPicture(inputSource);

        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString();
        String originalFileName = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originalFileName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            // 创建临时文件
            String tempFilePrefix = String.format("upload_%s_%s", DateUtil.formatDate(new Date()), uuid);
            String tempFileSuffix = "." + FileUtil.getSuffix(originalFileName);
            file = File.createTempFile(tempFilePrefix, tempFileSuffix);

            // 处理文件
            processFile(inputSource, file);

            // 上传文件到COS
            cosManager.putObject(uploadPath, file);  // 使用基础上传方法，不需要图片处理

            // 构建返回结果
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + uploadPath);
            return result;

        } catch (IOException e) {
            log.error("file upload error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            // 清理临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 获取原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 处理文件
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 校验图片
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file != null && !file.delete()) {
            log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
        }
    }
}