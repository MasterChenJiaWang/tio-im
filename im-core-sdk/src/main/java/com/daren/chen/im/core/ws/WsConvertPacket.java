/**
 *
 */
package com.daren.chen.im.core.ws;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.protocol.IProtocolConverter;

/**
 * Ws协议消息转化包
 *
 * @author WChao
 *
 */
public class WsConvertPacket implements IProtocolConverter {

    /**
     * WebSocket响应包;
     */
    @Override
    public ImPacket RespPacket(byte[] body, Command command, ImChannelContext channelContext) {
        ImSessionContext sessionContext = channelContext.getSessionContext();
        // 转ws协议响应包;
        if (sessionContext instanceof WsSessionContext) {
            WsResponsePacket wsResponsePacket = new WsResponsePacket();
            wsResponsePacket.setBody(body);
            wsResponsePacket.setWsOpcode(Opcode.TEXT);
            wsResponsePacket.setCommand(command);
            return wsResponsePacket;
        }
        return null;
    }

    @Override
    public ImPacket RespPacket(ImPacket imPacket, Command command, ImChannelContext imChannelContext) {

        return this.RespPacket(imPacket.getBody(), command, imChannelContext);
    }

    @Override
    public ImPacket ReqPacket(byte[] body, Command command, ImChannelContext channelContext) {
        return null;
    }
}
