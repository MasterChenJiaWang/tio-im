package com.daren.chen.im.core.cache.redis;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/22 14:25
 */
public class RedisTemplateUtils {

    /**
     * 是否集群
     */
    private static Boolean cluster;

    public static void setCluster(boolean cluster) {
        RedisTemplateUtils.cluster = cluster;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public static BaseJedisTemplate getRedisTemplate() throws Exception {
        if (cluster == null) {
            throw new Exception("RedisTemplateUtils 未初始化!");
        }
        try {
            // if (cluster) {
            // return JedisClusterTemplate.me();
            // }
            // return JedisTemplate.me();
            return RedissonTemplate.me();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("JedisTemplate 初始化失败!");
        }
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public static BaseJedisTemplate getRedisTemplate(boolean cluster) throws Exception {
        try {
            // if (cluster) {
            // return JedisClusterTemplate.me();
            // }
            // return JedisTemplate.me();
            return RedissonTemplate.me();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("JedisTemplate 初始化失败!");
        }
    }
}
