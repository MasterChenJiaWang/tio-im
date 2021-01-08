package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.ws.WsSessionContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.handshake.HandshakeCmdProcessor;

/**
 * 版本: [1.0] 功能说明: 心跳cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class HandshakeReqHandler extends AbstractCmdHandler {
    private static final Logger log = LoggerFactory.getLogger(HandshakeReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {

        try {
            HandshakeCmdProcessor handshakeProcessor =
                this.getMultiProcessor(channelContext, HandshakeCmdProcessor.class);
            if (Objects.isNull(handshakeProcessor)) {
                JimServerAPI.remove(channelContext, "没有对应的握手协议处理器HandshakeCmdProcessor...");
                return null;
            }
            ImPacket handShakePacket = handshakeProcessor.handshake(packet, channelContext);
            if (handShakePacket == null) {
                JimServerAPI.remove(channelContext, "业务层不同意握手");
                return null;
            }
            JimServerAPI.send(channelContext, handShakePacket);
            WsSessionContext wsSessionContext = (WsSessionContext)channelContext.getSessionContext();
            HttpRequest request = wsSessionContext.getHandshakeRequestPacket();
            handshakeProcessor.onAfterHandshake(request, channelContext);
            return null;
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_HANDSHAKE_REQ;
    }
}
