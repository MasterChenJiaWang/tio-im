/**
 *
 */
package com.daren.chen.im.server.cluster.redis;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cluster.ImCluster;
import com.daren.chen.im.core.cluster.ImClusterVO;

/**
 * @desc 默认Redis集群配置
 * @author WChao
 * @date 2020-05-01
 */
public class RedisCluster extends ImCluster {

    /**
     * Redis集群构造器
     *
     * @param clusterConfig
     *            集群配置
     */
    public RedisCluster(RedisClusterConfig clusterConfig) {
        super(clusterConfig);
    }

    @Override
    public void clusterToUser(String userId, ImPacket packet) {
        if (clusterConfig.isCluster2user()) {
            ImClusterVO imClusterVo = new ImClusterVO(packet);
            imClusterVo.setUserId(userId);
            clusterConfig.sendAsync(JSON.toJSONString(imClusterVo));
        }
    }

    @Override
    public void clusterToGroup(String group, ImPacket packet) {
        if (clusterConfig.isCluster2group()) {
            ImClusterVO imClusterVo = new ImClusterVO(packet);
            imClusterVo.setGroup(group);
            clusterConfig.sendAsync(JSON.toJSONString(imClusterVo));
        }
    }

    @Override
    public void clusterToIp(String ip, ImPacket packet) {
        if (clusterConfig.isCluster2ip()) {
            ImClusterVO imClusterVo = new ImClusterVO(packet);
            imClusterVo.setIp(ip);
            clusterConfig.sendAsync(JSON.toJSONString(imClusterVo));
        }
    }

    @Override
    public void clusterToChannelId(String channelId, ImPacket packet) {
        if (clusterConfig.isCluster2channelId()) {
            ImClusterVO imClusterVo = new ImClusterVO(packet);
            imClusterVo.setChannelId(channelId);
            clusterConfig.sendAsync(JSON.toJSONString(imClusterVo));
        }
    }

}
