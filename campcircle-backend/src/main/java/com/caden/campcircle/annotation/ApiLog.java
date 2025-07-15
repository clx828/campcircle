package com.caden.campcircle.annotation;

import java.lang.annotation.*;

/**
 * API日志记录注解
 * 
 * 用于标记需要记录请求日志的方法
 * 可以控制是否记录请求参数、响应结果等
 * 
 * @author caden
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录响应结果
     */
    boolean logResult() default true;

    /**
     * 是否记录异常信息
     */
    boolean logException() default true;

    /**
     * 参数最大长度（超过则截断）
     */
    int maxParamLength() default 5000;

    /**
     * 响应最大长度（超过则截断）
     */
    int maxResultLength() default 5000;

    /**
     * 是否忽略敏感参数（如密码等）
     */
    boolean ignoreSensitive() default true;

    /**
     * 敏感参数名称（这些参数将被脱敏）
     */
    String[] sensitiveParams() default {"password", "pwd", "token", "secret", "key"};
}
