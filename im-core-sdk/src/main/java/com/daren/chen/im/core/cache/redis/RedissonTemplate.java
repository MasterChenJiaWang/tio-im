package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImConst;

/**
 * @author WChao
 * @date 2018年5月18日 下午2:46:55
 */
public class RedissonTemplate implements Serializable {

    private static final long serialVersionUID = -4528751601700736437L;
    private static final Logger logger = LoggerFactory.getLogger(RedissonTemplate.class);
    private static volatile RedissonTemplate instance = null;
    private static RedisConfiguration redisConfig = null;
    private static final String REDIS = "redis";
    private static RedissonClient redissonClient = null;

    private RedissonTemplate() {};

    public static RedissonTemplate me() throws Exception {
        if (instance == null) {
            synchronized (RedissonTemplate.class) {
                if (instance == null) {
                    redisConfig = RedisConfigurationFactory.parseConfiguration();
                    init();
                    instance = new RedissonTemplate();
                }
            }
        }
        return instance;
    }

    private static void init() throws Exception {
        try {
            if (redisConfig.isCluster()) {
                String clusterAddr = redisConfig.getClusterAddr();
                if (StringUtils.isBlank(clusterAddr)) {
                    logger.error("the server Cluster Addr of redis must be not null!");
                    throw new Exception("the server Cluster Addr of redis must be not null!");
                }
                Config redissonConfig = new Config();
                ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers();
                String[] split = clusterAddr.split(",");
                if (split.length == 0) {
                    throw new Exception("the server Cluster Addr of redis must be not null!");
                }
                String[] strings = new String[split.length];
                for (int i = 0; i < split.length; i++) {
                    strings[i] = REDIS + "://" + split[i];
                }
                clusterServersConfig.addNodeAddress(strings).setPassword(redisConfig.getAuth())
                    .setTimeout(redisConfig.getTimeout()).setRetryAttempts(redisConfig.getRetryNum());
                redissonClient = Redisson.create(redissonConfig);
            } else {
                String host = redisConfig.getHost();
                logger.info("connect redis[{}]", ImConst.JIM);
                if (host == null) {
                    logger.error("the server ip of redis must be not null!");
                    throw new Exception("the server ip of redis must be not null!");
                }
                int port = redisConfig.getPort();
                String password = redisConfig.getAuth();
                Config redissonConfig = new Config();
                SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
                singleServerConfig.setAddress(REDIS + "://" + host + ":" + port).setPassword(password)
                    .setTimeout(redisConfig.getTimeout()).setRetryAttempts(redisConfig.getRetryNum());
                redissonClient = Redisson.create(redissonConfig);
            }

        } catch (Exception e) {
            logger.error("can't create RedissonClient for server" + redisConfig.getHost());
            throw new Exception("can't create RedissonClient for server" + redisConfig.getHost());
        }

    }

    /**
     * 获取RedissonClient客户端;
     *
     * @return
     */
    public final RedissonClient getRedissonClient() {
        return redissonClient;
    }
}
