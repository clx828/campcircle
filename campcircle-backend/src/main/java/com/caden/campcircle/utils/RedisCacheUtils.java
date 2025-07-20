package com.caden.campcircle.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis缓存工具类
 *
 * @deprecated 推荐使用 CacheService 替代此工具类
 */
@Component
@Slf4j
@Deprecated
public class RedisCacheUtils {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Random RANDOM = new Random();

    /**
     * 通用：从缓存中获取计数值，没有就执行 db 查询并写入缓存（有效期 8~10 分钟）
     */
    public Long getOrCacheCount(String redisKey, Supplier<Long> dbQuery) {
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException e) {
                log.warn("缓存值格式错误，删除缓存: {}", redisKey);
                redisTemplate.delete(redisKey);
            }
        }

        Long count = dbQuery.get();
        int expireSeconds = 480 + RANDOM.nextInt(121); // 8~10 分钟
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
        return count;
    }

    /**
     * 异步更新缓存计数值
     */
    @Async("taskExecutor")
    public void asyncUpdateCacheCount(String redisKey, Supplier<Long> dbQuery) {
        try {
            Long count = dbQuery.get();
            int expireSeconds = 480 + RANDOM.nextInt(121); // 8~10 分钟
            redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
            log.debug("异步更新缓存成功: {} = {}", redisKey, count);
        } catch (Exception e) {
            log.error("异步更新缓存失败: {}", redisKey, e);
        }
    }

    /**
     * 异步删除缓存
     */
    @Async("taskExecutor")
    public void asyncDeleteCache(String redisKey) {
        try {
            redisTemplate.delete(redisKey);
            log.debug("异步删除缓存成功: {}", redisKey);
        } catch (Exception e) {
            log.error("异步删除缓存失败: {}", redisKey, e);
        }
    }
}
