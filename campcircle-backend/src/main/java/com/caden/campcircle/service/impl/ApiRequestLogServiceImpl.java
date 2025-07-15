package com.caden.campcircle.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.mapper.ApiRequestLogMapper;
import com.caden.campcircle.model.dto.log.ApiRequestLogQueryRequest;
import com.caden.campcircle.model.entity.ApiRequestLog;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ApiRequestLogVO;
import com.caden.campcircle.service.ApiRequestLogService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 接口请求日志服务实现
 *
 * @author caden
 */
@Service
@Slf4j
public class ApiRequestLogServiceImpl extends ServiceImpl<ApiRequestLogMapper, ApiRequestLog> 
        implements ApiRequestLogService {

    @Resource
    private UserService userService;

    @Override
    public void logRequest(HttpServletRequest request, 
                          HttpServletResponse response, 
                          String requestBody, 
                          String responseBody, 
                          Integer durationMs, 
                          String exceptionInfo, 
                          Long userId) {
        
        ApiRequestLog apiRequestLog = new ApiRequestLog();
        
        // 设置基本信息
        apiRequestLog.setTraceId(generateTraceId());
        apiRequestLog.setRequestMethod(request.getMethod());
        apiRequestLog.setRequestUrl(request.getRequestURI());
        apiRequestLog.setClientIp(getClientIpAddress(request));
        apiRequestLog.setUserAgent(request.getHeader("User-Agent"));
        apiRequestLog.setUserId(userId);
        apiRequestLog.setDurationMs(durationMs);
        apiRequestLog.setExceptionInfo(exceptionInfo);
        apiRequestLog.setCreateTime(new Date());
        
        // 设置请求参数
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            apiRequestLog.setRequestParams(queryString);
        }
        
        // 设置请求体（限制长度，避免过大）
        if (StringUtils.isNotBlank(requestBody)) {
            apiRequestLog.setRequestBody(truncateString(requestBody, 10000));
        }
        
        // 设置请求头（只记录重要的头信息）
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", request.getHeader("Content-Type"));
        headers.put("Accept", request.getHeader("Accept"));
        headers.put("Authorization", maskSensitiveInfo(request.getHeader("Authorization")));
        apiRequestLog.setRequestHeaders(JSONUtil.toJsonStr(headers));
        
        // 设置响应信息
        if (response != null) {
            apiRequestLog.setResponseStatus(response.getStatus());
        }
        
        // 设置响应体（限制长度，避免过大）
        if (StringUtils.isNotBlank(responseBody)) {
            apiRequestLog.setResponseBody(truncateString(responseBody, 10000));
        }
        
        // 异步保存日志
        logRequestAsync(apiRequestLog);
    }

    @Override
    @Async
    public void logRequestAsync(ApiRequestLog apiRequestLog) {
        try {
            this.save(apiRequestLog);
        } catch (Exception e) {
            log.error("保存请求日志失败", e);
        }
    }

    @Override
    public QueryWrapper<ApiRequestLog> getQueryWrapper(ApiRequestLogQueryRequest queryRequest) {
        QueryWrapper<ApiRequestLog> queryWrapper = new QueryWrapper<>();
        
        if (queryRequest == null) {
            return queryWrapper;
        }
        
        // 链路追踪ID
        if (StringUtils.isNotBlank(queryRequest.getTraceId())) {
            queryWrapper.eq("traceId", queryRequest.getTraceId());
        }
        
        // 请求方式
        if (StringUtils.isNotBlank(queryRequest.getRequestMethod())) {
            queryWrapper.eq("requestMethod", queryRequest.getRequestMethod());
        }
        
        // 请求URL（模糊查询）
        if (StringUtils.isNotBlank(queryRequest.getRequestUrl())) {
            queryWrapper.like("requestUrl", queryRequest.getRequestUrl());
        }
        
        // 客户端IP
        if (StringUtils.isNotBlank(queryRequest.getClientIp())) {
            queryWrapper.eq("clientIp", queryRequest.getClientIp());
        }
        
        // 用户ID
        if (queryRequest.getUserId() != null) {
            queryWrapper.eq("userId", queryRequest.getUserId());
        }
        
        // 响应状态码
        if (queryRequest.getResponseStatus() != null) {
            queryWrapper.eq("responseStatus", queryRequest.getResponseStatus());
        }
        
        // 耗时范围
        if (queryRequest.getMinDurationMs() != null) {
            queryWrapper.ge("durationMs", queryRequest.getMinDurationMs());
        }
        if (queryRequest.getMaxDurationMs() != null) {
            queryWrapper.le("durationMs", queryRequest.getMaxDurationMs());
        }
        
        // 时间范围
        if (queryRequest.getStartTime() != null) {
            queryWrapper.ge("createTime", queryRequest.getStartTime());
        }
        if (queryRequest.getEndTime() != null) {
            queryWrapper.le("createTime", queryRequest.getEndTime());
        }
        
        // 是否包含异常信息
        if (queryRequest.getHasException() != null) {
            if (queryRequest.getHasException()) {
                queryWrapper.isNotNull("exceptionInfo");
            } else {
                queryWrapper.isNull("exceptionInfo");
            }
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc("createTime");
        
        return queryWrapper;
    }

    @Override
    public Page<ApiRequestLogVO> getApiRequestLogVOPage(ApiRequestLogQueryRequest queryRequest) {
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        
        // 查询日志
        Page<ApiRequestLog> logPage = this.page(new Page<>(current, size), 
                getQueryWrapper(queryRequest));
        
        // 转换为VO
        Page<ApiRequestLogVO> voPage = new Page<>(current, size, logPage.getTotal());
        List<ApiRequestLogVO> voList = logPage.getRecords().stream()
                .map(ApiRequestLogVO::objToVo)
                .collect(Collectors.toList());
        
        // 填充用户信息
        fillUserInfo(voList);
        
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 填充用户信息
     */
    private void fillUserInfo(List<ApiRequestLogVO> voList) {
        Set<Long> userIds = voList.stream()
                .map(ApiRequestLogVO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (userIds.isEmpty()) {
            return;
        }
        
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        voList.forEach(vo -> {
            if (vo.getUserId() != null && userMap.containsKey(vo.getUserId())) {
                vo.setUser(userService.getUserVO(userMap.get(vo.getUserId())));
            }
        });
    }

    @Override
    public Map<String, Object> getRequestStatistics(Date startTime, Date endTime) {
        return this.baseMapper.getRequestStatistics(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getTopRequestedApis(Date startTime, Date endTime, Integer limit) {
        return this.baseMapper.getTopRequestedApis(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getTopActiveUsers(Date startTime, Date endTime, Integer limit) {
        return this.baseMapper.getTopActiveUsers(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getErrorStatistics(Date startTime, Date endTime, Integer limit) {
        return this.baseMapper.getErrorStatistics(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getSlowRequestStatistics(Date startTime, Date endTime, Integer minDurationMs, Integer limit) {
        return this.baseMapper.getSlowRequestStatistics(startTime, endTime, minDurationMs, limit);
    }

    @Override
    public List<Map<String, Object>> getHourlyStatistics(Date startTime, Date endTime) {
        return this.baseMapper.getHourlyStatistics(startTime, endTime);
    }

    @Override
    public List<ApiRequestLogVO> getRequestsByTraceId(String traceId) {
        List<ApiRequestLog> logs = this.baseMapper.getRequestsByTraceId(traceId);
        List<ApiRequestLogVO> voList = logs.stream()
                .map(ApiRequestLogVO::objToVo)
                .collect(Collectors.toList());
        fillUserInfo(voList);
        return voList;
    }

    @Override
    public int cleanExpiredLogs(Date beforeDate) {
        QueryWrapper<ApiRequestLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("createTime", beforeDate);
        return this.baseMapper.delete(queryWrapper);
    }

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 截断字符串
     */
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 脱敏敏感信息
     */
    private String maskSensitiveInfo(String info) {
        if (StringUtils.isBlank(info)) {
            return info;
        }
        if (info.length() <= 10) {
            return "***";
        }
        return info.substring(0, 6) + "***" + info.substring(info.length() - 4);
    }
}
