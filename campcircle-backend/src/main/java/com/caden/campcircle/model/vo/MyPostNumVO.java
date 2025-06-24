package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MyPostNumVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long ownPostNum;
    private Long favourPostNum;
    private Long thumbPostNum;
}
