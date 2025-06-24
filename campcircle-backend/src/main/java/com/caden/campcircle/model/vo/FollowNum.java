package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FollowNum implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long followNum;
    private Long fansNum;
    private Long thumbNum;

}
