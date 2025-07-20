package com.caden.campcircle.service;

import java.util.function.Supplier;

/**
 * 缓存服务接口
 * 
 * @author caden
 */
public interface CacheService {
    
    /**
     * 获取或设置缓存计数值
     * 
     * @param redisKey 缓存键
     * @param dbQuery 数据库查询函数
     * @return 计数值
     */
    Long getOrCacheCount(String redisKey, Supplier<Long> dbQuery);
    
    /**
     * 异步更新缓存计数值
     * 
     * @param redisKey 缓存键
     * @param dbQuery 数据库查询函数
     */
    void asyncUpdateCacheCount(String redisKey, Supplier<Long> dbQuery);
    
    /**
     * 删除缓存
     * 
     * @param redisKey 缓存键
     */
    void deleteCache(String redisKey);
    
    /**
     * 异步删除缓存
     * 
     * @param redisKey 缓存键
     */
    void asyncDeleteCache(String redisKey);
    
    /**
     * 批量删除缓存
     * 
     * @param redisKeys 缓存键数组
     */
    void deleteCaches(String... redisKeys);
    
    /**
     * 异步批量删除缓存
     * 
     * @param redisKeys 缓存键数组
     */
    void asyncDeleteCaches(String... redisKeys);
    
    /**
     * 设置缓存值
     * 
     * @param redisKey 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    void setCache(String redisKey, String value, int expireSeconds);
    
    /**
     * 异步设置缓存值
     * 
     * @param redisKey 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    void asyncSetCache(String redisKey, String value, int expireSeconds);
}
