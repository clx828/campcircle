package com.caden.campcircle.controller;

import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.entity.Picture;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.service.PictureService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    @PostMapping("/upload")
    public BaseResponse<Picture> uploadPicture(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        log.info("uploadPicture");
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件为空");
        }
        LoginUserVO loginUser = userService.getLoginUserVO(userService.getLoginUser(request));
        Picture picture = pictureService.uploadPicture(multipartFile, loginUser);
        return ResultUtils.success(picture);
    }
}
