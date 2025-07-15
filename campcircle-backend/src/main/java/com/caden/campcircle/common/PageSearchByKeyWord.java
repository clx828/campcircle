package com.caden.campcircle.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageSearchByKeyWord extends PageRequest implements Serializable {
    private static final long serialVersionUID = -7112869742285842865L;
    private String keyWord;
}
