package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.SystemTimer;

import com.daren.chen.im.core.cache.ICache;
import com.daren.chen.im.core.utils.JsonKit;

/**
 *
 * @author wchao 2017年8月10日 下午1:35:01
 */
public class RedisCache implements ICache {

    private final Logger log = LoggerFactory.getLogger(RedisCache.class);

    public static String cacheKey(String cacheName, String key) {
        return keyPrefix(cacheName) + key;
    }

    public static String keyPrefix(String cacheName) {
        return cacheName + ":";
    }

    private String cacheName = null;

    private Integer timeToLiveSeconds = null;

    private Integer timeToIdleSeconds = null;

    private Integer timeout = null;

    public RedisCache(String cacheName, Integer timeToLiveSeconds, Integer timeToIdleSeconds) {
        this.cacheName = cacheName;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.timeToIdleSeconds = timeToIdleSeconds;
        this.timeout = this.timeToLiveSeconds == null ? this.timeToIdleSeconds : this.timeToLiveSeconds;

    }

    @Override
    public void clear() {
        long start = SystemTimer.currentTimeMillis();
        try {
            RedisTemplateUtils.getRedisTemplate().delKeysLike(keyPrefix(cacheName));
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        long end = SystemTimer.currentTimeMillis();
        long iv = end - start;
        log.info("clear cache {}, cost {}ms", cacheName, iv);
    }

    @Override
    public Serializable get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        Serializable value = null;
        try {
            value = RedisTemplateUtils.getRedisTemplate().get(cacheKey(cacheName, key), Serializable.class);
            if (timeToIdleSeconds != null) {
                if (value != null) {
                    RedisExpireUpdateTask.add(cacheName, key, value, timeout);
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        T value = null;
        try {
            value = RedisTemplateUtils.getRedisTemplate().get(cacheKey(cacheName, key), clazz);
            if (timeToIdleSeconds != null) {
                if (value != null) {
                    RedisExpireUpdateTask.add(cacheName, key, (Serializable)value, timeout);
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return value;
    }

    @Override
    public Collection<String> keys() {
        try {
            return RedisTemplateUtils.getRedisTemplate().keys(keyPrefix(cacheName));
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    @Override
    public void put(String key, Serializable value) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            RedisTemplateUtils.getRedisTemplate().set(cacheKey(cacheName, key), value, Integer.parseInt(timeout + ""));
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public void putAll(List<Pair<String, Serializable>> values) {
        if (values == null || values.size() < 1) {
            return;
        }
        int expire = Integer.parseInt(timeout + "");
        try {
            List<PairEx<String, String, Integer>> pairDatas = new ArrayList<>();
            for (Pair<String, Serializable> pair : values) {
                pairDatas.add(RedisTemplateUtils.getRedisTemplate().makePairEx(cacheKey(cacheName, pair.getKey()),
                    JsonKit.toJSONString(pair.getValue()), expire));
            }
            RedisTemplateUtils.getRedisTemplate().batchSetStringEx(pairDatas);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public void listPushTail(String key, Serializable value) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            String jsonValue = value instanceof String ? (String)value : JsonKit.toJSONString(value);
            RedisTemplateUtils.getRedisTemplate().listPushTail(cacheKey(cacheName, key), jsonValue);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public List<String> listGetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            return RedisTemplateUtils.getRedisTemplate().listGetAll(cacheKey(cacheName, key));
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    /**
     * list
     *
     * @param key
     * @param value
     * @return
     */
    public Long listRemove(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return 0L;
        }
        try {
            return RedisTemplateUtils.getRedisTemplate().listRemove(cacheKey(cacheName, key), 0, value);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return 0L;
    }

    public void sortSetPush(String key, double score, Serializable value) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            String jsonValue = value instanceof String ? (String)value : JsonKit.toJSONString(value);
            RedisTemplateUtils.getRedisTemplate().sortSetPush(cacheKey(cacheName, key), score, jsonValue);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public Long sortRemove(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return 0L;
        }
        try {
            return RedisTemplateUtils.getRedisTemplate().sortRemove(cacheKey(cacheName, key), value);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return 0L;
    }

    public List<String> sortSetGetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            Set<String> dataSet = RedisTemplateUtils.getRedisTemplate().sorSetRangeByScore(cacheKey(cacheName, key),
                Double.MIN_VALUE, Double.MAX_VALUE);
            if (dataSet == null) {
                return null;
            }
            return new ArrayList<String>(dataSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    public List<String> sortSetGetAll(String key, int count) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            Set<String> dataSet = RedisTemplateUtils.getRedisTemplate().sorSetRangeByScore(cacheKey(cacheName, key),
                Double.MIN_VALUE, Double.MAX_VALUE);
            if (dataSet == null) {
                return null;
            }
            return new ArrayList<String>(dataSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    /**
     *
     * @param key
     * @param min
     * @return
     */
    public List<String> sortSetGetAll(String key, double min) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            Set<String> dataSet = RedisTemplateUtils.getRedisTemplate().sorSetRangeByScore(cacheKey(cacheName, key),
                min, Double.MAX_VALUE);
            if (dataSet == null) {
                return null;
            }
            return new ArrayList<>(dataSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    public List<String> sortSetGetAll(String key, double min, double max) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            Set<String> dataSet =
                RedisTemplateUtils.getRedisTemplate().sorSetRangeByScore(cacheKey(cacheName, key), min, max);
            if (dataSet == null) {
                return null;
            }
            return new ArrayList<>(dataSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    public List<String> sortSetGetAll(String key, double min, double max, int offset, int count) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            Set<String> dataSet = RedisTemplateUtils.getRedisTemplate().sorSetRangeByScore(cacheKey(cacheName, key),
                min, max, offset, count);
            if (dataSet == null) {
                return null;
            }
            return new ArrayList<String>(dataSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    @Override
    public void putTemporary(String key, Serializable value) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            RedisTemplateUtils.getRedisTemplate().set(cacheKey(cacheName, key), value, 10);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    @Override
    public void remove(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            RedisTemplateUtils.getRedisTemplate().delKey(cacheKey(cacheName, key));
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public String getCacheName() {
        return cacheName;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getTimeToIdleSeconds() {
        return timeToIdleSeconds;
    }

    public Integer getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }
}
