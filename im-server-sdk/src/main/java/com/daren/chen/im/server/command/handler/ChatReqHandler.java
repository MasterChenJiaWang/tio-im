package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.queue.MsgAndAckQueueRunnable;
import com.daren.chen.im.server.util.ChatKit;

/**
 * 版本: [1.0] 功能说明: 聊天请求cmd消息命令处理器
 *
 * @author : WChao 创建时间: 2017年9月22日 下午2:58:59
 */
public class ChatReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        try {
            ImServerChannelContext imServerChannelContext = (ImServerChannelContext)channelContext;
            if (packet.getBody() == null) {
                return ProtocolManager.Packet.dataInCorrect(channelContext);
            }
            ChatBody chatBody = ChatKit.toChatBody(packet.getBody(), channelContext);
            if (chatBody == null) {
                return ProtocolManager.Packet.dataInCorrect(channelContext);
            }
            String userId = imServerChannelContext.getUserId();
            String from = chatBody.getFrom();
            if (StringUtils.isBlank(from)) {
                return ProtocolManager.Packet.dataInCorrect(channelContext);
            }
            if (!from.equalsIgnoreCase(userId)) {
                return ProtocolManager.Packet.dataInCorrect(channelContext);
            }
            packet.setBody(chatBody.toByte());
            // 聊天数据格式不正确
            if (ChatType.forNumber(chatBody.getChatType()) == null) {
                return ProtocolManager.Packet.dataInCorrect(channelContext);
            }
            // 异步调用业务处理消息接口
            MsgAndAckQueueRunnable msgAndAckQueueRunnable = getMsgAndAckQueueRunnable(imServerChannelContext);
            msgAndAckQueueRunnable.addMsg(chatBody);
            msgAndAckQueueRunnable.executor.execute(msgAndAckQueueRunnable);
            ImPacket chatPacket =
                new ImPacket(Command.COMMAND_CHAT_REQ, new RespBody(Command.COMMAND_CHAT_RESP, chatBody).toByte());
            // 设置同步序列号;
            chatPacket.setSynSeq(packet.getSynSeq());
            ImServerConfig imServerConfig = ImConfig.Global.get();
            boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
            // 私聊
            if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatBody.getChatType()) {
                String toId = chatBody.getTo();
                // JimServerAPI.sendToUser(toId, chatPacket);
                // JimServerAPI.sendPushByUser(chatBody, imServerConfig);
                if (ChatKit.isOnline(toId, isStore)) {
                    JimServerAPI.sendToUser(toId, chatPacket);
                    // 发送成功响应包
                    // return ProtocolManager.Packet.success(channelContext, packet);
                    return null;
                } else {
                    // 用户不在线响应包
                    // return ProtocolManager.Packet.offline(channelContext, packet);
                }

                return null;
                // 群聊
            } else if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()) {
                String groupId = chatBody.getGroupId();
                JimServerAPI.sendToGroup(groupId, chatPacket);
                // 发送成功响应包
                // return ProtocolManager.Packet.success(channelContext, packet);
                return null;
            }
            return ProtocolManager.Packet.fail(channelContext, packet);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Packet.fail(channelContext, packet);
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_CHAT_REQ;
    }

    /**
     * 获取聊天业务处理异步消息队列
     *
     * @param imServerChannelContext
     *            IM通道上下文
     * @return
     */
    private MsgAndAckQueueRunnable getMsgAndAckQueueRunnable(ImServerChannelContext imServerChannelContext) {
        MsgAndAckQueueRunnable msgAndAckQueueRunnable =
            (MsgAndAckQueueRunnable)imServerChannelContext.getMsgAndAckQue();
        if (Objects.nonNull(msgAndAckQueueRunnable.getProtocolCmdProcessor())) {
            return msgAndAckQueueRunnable;
        }
        synchronized (MsgAndAckQueueRunnable.class) {
            msgAndAckQueueRunnable.setProtocolCmdProcessor(this.getSingleProcessor());
        }
        return msgAndAckQueueRunnable;
    }

}
