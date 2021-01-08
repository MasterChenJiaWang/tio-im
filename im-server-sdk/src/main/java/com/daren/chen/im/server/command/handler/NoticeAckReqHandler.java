package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.NoticeAckBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.queue.NoticeAckQueueRunnable;

/**
 * 版本: [1.0] 功能说明: 消息通知cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class NoticeAckReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(NoticeAckReqHandler.class);

    /**
     *
     */

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            NoticeAckBody noticeAckBody = JsonKit.toBean(packet.getBody(), NoticeAckBody.class);
            if (noticeAckBody == null) {
                log.error("上下文ID [{}] 用户ID [{}]   noticeAckBody is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
            // 异步调用业务处理消息接口
            NoticeAckQueueRunnable msgQueueRunnable = getMsgQueueRunnable(imServerChannelContext);
            msgQueueRunnable.addMsg(noticeAckBody);
            msgQueueRunnable.executor.execute(msgQueueRunnable);
            // 没有通知成功
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Command command() {
        return Command.NOTICE_ACK_REQ;
    }

    /**
     * 获取聊天业务处理异步消息队列
     *
     * @param imServerChannelContext
     *            IM通道上下文
     * @return
     */
    private NoticeAckQueueRunnable getMsgQueueRunnable(ImServerChannelContext imServerChannelContext) {
        NoticeAckQueueRunnable noticeAckQueueRunnable =
            (NoticeAckQueueRunnable)imServerChannelContext.getNoticeAckQue();
        if (Objects.nonNull(noticeAckQueueRunnable.getProtocolCmdProcessor())) {
            return noticeAckQueueRunnable;
        }
        synchronized (NoticeAckQueueRunnable.class) {
            noticeAckQueueRunnable.setProtocolCmdProcessor(this.getSingleProcessor());
        }
        return noticeAckQueueRunnable;
    }
}
