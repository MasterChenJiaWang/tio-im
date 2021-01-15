package com.daren.chen.im.core.cache.redis;

import java.util.Collections;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2021/1/6 18:04
 */
public class RedisClusterLock {
    private final static Logger logger = LoggerFactory.getLogger(RedisClusterLock.class);

    /**
     * 锁的过期时间(单位：秒)
     */
    private static final int LOCK_EXPIRE_TIME = 30;

    /**
     * 最大尝试次数
     */
    private static final int MAX_ATTEMPTS = 100;

    /**
     * key前缀
     */
    private static final String KEY_PRE = "REDIS_LOCK_";
    /** 加锁标志 */
    public static final String LOCKED = "TRUE";
    /** 毫秒与毫微秒的换算单位 1毫秒 = 1000000毫微秒 */
    public static final long MILLI_NANO_CONVERSION = 1000 * 1000L;
    /** 默认超时时间（毫秒） */
    public static final long DEFAULT_TIME_OUT = 1000;
    public static final Random RANDOM = new Random();
    /** 锁的超时时间（秒），过期删除 */
    public static final int EXPIRE = 3 * 60;
    private static final Long RELEASE_SUCCESS = 1L;
    /**
     *
     */
    private Jedis jedis;
    /**
     *
     */
    private final JedisCluster jedisCluster;

    public RedisClusterLock(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    /**
     *
     * @param lockKey
     * @param value
     * @param expire
     * @return
     */
    public Boolean setLock(String lockKey, String value, int expire) {
        return setLock(lockKey, value, 2000, expire);
    }

    /**
     * 加锁 key不存在时才设置key value
     *
     * @param lockKey
     *            加锁键
     * @param value
     *            加锁客户端唯一标识
     * @param timeout
     *            等待时间 毫秒
     * @param expire
     *            锁过期时间
     * @return
     */
    public Boolean setLock(String lockKey, String value, long timeout, int expire) {
        long nano = System.nanoTime();
        timeout *= MILLI_NANO_CONVERSION;
        try {
            while ((System.nanoTime() - nano) < timeout) {
                if (this.jedis.setnx(lockKey, value) == 1) {
                    this.jedis.expire(lockKey, expire);
                    return Boolean.TRUE;
                }
                // 短暂休眠，避免出现活锁
                Thread.sleep(3, RANDOM.nextInt(500));
                return false;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Locking error", e);
        }
    }

    /**
     *
     * @param lockKey
     * @param value
     * @param expire
     *            秒
     * @return
     */
    public Boolean setLockOfCluster(String lockKey, String value, int expire) {
        return setLockOfCluster(lockKey, value, 10000, expire);
    }

    /**
     * 加锁 key不存在时才设置key value
     *
     * @param lockKey
     *            加锁键
     * @param value
     *            加锁客户端唯一标识
     * @param timeout
     *            等待时间 毫秒
     * @param expire
     *            锁过期时间 秒
     * @return
     */
    public Boolean setLockOfCluster(String lockKey, String value, long timeout, int expire) {
        long nano = System.nanoTime();
        timeout *= MILLI_NANO_CONVERSION;
        try {
            while ((System.nanoTime() - nano) < timeout) {
                if (this.jedisCluster.setnx(lockKey, value) == 1) {
                    this.jedisCluster.expire(lockKey, expire);
                    return true;
                }
                // 短暂休眠，避免出现活锁
                Thread.sleep(20, RANDOM.nextInt(500));
            }
            logger.info("线程 [{}] ,总共耗时 {}", Thread.currentThread().getName(), (System.nanoTime() - nano) / 1000000000);
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Locking error", e);
        }
    }

    /**
     * 解锁
     *
     * @param lockKey
     *            加锁键
     * @param value
     * @return
     */
    public boolean unLock(String lockKey, String value) {
        try {
            String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object res = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(value));
            return RELEASE_SUCCESS.equals(res);

        } catch (Exception e) {
            logger.error("释放Redis锁异常：", e);
        } finally {
            if (this.jedis != null) {
                this.jedis.close();
            }
        }

        return false;
    }

    /**
     * 解锁
     *
     * @param lockKey
     *            加锁键
     * @param value
     * @return
     */
    public boolean unLockOfCluster(String lockKey, String value) {
        try {
            String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object res =
                jedisCluster.eval(script, Collections.singletonList(lockKey), Collections.singletonList(value));
            return RELEASE_SUCCESS.equals(res);

        } catch (Exception e) {
            logger.error("释放Redis锁异常：", e);
        }

        return false;
    }

}
