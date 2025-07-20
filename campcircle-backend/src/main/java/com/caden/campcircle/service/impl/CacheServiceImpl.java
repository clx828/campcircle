package com.caden.campcircle.service.impl;

import com.caden.campcircle.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务实现类
 * 
 * @author caden
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Random RANDOM = new Random();

    @Override
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
        // 设置 8 到 10 分钟之间的随机过期时间（单位：秒）
        int expireSeconds = 480 + RANDOM.nextInt(121); // 480~600 秒
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
        return count;
    }

    @Override
    @Async("taskExecutor")
    public void asyncUpdateCacheCount(String redisKey, Supplier<Long> dbQuery) {
        try {
            Long count = dbQuery.get();
            // 设置 8 到 10 分钟之间的随机过期时间（单位：秒）
            int expireSeconds = 480 + RANDOM.nextInt(121); // 480~600 秒
            redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
            log.debug("异步更新缓存成功: {} = {}", redisKey, count);
        } catch (Exception e) {
            log.error("异步更新缓存失败: {}", redisKey, e);
        }
    }

    @Override
    public void deleteCache(String redisKey) {
        try {
            redisTemplate.delete(redisKey);
            log.debug("删除缓存成功: {}", redisKey);
        } catch (Exception e) {
            log.error("删除缓存失败: {}", redisKey, e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void asyncDeleteCache(String redisKey) {
        deleteCache(redisKey);
    }

    @Override
    public void deleteCaches(String... redisKeys) {
        try {
            if (redisKeys != null && redisKeys.length > 0) {
                redisTemplate.delete(java.util.Arrays.asList(redisKeys));
                log.debug("批量删除缓存成功: {}", java.util.Arrays.toString(redisKeys));
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败: {}", java.util.Arrays.toString(redisKeys), e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void asyncDeleteCaches(String... redisKeys) {
        deleteCaches(redisKeys);
    }

    @Override
    public void setCache(String redisKey, String value, int expireSeconds) {
        try {
            redisTemplate.opsForValue().set(redisKey, value, expireSeconds, TimeUnit.SECONDS);
            log.debug("设置缓存成功: {} = {}", redisKey, value);
        } catch (Exception e) {
            log.error("设置缓存失败: {} = {}", redisKey, value, e);
        }
    }

    @Override
    @Async("taskExecutor")
    public void asyncSetCache(String redisKey, String value, int expireSeconds) {
        setCache(redisKey, value, expireSeconds);
    }
}
