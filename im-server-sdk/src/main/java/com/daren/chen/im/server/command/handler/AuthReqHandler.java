package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.AuthReqBody;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.auth.AuthCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 *
 * 版本: [1.0] 功能说明: 鉴权请求消息命令处理器
 *
 * @author : WChao 创建时间: 2017年9月13日 下午1:39:35
 */
public class AuthReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            if (packet.getBody() == null) {
                RespBody respBody = new RespBody(Command.COMMAND_AUTH_RESP, ImStatus.C10010.getText());
                return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
            }
            AuthRespBody authRespBody = null;
            AuthCmdProcessor authCmdProcessor = this.getSingleProcessor(AuthCmdProcessor.class);
            AuthReqBody authReqBody = JsonKit.toBean(packet.getBody(), AuthReqBody.class);
            if (authReqBody == null) {
                log.error("上下文ID [{}] 用户ID [{}]   请求体为空", imChannelContext.getId(), imChannelContext.getUserId());
                authRespBody = AuthRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(authRespBody, imChannelContext);
            }
            if (Objects.nonNull(authCmdProcessor)) {
                authRespBody = authCmdProcessor.checkToken(authReqBody, imChannelContext);
                authRespBody = AuthRespBody.success().setData(authRespBody);
                return ProtocolManager.Converter.respPacket(authRespBody, imChannelContext);
            }
            RespBody respBody = new RespBody(Command.COMMAND_AUTH_RESP, ImStatus.C10009.getText()).setData(authReqBody);
            return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(AuthRespBody.failed().setMsg(e.getMessage()), imChannelContext);
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_AUTH_REQ;
    }
}
