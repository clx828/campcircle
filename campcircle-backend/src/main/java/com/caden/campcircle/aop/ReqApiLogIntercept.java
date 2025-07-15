package com.caden.campcircle.aop;

import cn.hutool.json.JSONUtil;
import com.caden.campcircle.annotation.ApiLog;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.service.ApiRequestLogService;
import com.caden.campcircle.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * API请求日志AOP拦截器
 *
 * 使用AOP环绕通知记录所有Controller方法的请求日志
 *
 * @author caden
 */
@Aspect
@Component
@Slf4j
public class ReqApiLogIntercept {

    @Resource
    private ApiRequestLogService apiRequestLogService;

    @Resource
    private UserService userService;

    /**
     * 定义切点1：拦截所有Controller层的方法
     */
    @Pointcut("execution(* com.caden.campcircle.controller.*.*(..))")
    public void controllerMethods() {
    }

    /**
     * 定义切点2：拦截带有@ApiLog注解的方法
     */
    @Pointcut("@annotation(com.caden.campcircle.annotation.ApiLog)")
    public void apiLogMethods() {
    }

    /**
     * 环绕通知：记录请求日志（Controller方法）
     */
    @Around("controllerMethods()")
    public Object logControllerRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        return logApiRequest(joinPoint, null);
    }

    /**
     * 环绕通知：记录请求日志（带注解的方法）
     */
    @Around("apiLogMethods()")
    public Object logAnnotatedRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiLog apiLog = method.getAnnotation(ApiLog.class);
        return logApiRequest(joinPoint, apiLog);
    }

    /**
     * 通用的日志记录方法
     */
    private Object logApiRequest(ProceedingJoinPoint joinPoint, ApiLog apiLog) throws Throwable {
        // 获取请求开始时间
        long startTime = System.currentTimeMillis();

        // 获取HTTP请求和响应对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 如果不是HTTP请求，直接执行方法
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 判断是否需要记录日志
        if (!shouldLogRequest(request)) {
            return joinPoint.proceed();
        }

        Object result = null;
        String exceptionInfo = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            // 记录异常信息
            exceptionInfo = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            throw throwable;
        } finally {
            // 记录请求日志
            recordRequestLog(request, response, joinPoint, startTime, result, exceptionInfo, apiLog);
        }
    }

    /**
     * 记录请求日志
     */
    private void recordRequestLog(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ProceedingJoinPoint joinPoint,
                                 long startTime,
                                 Object result,
                                 String exceptionInfo,
                                 ApiLog apiLog) {
        try {
            // 计算请求耗时
            int durationMs = (int) (System.currentTimeMillis() - startTime);

            // 获取当前用户ID
            Long userId = getCurrentUserId(request);

            // 获取请求参数（根据注解配置决定是否记录）
            String requestParams = null;
            if (apiLog == null || apiLog.logParams()) {
                requestParams = getRequestParams(joinPoint, apiLog);
            }

            // 获取响应内容（根据注解配置决定是否记录）
            String responseBody = null;
            if (apiLog == null || apiLog.logResult()) {
                responseBody = getResponseBody(result, apiLog);
            }

            // 处理异常信息（根据注解配置决定是否记录）
            if (apiLog != null && !apiLog.logException()) {
                exceptionInfo = null;
            }

            // 异步记录日志
            apiRequestLogService.logRequest(request, response, requestParams, responseBody,
                                          durationMs, exceptionInfo, userId);

        } catch (Exception e) {
            log.error("记录API请求日志失败", e);
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUserPermitNull(request);
            return loginUser != null ? loginUser.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint, ApiLog apiLog) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }

            // 获取方法参数名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();

            // 构建参数Map
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                if (args[i] instanceof HttpServletRequest ||
                    args[i] instanceof HttpServletResponse) {
                    paramMap.put(paramNames[i], "[" + args[i].getClass().getSimpleName() + "]");
                } else {
                    // 处理敏感参数
                    Object paramValue = args[i];
                    if (apiLog != null && apiLog.ignoreSensitive()) {
                        paramValue = maskSensitiveParam(paramNames[i], paramValue, apiLog.sensitiveParams());
                    }
                    paramMap.put(paramNames[i], paramValue);
                }
            }

            String jsonStr = JSONUtil.toJsonStr(paramMap);

            // 根据注解配置截断长度
            int maxLength = apiLog != null ? apiLog.maxParamLength() : 5000;
            return truncateString(jsonStr, maxLength);

        } catch (Exception e) {
            return "参数序列化失败: " + e.getMessage();
        }
    }

    /**
     * 获取响应内容
     */
    private String getResponseBody(Object result, ApiLog apiLog) {
        try {
            if (result == null) {
                return null;
            }

            String jsonStr = JSONUtil.toJsonStr(result);

            // 根据注解配置截断长度
            int maxLength = apiLog != null ? apiLog.maxResultLength() : 5000;
            return truncateString(jsonStr, maxLength);

        } catch (Exception e) {
            return "响应序列化失败: " + e.getMessage();
        }
    }

    /**
     * 脱敏敏感参数
     */
    private Object maskSensitiveParam(String paramName, Object paramValue, String[] sensitiveParams) {
        if (paramValue == null || paramName == null) {
            return paramValue;
        }
        // 检查参数名是否为敏感参数
        for (String sensitiveParam : sensitiveParams) {
            if (paramName.toLowerCase().contains(sensitiveParam.toLowerCase())) {
                if (paramValue instanceof String) {
                    String strValue = (String) paramValue;
                    if (strValue.length() <= 6) {
                        return "***";
                    }
                    return strValue.substring(0, 3) + "***" + strValue.substring(strValue.length() - 3);
                } else {
                    return "***";
                }
            }
        }

        return paramValue;
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
     * 判断是否需要记录该请求
     */
    private boolean shouldLogRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 排除静态资源
        if (uri.contains("/static/") || uri.contains("/css/") ||
            uri.contains("/js/") || uri.contains("/images/") ||
            uri.contains("/favicon.ico")) {
            return false;
        }

        // 排除健康检查接口
        if (uri.contains("/health") || uri.contains("/actuator/")) {
            return false;
        }

        // 排除Swagger相关接口
        if (uri.contains("/swagger") || uri.contains("/api-docs") ||
            uri.contains("/webjars/")) {
            return false;
        }

        // 排除日志查询接口本身（避免递归记录）
        if (uri.startsWith("/api/api-log/")) {
            return false;
        }

        return true;
    }
}
