package com.daren.chen.im.server.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.CloseReqBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 关闭请求cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class CloseReqHandler extends AbstractCmdHandler {
    private static final Logger log = LoggerFactory.getLogger(CloseReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            CloseReqBody closeReqBody;
            try {
                closeReqBody = JsonKit.toBean(packet.getBody(), CloseReqBody.class);
            } catch (Exception e) {
                // 关闭请求消息格式不正确
                return ProtocolManager.Converter
                    .respPacket(new RespBody(Command.COMMAND_CLOSE_REQ, ImStatus.C10020.getText()), imChannelContext);
            }
            JimServerAPI.bSend(imChannelContext, ProtocolManager.Converter
                .respPacket(new RespBody(Command.COMMAND_CLOSE_REQ, ImStatus.C10021.getText()), imChannelContext));
            if (closeReqBody == null || closeReqBody.getUserId() == null) {
                JimServerAPI.remove(imChannelContext, "收到关闭请求");
            } else {
                String userId = closeReqBody.getUserId();
                JimServerAPI.remove(userId, "收到关闭请求!");
            }
            return null;
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_CLOSE_REQ;
    }
}
