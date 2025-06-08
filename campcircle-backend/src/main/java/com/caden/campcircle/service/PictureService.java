package com.caden.campcircle.service;

import com.caden.campcircle.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.vo.LoginUserVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author chenj
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-08 16:18:03
*/
public interface PictureService extends IService<Picture> {

    Picture uploadPicture(MultipartFile multipartFile , LoginUserVO loginUser);

}
