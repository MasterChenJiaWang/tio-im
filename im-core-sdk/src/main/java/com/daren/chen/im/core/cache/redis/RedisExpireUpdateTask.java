package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时更新redis的过期时间
 *
 * @author wchao 2017年8月14日 下午1:34:06
 */
public class RedisExpireUpdateTask {
    private static final Logger log = LoggerFactory.getLogger(RedisExpireUpdateTask.class);

    private static boolean started = false;

    private static final LinkedBlockingQueue<ExpireVo> REDIS_EXPIRE_VO_QUEUE = new LinkedBlockingQueue<>();

    public static void add(String cacheName, String key, Serializable value, long expire) {
        ExpireVo expireVo = new ExpireVo(cacheName, key, value, expire);
        REDIS_EXPIRE_VO_QUEUE.offer(expireVo);
    }

    public static void start() {
        if (started) {
            return;
        }
        synchronized (RedisExpireUpdateTask.class) {
            if (started) {
                return;
            }
            started = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PairEx<String, Void, Integer>> l2Datas = new ArrayList<PairEx<String, Void, Integer>>();
                int count = 0;
                while (true) {
                    try {
                        ExpireVo expireVo = REDIS_EXPIRE_VO_QUEUE.poll();
                        if (expireVo != null) {
                            l2Datas.add(RedisTemplateUtils.getRedisTemplate().makePairEx(expireVo.getKey(), null,
                                Integer.parseInt(expireVo.getExpire() + "")));
                            count++;
                        }
                        if (count > 0 && expireVo == null) {
                            log.debug("批量更新缓存过期时间,更新数量:" + l2Datas.size());
                            RedisTemplateUtils.getRedisTemplate().batchSetExpire(l2Datas);
                            l2Datas.clear();
                            count = 0;
                        } else if (count == 0 && expireVo == null) {
                            try {
                                Thread.sleep(1000 * 5);
                            } catch (InterruptedException e) {
                                log.error(e.toString(), e);
                            }
                        }
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }, RedisExpireUpdateTask.class.getName()).start();
    }

    /**
     *
     * @author wchao
     */
    private RedisExpireUpdateTask() {

    }
}
