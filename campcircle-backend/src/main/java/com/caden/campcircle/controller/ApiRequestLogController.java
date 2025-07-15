package com.caden.campcircle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.campcircle.common.BaseResponse;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.ResultUtils;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.model.dto.log.ApiRequestLogQueryRequest;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ApiRequestLogVO;
import com.caden.campcircle.service.ApiRequestLogService;
import com.caden.campcircle.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 接口请求日志控制器
 *
 * @author caden
 */
@RestController
@RequestMapping("/api-log")
@Slf4j
@Api(tags = "接口请求日志管理")
public class ApiRequestLogController {

    @Resource
    private ApiRequestLogService apiRequestLogService;

    @Resource
    private UserService userService;

    /**
     * 分页查询请求日志
     */
    @PostMapping("/list")
    @ApiOperation(value = "分页查询请求日志", notes = "管理员可以查看所有请求日志")
    public BaseResponse<Page<ApiRequestLogVO>> listApiRequestLogs(@RequestBody ApiRequestLogQueryRequest queryRequest,
                                                                 HttpServletRequest request) {
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看请求日志");
        }

        Page<ApiRequestLogVO> logPage = apiRequestLogService.getApiRequestLogVOPage(queryRequest);
        return ResultUtils.success(logPage);
    }

    /**
     * 获取请求统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取请求统计信息", notes = "获取指定时间范围内的请求统计数据")
    public BaseResponse<Map<String, Object>> getRequestStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        Map<String, Object> statistics = apiRequestLogService.getRequestStatistics(startTime, endTime);
        return ResultUtils.success(statistics);
    }

    /**
     * 获取最频繁访问的接口
     */
    @GetMapping("/top-apis")
    @ApiOperation(value = "获取最频繁访问的接口", notes = "获取访问量最高的接口列表")
    public BaseResponse<List<Map<String, Object>>> getTopRequestedApis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        List<Map<String, Object>> topApis = apiRequestLogService.getTopRequestedApis(startTime, endTime, limit);
        return ResultUtils.success(topApis);
    }

    /**
     * 获取最活跃的用户
     */
    @GetMapping("/top-users")
    @ApiOperation(value = "获取最活跃的用户", notes = "获取请求量最高的用户列表")
    public BaseResponse<List<Map<String, Object>>> getTopActiveUsers(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        List<Map<String, Object>> topUsers = apiRequestLogService.getTopActiveUsers(startTime, endTime, limit);
        return ResultUtils.success(topUsers);
    }

    /**
     * 获取错误请求统计
     */
    @GetMapping("/errors")
    @ApiOperation(value = "获取错误请求统计", notes = "获取错误请求的统计信息")
    public BaseResponse<List<Map<String, Object>>> getErrorStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        List<Map<String, Object>> errors = apiRequestLogService.getErrorStatistics(startTime, endTime, limit);
        return ResultUtils.success(errors);
    }

    /**
     * 获取慢请求统计
     */
    @GetMapping("/slow-requests")
    @ApiOperation(value = "获取慢请求统计", notes = "获取响应时间较长的请求统计")
    public BaseResponse<List<Map<String, Object>>> getSlowRequestStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1000") Integer minDurationMs,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        List<Map<String, Object>> slowRequests = apiRequestLogService.getSlowRequestStatistics(
                startTime, endTime, minDurationMs, limit);
        return ResultUtils.success(slowRequests);
    }

    /**
     * 获取按小时统计的请求量
     */
    @GetMapping("/hourly-statistics")
    @ApiOperation(value = "获取按小时统计的请求量", notes = "获取每小时的请求量统计")
    public BaseResponse<List<Map<String, Object>>> getHourlyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看统计信息");
        }

        List<Map<String, Object>> hourlyStats = apiRequestLogService.getHourlyStatistics(startTime, endTime);
        return ResultUtils.success(hourlyStats);
    }

    /**
     * 根据traceId查询请求链路
     */
    @GetMapping("/trace/{traceId}")
    @ApiOperation(value = "根据traceId查询请求链路", notes = "查询指定链路追踪ID的所有相关请求")
    public BaseResponse<List<ApiRequestLogVO>> getRequestsByTraceId(@PathVariable String traceId,
                                                                   HttpServletRequest request) {
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以查看请求链路");
        }

        List<ApiRequestLogVO> requests = apiRequestLogService.getRequestsByTraceId(traceId);
        return ResultUtils.success(requests);
    }

    /**
     * 清理过期日志
     */
    @DeleteMapping("/clean")
    @ApiOperation(value = "清理过期日志", notes = "清理指定日期之前的请求日志")
    public BaseResponse<Integer> cleanExpiredLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date beforeDate,
            HttpServletRequest request) {
        
        // 验证管理员权限
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有管理员可以清理日志");
        }

        int cleanedCount = apiRequestLogService.cleanExpiredLogs(beforeDate);
        return ResultUtils.success(cleanedCount);
    }
}
