/**
 *
 */
package com.daren.chen.im.server.cluster.redis;

import java.util.concurrent.atomic.AtomicLong;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cluster.ImClusterConfig;

/**
 * @desc Redis集群配置
 * @author WChao
 * @date 2020-05-01
 */
public class RedisClusterConfig extends ImClusterConfig implements ImConst {

    private static final Logger log = LoggerFactory.getLogger(RedisClusterConfig.class);
    /**
     * 集群主题后缀
     */
    private String topicSuffix;
    /**
     * 集群订阅主题
     */
    private String topic;
    /**
     * 客户端
     */
    private RedissonClient redissonClient;

    /**
     * Redis发布/订阅Topic
     */
    public RTopic rTopic;

    /**
     * 收到了多少次topic
     */
    public static final AtomicLong RECEIVED_TOPIC_COUNT = new AtomicLong();

    /**
     * JIM内置的集群是用redis的topic来实现的，所以不同机器就要有一个不同的topicSuffix
     *
     * @param topicSuffix
     *            不同类型的就要有一个不同的topicSuffix
     * @param redissonClient
     *            redis客户端
     * @return
     * @author: WChao
     */
    public static RedisClusterConfig newInstance(String topicSuffix, RedissonClient redissonClient,
        MessageListener messageListener) {
        if (redissonClient == null) {
            throw new RuntimeException(RedissonClient.class.getSimpleName() + "不允许为空");
        }
        RedisClusterConfig me = new RedisClusterConfig(topicSuffix, redissonClient);
        me.rTopic = redissonClient.getTopic(me.topic);
        // 改成使用String 是因为 redisson 的 编码方式改成了 StringCodec,对应的 messageListener 中的接收也用 string
        me.rTopic.addListener(String.class, messageListener);
        return me;
    }

    private RedisClusterConfig(String topicSuffix, RedissonClient redissonClient) {
        this.setTopicSuffix(topicSuffix);
        this.setRedissonClient(redissonClient);
    }

    @Override
    public void send(String message) {
        rTopic.publish(message);
    }

    @Override
    public void sendAsync(String message) {
        rTopic.publishAsync(message);
    }

    public String getTopicSuffix() {
        return topicSuffix;
    }

    public void setTopicSuffix(String topicSuffix) {
        this.topicSuffix = topicSuffix;
        this.topic = topicSuffix + Topic.JIM_CLUSTER_TOPIC;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RTopic getRTopic() {
        return rTopic;
    }

    public void setRTopic(RTopic rTopic) {
        this.rTopic = rTopic;
    }

}
