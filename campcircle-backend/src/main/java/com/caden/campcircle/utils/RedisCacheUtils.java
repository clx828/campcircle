package com.caden.campcircle.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
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
            return Long.parseLong(cached);
        }

        Long count = dbQuery.get();
        int expireSeconds = 480 + RANDOM.nextInt(121); // 8~10 分钟
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
        return count;
    }
}
