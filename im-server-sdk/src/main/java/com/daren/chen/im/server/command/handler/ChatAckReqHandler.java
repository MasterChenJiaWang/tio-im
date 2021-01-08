package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.queue.MsgAndAckQueueRunnable;

/**
 * 版本: [1.0] 功能说明: 消息通知cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class ChatAckReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatAckReqHandler.class);

    /**
     *
     */

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            ChatAckBody chatAckBody = JsonKit.toBean(packet.getBody(), ChatAckBody.class);
            if (chatAckBody == null) {
                log.error("上下文ID [{}] 用户ID [{}]   msgNoticeReq is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
            // 异步调用业务处理消息接口
            MsgAndAckQueueRunnable msgAndAckQueueRunnable = getMsgAndAckQueueRunnable(imServerChannelContext);
            msgAndAckQueueRunnable.addMsg(chatAckBody);
            msgAndAckQueueRunnable.executor.execute(msgAndAckQueueRunnable);
            // 没有通知成功
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Command command() {
        return Command.CHAT_ACK_REQ;
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
