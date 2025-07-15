package com.caden.campcircle.model.vo;

import com.caden.campcircle.model.entity.ApiRequestLog;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口请求日志视图对象
 *
 * @author caden
 */
@Data
public class ApiRequestLogVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求URI
     */
    private String requestUrl;

    /**
     * URL参数（GET）
     */
    private String requestParams;

    /**
     * 请求体内容（POST JSON等）- 敏感信息可能需要脱敏
     */
    private String requestBody;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * User-Agent信息
     */
    private String userAgent;

    /**
     * 响应HTTP状态码
     */
    private Integer responseStatus;

    /**
     * 响应内容（JSON）- 敏感信息可能需要脱敏
     */
    private String responseBody;

    /**
     * 异常信息（如有）
     */
    private String exceptionInfo;

    /**
     * 请求耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户信息（如果有）
     */
    private UserVO user;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 对象转包装类
     *
     * @param apiRequestLog 原始对象
     * @return VO对象
     */
    public static ApiRequestLogVO objToVo(ApiRequestLog apiRequestLog) {
        if (apiRequestLog == null) {
            return null;
        }
        ApiRequestLogVO apiRequestLogVO = new ApiRequestLogVO();
        BeanUtils.copyProperties(apiRequestLog, apiRequestLogVO);
        return apiRequestLogVO;
    }

    private static final long serialVersionUID = 1L;
}
