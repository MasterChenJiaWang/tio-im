package com.daren.chen.im.server.springboot.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.json.Json;

import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cache.redis.RedissonTemplate;
import com.daren.chen.im.core.cluster.ImCluster;
import com.daren.chen.im.core.cluster.ImClusterVO;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.cluster.redis.RedisCluster;
import com.daren.chen.im.server.cluster.redis.RedisClusterConfig;
import com.daren.chen.im.server.config.ImServerConfig;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/1 17:07
 */
public class RedisAddTopicMessageListener implements MessageListener<ImClusterVO> {

    private static final Logger log = LoggerFactory.getLogger(RedisAddTopicMessageListener.class);

    /**
     *
     * @param channel
     * @param imClusterVo
     */
    @Override
    public void onMessage(CharSequence channel, ImClusterVO imClusterVo) {
        String clientId = imClusterVo.getClientId();
        if (StringUtils.isBlank(clientId)) {
            log.error("clientId is null");
            return;
        }
        if (Objects.equals(ImClusterVO.CLIENT_ID, clientId)) {
            return;
        }
        log.info("收到添加topic:{},  ImClusterVo:{}", channel, Json.toJson(imClusterVo));
        ImPacket packet = imClusterVo.getPacket();
        if (packet == null) {
            log.error("packet is null");
            return;
        }
        byte[] body = packet.getBody();
        if (body == null) {
            return;
        }
        String newTopicName = JsonKit.toBean(body, String.class);
        ImServerConfig imServerConfig = ImConfig.Global.get();
        if (ImServerConfig.ON.equalsIgnoreCase(imServerConfig.getIsCluster())) {
            try {
                RedisCluster redisCluster = new RedisCluster(RedisClusterConfig.newInstance(newTopicName,
                    RedissonTemplate.me().getRedissonClient(), imServerConfig.getMessageListener()));
                List<ImCluster> clusters = imServerConfig.getClusters();
                if (CollectionUtil.isEmpty(clusters)) {
                    clusters = new ArrayList<>();
                }
                clusters.add(redisCluster);
                imServerConfig.setClusters(clusters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
