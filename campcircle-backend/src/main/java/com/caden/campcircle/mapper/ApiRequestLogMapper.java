package com.caden.campcircle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caden.campcircle.model.entity.ApiRequestLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 接口请求日志Mapper
 *
 * @author caden
 */
@Mapper
public interface ApiRequestLogMapper extends BaseMapper<ApiRequestLog> {

    /**
     * 根据时间范围查询请求统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as totalRequests, " +
            "COUNT(DISTINCT userId) as uniqueUsers, " +
            "AVG(durationMs) as avgDuration, " +
            "MAX(durationMs) as maxDuration, " +
            "SUM(CASE WHEN responseStatus >= 200 AND responseStatus < 300 THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN responseStatus >= 400 THEN 1 ELSE 0 END) as errorCount " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getRequestStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询最频繁访问的接口
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 接口访问统计
     */
    @Select("SELECT " +
            "requestUrl, " +
            "requestMethod, " +
            "COUNT(*) as requestCount, " +
            "AVG(durationMs) as avgDuration " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY requestUrl, requestMethod " +
            "ORDER BY requestCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopRequestedApis(@Param("startTime") Date startTime, 
                                                  @Param("endTime") Date endTime, 
                                                  @Param("limit") Integer limit);

    /**
     * 查询最活跃的用户
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 用户活跃度统计
     */
    @Select("SELECT " +
            "userId, " +
            "COUNT(*) as requestCount, " +
            "AVG(durationMs) as avgDuration " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime} " +
            "AND userId IS NOT NULL " +
            "GROUP BY userId " +
            "ORDER BY requestCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopActiveUsers(@Param("startTime") Date startTime, 
                                               @Param("endTime") Date endTime, 
                                               @Param("limit") Integer limit);

    /**
     * 查询错误请求统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 错误统计
     */
    @Select("SELECT " +
            "requestUrl, " +
            "requestMethod, " +
            "responseStatus, " +
            "COUNT(*) as errorCount " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime} " +
            "AND responseStatus >= 400 " +
            "GROUP BY requestUrl, requestMethod, responseStatus " +
            "ORDER BY errorCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getErrorStatistics(@Param("startTime") Date startTime, 
                                                @Param("endTime") Date endTime, 
                                                @Param("limit") Integer limit);

    /**
     * 查询慢请求统计
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param minDurationMs 最小耗时（毫秒）
     * @param limit         限制数量
     * @return 慢请求统计
     */
    @Select("SELECT " +
            "requestUrl, " +
            "requestMethod, " +
            "AVG(durationMs) as avgDuration, " +
            "MAX(durationMs) as maxDuration, " +
            "COUNT(*) as slowRequestCount " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime} " +
            "AND durationMs >= #{minDurationMs} " +
            "GROUP BY requestUrl, requestMethod " +
            "ORDER BY avgDuration DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getSlowRequestStatistics(@Param("startTime") Date startTime, 
                                                      @Param("endTime") Date endTime, 
                                                      @Param("minDurationMs") Integer minDurationMs, 
                                                      @Param("limit") Integer limit);

    /**
     * 按小时统计请求量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 小时统计
     */
    @Select("SELECT " +
            "DATE_FORMAT(createTime, '%Y-%m-%d %H:00:00') as hour, " +
            "COUNT(*) as requestCount " +
            "FROM api_request_log " +
            "WHERE createTime BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(createTime, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour")
    List<Map<String, Object>> getHourlyStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 根据traceId查询请求链路
     *
     * @param traceId 链路追踪ID
     * @return 请求链路
     */
    @Select("SELECT * FROM api_request_log WHERE traceId = #{traceId} ORDER BY createTime")
    List<ApiRequestLog> getRequestsByTraceId(@Param("traceId") String traceId);
}
