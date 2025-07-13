package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class HotPostVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String content;
    private Double hotScore;
    private Date lastHotUpdateTime;
}
