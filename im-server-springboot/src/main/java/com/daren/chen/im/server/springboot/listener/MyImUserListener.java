package com.daren.chen.im.server.springboot.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeReq;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.listener.AbstractImUserListener;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.springboot.service.ReceiveNoticeServiceProcessor;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-02 18:18
 */
public class MyImUserListener extends AbstractImUserListener {

    private static final Logger logger = LoggerFactory.getLogger(MyImUserListener.class);

    @Override
    public void doAfterBind(ImChannelContext imChannelContext, User user) throws ImException {
        // logger.info("绑定用户:{}", JSONObject.toJSONString(user));
        //
        // String userId = user.getUserId();
        // if (StringUtils.isNotBlank(userId)) {
        // initApplyNotice(userId, imChannelContext);
        // }
        // ImServerConfig imServerConfig = ImConfig.Global.get();
        // MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // //
        // offlineMesage(imChannelContext, user.getId(), messageHelper);
        // //
        // notice(imChannelContext, user.getId(), messageHelper);
    }

    @Override
    public void doAfterUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        // logger.info("解绑用户:{}", JSONObject.toJSONString(user));
    }

    /**
     * 初始化用户登录的好友请求通知
     *
     * @param userId
     * @param imChannelContext
     * @throws ImException
     */
    private void initApplyNotice(String userId, ImChannelContext imChannelContext) throws ImException {
        ReceiveNoticeServiceProcessor receiveNoticeServiceProcessor = new ReceiveNoticeServiceProcessor();
        ReceiveMsgNoticeReq receiveMsgNoticeReq = new ReceiveMsgNoticeReq();
        receiveMsgNoticeReq.setUserId(userId);
        ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody =
            receiveNoticeServiceProcessor.receiveMsgNotice(receiveMsgNoticeReq, imChannelContext);

        JimServerAPI.sendToUser(userId,
            ProtocolManager.Converter.respPacket(receiveMsgNoticeRespBody, imChannelContext));
    }

    /**
     *
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void offlineMesage(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper) {
        try {
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
        } catch (ImException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void notice(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper) {
        try {
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
        } catch (ImException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

}
