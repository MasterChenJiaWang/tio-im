package com.daren.chen.im.core.cache.redis;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author WChao
 * @date 2018年3月8日 下午2:35:19
 */
public class RedisCacheManager {

    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);
    private static final Map<String, RedisCache> MAP = new HashMap<>();

    public static RedisCache getCache(String cacheName) {
        RedisCache redisCache = MAP.get(cacheName);
        if (redisCache == null) {
            log.error(
                "cacheName[{}]还没注册，请初始化时调用：{}.register(redisson, cacheName, timeToLiveSeconds, timeToIdleSeconds)",
                cacheName, RedisCache.class.getSimpleName());
        }
        return redisCache;
    }

    /**
     * timeToLiveSeconds和timeToIdleSeconds不允许同时为null
     *
     * @param cacheName
     * @param timeToLiveSeconds
     * @param timeToIdleSeconds
     * @return
     * @author wchao
     */
    public static RedisCache register(String cacheName, Integer timeToLiveSeconds, Integer timeToIdleSeconds) {
        RedisExpireUpdateTask.start();

        RedisCache redisCache = MAP.get(cacheName);
        if (redisCache == null) {
            synchronized (RedisCacheManager.class) {
                redisCache = MAP.get(cacheName);
                if (redisCache == null) {
                    redisCache = new RedisCache(cacheName, timeToLiveSeconds, timeToIdleSeconds);
                    MAP.put(cacheName, redisCache);
                }
            }
        }
        return redisCache;
    }

}
