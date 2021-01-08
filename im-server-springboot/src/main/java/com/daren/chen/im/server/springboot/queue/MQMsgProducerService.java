package com.daren.chen.im.server.springboot.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.server.springboot.constant.RabbitConstants;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/30 10:15
 */

@Service
public class MQMsgProducerService implements RabbitTemplate.ConfirmCallback {

    private final Logger log = LoggerFactory.getLogger(MQMsgProducerService.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     *
     */
    private static final Map<String, JSONObject> REQ_MAP = new ConcurrentHashMap<>(16);
    /**
     *
     */
    private static final Map<String, Boolean> RES_MAP = new ConcurrentHashMap<>(16);

    @Autowired
    public MQMsgProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        // rabbitTemplate如果为单例的话，那回调就是最后设置的内容
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    public boolean addChatMsg(JSONObject jsonObject) {
        CorrelationData correlationId = logreq(jsonObject);
        rabbitTemplate.convertAndSend(RabbitConstants.EXCHANGE_IM_CHAT, RabbitConstants.ROUTINGKEY_IM_CHAT, jsonObject,
            correlationId);
        return waitResponse(jsonObject, correlationId);
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    public void updateUserOnlineStatus(JSONObject jsonObject) {
        rabbitTemplate.convertAndSend(RabbitConstants.TOPIC_EXCHANGE_IM_CHAT,
            RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS, jsonObject);
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    public void updateLastMsgId(JSONObject jsonObject) {
        rabbitTemplate.convertAndSend(RabbitConstants.EXCHANGE_IM_CHAT,
            RabbitConstants.ROUTINGKEY_IM_CHAT_UPDATE_LAST_ID, jsonObject);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (correlationData == null) {
            return;
        }
        JSONObject jsonObject = REQ_MAP.remove(correlationData.getId());
        if (null != jsonObject) {
            synchronized (jsonObject) {
                RES_MAP.put(correlationData.getId(), ack);
                jsonObject.notifyAll();
            }
            // 回调方法中不能拿到请求相关的鉴权信息，不记录正常日志
            if (!ack) {
                String logmsg = String.format("confirm requestId=[%s], rid=[%s], cause=[%s]",
                    jsonObject.getString("id"), correlationData.toString(), cause);
                log.info(logmsg);
            }
        }
    }

    /**
     *
     * @param jsonObject
     * @return
     */
    private CorrelationData logreq(JSONObject jsonObject) {
        String id = jsonObject.getString("last_msg_id");
        CorrelationData correlationId = new CorrelationData(id);
        log.debug("{} 消息投递", correlationId.toString());
        REQ_MAP.put(id, jsonObject);
        return correlationId;
    }

    /**
     *
     * @param jsonObject
     * @param correlationId
     * @return
     */
    private boolean waitResponse(JSONObject jsonObject, CorrelationData correlationId) {
        try {
            synchronized (jsonObject) {
                jsonObject.wait(1000);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return RES_MAP.get(correlationId.getId()) == null ? false : RES_MAP.remove(correlationId.getId());
    }
}
