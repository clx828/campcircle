package com.caden.campcircle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.manager.FilePictureUpload;
import com.caden.campcircle.model.dto.file.UploadPictureResult;
import com.caden.campcircle.model.entity.Picture;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.service.PictureService;
import com.caden.campcircle.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
* @author chenj
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-06-08 16:18:03
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FilePictureUpload filePictureUpload;
    @Override
    public Picture uploadPicture(MultipartFile multipartFile, LoginUserVO loginUser) {
        String uploadPathPrefix = String.format("campcircle/post/%s",loginUser.getId());
        UploadPictureResult uploadPictureResult = filePictureUpload.uploadPicture(multipartFile, uploadPathPrefix);
        Picture picture = this.getNewPicture(loginUser,uploadPictureResult);
        return picture;
    }

    public Picture getNewPicture(LoginUserVO loginUser,UploadPictureResult uploadPictureResult){
        Picture picture = new Picture();
        picture.setPictureUrl(uploadPictureResult.getUrl());
        picture.setUserId(loginUser.getId());
        boolean result = this.save(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR,  "图片数据新增失败");
        return picture;
    }
}




