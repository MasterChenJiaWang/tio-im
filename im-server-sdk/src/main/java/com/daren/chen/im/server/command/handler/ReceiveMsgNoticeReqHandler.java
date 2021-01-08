package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeReq;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.notice.ReceiveMsgNoticeCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 消息通知cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class ReceiveMsgNoticeReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(ReceiveMsgNoticeReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody = null;
            ReceiveMsgNoticeReq receiveMsgNoticeReq = JsonKit.toBean(packet.getBody(), ReceiveMsgNoticeReq.class);
            if (receiveMsgNoticeReq == null) {
                log.error("上下文ID [{}] 用户ID [{}]   receiveMsgNoticeReq is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                receiveMsgNoticeRespBody = ReceiveMsgNoticeRespBody.failed().setMsg("receiveMsgNoticeReq is null");
                return ProtocolManager.Converter.respPacket(receiveMsgNoticeRespBody, imChannelContext);
            }
            // 实际绑定之前执行处理器动作
            ReceiveMsgNoticeCmdProcessor receiveMsgNoticeCmdProcessor =
                this.getSingleProcessor(ReceiveMsgNoticeCmdProcessor.class);
            if (Objects.nonNull(receiveMsgNoticeCmdProcessor)) {
                receiveMsgNoticeRespBody =
                    receiveMsgNoticeCmdProcessor.receiveMsgNotice(receiveMsgNoticeReq, imChannelContext);
                JimServerAPI.sendToUser(receiveMsgNoticeReq.getUserId(), ProtocolManager.Converter
                    .respPacket(ReceiveMsgNoticeRespBody.success(receiveMsgNoticeRespBody), imChannelContext));
                return null;
            }
            JimServerAPI.sendToUser(receiveMsgNoticeReq.getUserId(),
                ProtocolManager.Converter.respPacket(ReceiveMsgNoticeRespBody.failed(), imChannelContext));
            return null;
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(ReceiveMsgNoticeRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    @Override
    public Command command() {
        return Command.RECEIVE_MSG_NOTICE_REQ;
    }
}
