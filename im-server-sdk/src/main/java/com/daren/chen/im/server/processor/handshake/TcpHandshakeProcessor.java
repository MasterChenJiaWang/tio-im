/**
 *
 */
package com.daren.chen.im.server.processor.handshake;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.HandshakeBody;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.tcp.TcpSessionContext;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年9月11日 下午8:11:34
 */
public class TcpHandshakeProcessor extends AbstractProtocolCmdProcessor implements HandshakeCmdProcessor {

    @Override
    public ImPacket handshake(ImPacket packet, ImChannelContext channelContext) throws ImException {
        RespBody handshakeBody =
            new RespBody(Command.COMMAND_HANDSHAKE_RESP, new HandshakeBody(Protocol.HANDSHAKE_BYTE));
        return ProtocolManager.Converter.respPacket(handshakeBody, channelContext);
    }

    /**
     * 握手成功后
     *
     * @param packet
     * @param channelContext
     * @throws ImException
     * @author Wchao
     */
    @Override
    public void onAfterHandshake(ImPacket packet, ImChannelContext channelContext) throws ImException {

    }

    @Override
    public boolean isProtocol(ImChannelContext channelContext) {
        ImSessionContext sessionContext = channelContext.getSessionContext();
        if (sessionContext == null) {
            return false;
        } else if (sessionContext instanceof TcpSessionContext) {
            return true;
        }
        return false;
    }

    @Override
    public void chatAck(ImChannelContext imChannelContext, Message message) {

    }

    @Override
    public void noticeAck(ImChannelContext imChannelContext, Message message) {

    }
}
