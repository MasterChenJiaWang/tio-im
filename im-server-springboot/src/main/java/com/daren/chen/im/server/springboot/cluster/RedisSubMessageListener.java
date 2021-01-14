package com.daren.chen.im.server.springboot.cluster;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Tio;
import org.tio.utils.json.Json;

import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cluster.ImClusterVO;
import com.daren.chen.im.server.JimServerAPI;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/1 17:07
 */
public class RedisSubMessageListener implements MessageListener<ImClusterVO> {

    private static final Logger log = LoggerFactory.getLogger(RedisSubMessageListener.class);

    /**
     *
     * @param channel
     * @param imClusterVo
     */
    @Override
    public void onMessage(CharSequence channel, ImClusterVO imClusterVo) {
        // if (StringUtils.isBlank(data)) {
        // log.error("data is null");
        // return;
        // }
        // log.info("data = {}", data);
        // ImClusterVO imClusterVo = JSONUtil.toBean(data, ImClusterVO.class);
        String clientId = imClusterVo.getClientId();
        if (StringUtils.isBlank(clientId)) {
            log.error("clientId is null");
            return;
        }
        if (Objects.equals(ImClusterVO.CLIENT_ID, clientId)) {
            return;
        }
        ImPacket packet = imClusterVo.getPacket();
        if (packet == null) {
            log.error("packet is null");
            return;
        }
        log.info("收到topic:{}, ImClusterVo:{}", channel, Json.toJson(imClusterVo));
        packet.setFromCluster(true);
        // 发送给所有
        boolean isToAll = imClusterVo.isToAll();
        if (isToAll) {
            Tio.sendToAll(null, packet);
        }
        // 发送给指定组
        String group = imClusterVo.getGroup();
        if (StringUtils.isNotBlank(group)) {
            JimServerAPI.sendToGroup(group, packet);
        }
        // 发送给指定用户
        String userId = imClusterVo.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            JimServerAPI.sendToUser(userId, packet);
        }
        // 发送给指定token
        String token = imClusterVo.getToken();
        if (StringUtils.isNotBlank(token)) {
            // Tio.sendToToken(me.groupContext, token, packet);
        }
        // 发送给指定ip
        String ip = imClusterVo.getIp();
        if (StringUtils.isNotBlank(ip)) {
            JimServerAPI.sendToIp(ip, packet);
        }
    }
}
