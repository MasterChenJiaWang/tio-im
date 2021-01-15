package com.daren.chen.im.server.springboot.http;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.daren.chen.im.core.cache.redis.JedisClusterTemplate;
import com.daren.chen.im.core.cache.redis.RedisClusterLock;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2021/1/15 9:32
 */
@SpringBootTest
public class RedisLockTest {
    private static final Logger logger = LoggerFactory.getLogger(RedisLockTest.class);

    @Test
    public void lock() throws Exception {
        test1();

        Thread.sleep(100000);
    }

    private void test1() {

        AtomicInteger n = new AtomicInteger();

        for (int i = 1; i <= 100; i++) {
            new Thread(() -> {
                String s = UUID.randomUUID().toString();
                RedisClusterLock redisClusterLock = null;
                try {
                    redisClusterLock = new RedisClusterLock(JedisClusterTemplate.me().getJedisCluster());

                    Boolean aBoolean = redisClusterLock.setLockOfCluster("lock_test", s, 40000, 10);
                    if (aBoolean) {
                        // 随机暂停时间 有时可能超过 10s
                        int i2 = 3000 + new Random().nextInt(2000);
                        logger.info("线程[{}] 暂停时间= {}", Thread.currentThread().getName(), i2);
                        Thread.sleep(i2);
                        n.getAndIncrement();
                        logger.info("线程[{}] 处理完毕, n = {}", Thread.currentThread().getName(), n.get());
                    } else {
                        logger.info("线程[{}] 获取锁失败!, n = {}", Thread.currentThread().getName(), n.get());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (redisClusterLock != null) {
                        boolean lock_test = redisClusterLock.unLockOfCluster("lock_test", s);
                        if (lock_test) {
                            logger.info("线程[{}] 解锁成功!", Thread.currentThread().getName());
                        } else {
                            logger.info("线程[{}] 解锁失败!", Thread.currentThread().getName());
                        }

                    }
                }

            }, "线程-" + i).start();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
