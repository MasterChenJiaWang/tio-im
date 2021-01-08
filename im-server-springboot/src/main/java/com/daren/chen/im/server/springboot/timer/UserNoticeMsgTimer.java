package com.daren.chen.im.server.springboot.timer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;

import cn.hutool.core.collection.CollectionUtil;

/**
 * 定时发送离线消息
 *
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/6 12:18
 */
@Deprecated
@Component
public class UserNoticeMsgTimer {
    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(UserNoticeMsgTimer.class);

    /**
     *
     */
    // @Scheduled(cron = "0 */10 * * * ?")
    public void sendNoticeMsg() {

        try {
            List<ImChannelContext> all = JimServerAPI.findAll();
            if (CollectionUtil.isEmpty(all)) {
                return;
            }
            //
            for (ImChannelContext imChannelContext : all) {
                String userId = imChannelContext.getUserId();
                if (StringUtils.isBlank(userId)) {
                    continue;
                }
                ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
                MessageHelper messageHelper = imServerConfig.getMessageHelper();
                //
                notice(imChannelContext, userId, messageHelper);
                //
                offlineMesage(imChannelContext, userId, messageHelper);
            }
        } catch (ImException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void notice(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper)
        throws ImException {
        ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody =
            messageHelper.receiveMsgNotice(imChannelContext.getUserId(), userId);
        if (receiveMsgNoticeRespBody == null) {
            return;
        }
        List<MsgNoticeRespBody> list = receiveMsgNoticeRespBody.getList();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        ReceiveMsgNoticeRespBody success = ReceiveMsgNoticeRespBody.success(receiveMsgNoticeRespBody);
        // 发送通知给 客户
        JimServerAPI.sendToUser(userId,
            ProtocolManager.Converter.respPacket(ReceiveMsgNoticeRespBody.success(success), imChannelContext));
        log.info("上下文ID [{}] 用户ID [{}]   定时发送通知消息 消息 【{}】", imChannelContext.getId(), userId,
            JSON.toJSONString(success));
    }

    /**
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void offlineMesage(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper)
        throws ImException {
        UserMessageData messageData = messageHelper.getFriendsOfflineMessage(imChannelContext.getUserId(), userId);
        if (messageData == null) {
            return;
        }
        if (CollectionUtil.isEmpty(messageData.getGroups()) && CollectionUtil.isEmpty(messageData.getFriends())) {
            return;
        }
        RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10015.getText());
        resPacket.setData(messageData);
        ImPacket imPacket = ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        // 发送通知给 客户
        JimServerAPI.sendToUser(userId, imPacket);
        log.info("上下文ID [{}] 用户ID [{}]   定时发送离线消息 消息 【{}】", imChannelContext.getId(), userId,
            JSON.toJSONString(messageData));
    }
}
