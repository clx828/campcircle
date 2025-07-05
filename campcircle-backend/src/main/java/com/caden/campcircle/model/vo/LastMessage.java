package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LastMessage implements Serializable {
    private String content;
    private Integer messageType;
    private Integer isRecalled;
    private Date createTime;
}
