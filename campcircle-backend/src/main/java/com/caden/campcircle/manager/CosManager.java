package com.caden.campcircle.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.caden.campcircle.config.CosClientConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Service;

/**
 * Cos 对象存储操作
 *
 */
@Service
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片（原图，不进行处理）
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传并处理图片（需要开启数据万象服务，暂时注释掉）
     */
    /*
    public PutObjectResult putPictureObjectWithProcessing(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //对图片进行处理
        PicOperations picOperations = new PicOperations();
        //表示返回原图信息
        picOperations.setIsPicInfo(1);
        //图片压缩（改成webp）
        List<PicOperations.Rule> rules = new ArrayList<>();
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule combinedRule = new PicOperations.Rule();
        combinedRule.setFileId(webpKey);
        combinedRule.setBucket(cosClientConfig.getBucket());
        combinedRule.setRule("imageMogr2/format/webp");
        rules.add(combinedRule);
        //缩略图处理
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        String thumbnailUrlKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
        thumbnailRule.setFileId(thumbnailUrlKey);
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
        rules.add(thumbnailRule);
        //构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
    */

    public void deleteObjectByKey(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}