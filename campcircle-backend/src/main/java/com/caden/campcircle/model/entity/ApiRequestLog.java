package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口请求日志实体类
 *
 * @author caden
 */
@TableName(value = "api_request_log")
@Data
public class ApiRequestLog implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 请求方式（GET/POST/PUT/DELETE）
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
     * 请求体内容（POST JSON等）
     */
    private String requestBody;

    /**
     * 请求头信息（JSON格式）
     */
    private String requestHeaders;

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
     * 响应内容（JSON）
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
     * 用户ID（如已登录）
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
