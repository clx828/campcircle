package com.caden.campcircle.model.dto.log;

import com.caden.campcircle.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口请求日志查询请求
 *
 * @author caden
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiRequestLogQueryRequest extends PageRequest implements Serializable {

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求URL（支持模糊查询）
     */
    private String requestUrl;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 最小耗时（毫秒）
     */
    private Integer minDurationMs;

    /**
     * 最大耗时（毫秒）
     */
    private Integer maxDurationMs;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 是否包含异常信息
     */
    private Boolean hasException;

    private static final long serialVersionUID = 1L;
}
