package com.caden.campcircle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.log.ApiRequestLogQueryRequest;
import com.caden.campcircle.model.entity.ApiRequestLog;
import com.caden.campcircle.model.vo.ApiRequestLogVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 接口请求日志服务
 *
 * @author caden
 */
public interface ApiRequestLogService extends IService<ApiRequestLog> {

    /**
     * 记录请求日志
     *
     * @param request        HTTP请求
     * @param response       HTTP响应
     * @param requestBody    请求体内容
     * @param responseBody   响应体内容
     * @param durationMs     请求耗时
     * @param exceptionInfo  异常信息
     * @param userId         用户ID
     */
    void logRequest(HttpServletRequest request, 
                   HttpServletResponse response, 
                   String requestBody, 
                   String responseBody, 
                   Integer durationMs, 
                   String exceptionInfo, 
                   Long userId);

    /**
     * 异步记录请求日志
     *
     * @param apiRequestLog 日志对象
     */
    void logRequestAsync(ApiRequestLog apiRequestLog);

    /**
     * 构建查询条件
     *
     * @param apiRequestLogQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<ApiRequestLog> getQueryWrapper(ApiRequestLogQueryRequest apiRequestLogQueryRequest);

    /**
     * 分页查询请求日志
     *
     * @param apiRequestLogQueryRequest 查询请求
     * @return 分页结果
     */
    Page<ApiRequestLogVO> getApiRequestLogVOPage(ApiRequestLogQueryRequest apiRequestLogQueryRequest);

    /**
     * 获取请求统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    Map<String, Object> getRequestStatistics(Date startTime, Date endTime);

    /**
     * 获取最频繁访问的接口
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 接口访问统计
     */
    List<Map<String, Object>> getTopRequestedApis(Date startTime, Date endTime, Integer limit);

    /**
     * 获取最活跃的用户
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 用户活跃度统计
     */
    List<Map<String, Object>> getTopActiveUsers(Date startTime, Date endTime, Integer limit);

    /**
     * 获取错误请求统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 错误统计
     */
    List<Map<String, Object>> getErrorStatistics(Date startTime, Date endTime, Integer limit);

    /**
     * 获取慢请求统计
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param minDurationMs 最小耗时
     * @param limit         限制数量
     * @return 慢请求统计
     */
    List<Map<String, Object>> getSlowRequestStatistics(Date startTime, Date endTime, Integer minDurationMs, Integer limit);

    /**
     * 获取按小时统计的请求量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 小时统计
     */
    List<Map<String, Object>> getHourlyStatistics(Date startTime, Date endTime);

    /**
     * 根据traceId查询请求链路
     *
     * @param traceId 链路追踪ID
     * @return 请求链路
     */
    List<ApiRequestLogVO> getRequestsByTraceId(String traceId);

    /**
     * 清理过期日志
     *
     * @param beforeDate 清理此日期之前的日志
     * @return 清理的记录数
     */
    int cleanExpiredLogs(Date beforeDate);

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    String getClientIpAddress(HttpServletRequest request);

    /**
     * 生成链路追踪ID
     *
     * @return 链路追踪ID
     */
    String generateTraceId();
}
