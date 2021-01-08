/**
 *
 */
package com.daren.chen.im.server.processor.handshake;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.core.ws.WsRequestPacket;
import com.daren.chen.im.core.ws.WsResponsePacket;
import com.daren.chen.im.core.ws.WsSessionContext;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年9月11日 下午4:22:36
 */
public class WsHandshakeProcessor extends AbstractProtocolCmdProcessor implements HandshakeCmdProcessor {

    /**
     * 对httpResponsePacket参数进行补充并返回，如果返回null表示不想和对方建立连接，框架会断开连接，如果返回非null，框架会把这个对象发送给对方
     *
     * @param packet
     * @param imChannelContext
     * @return
     * @throws Exception
     * @author: WChao
     */
    @Override
    public ImPacket handshake(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        WsRequestPacket wsRequestPacket = (WsRequestPacket)packet;
        WsSessionContext wsSessionContext = (WsSessionContext)imChannelContext.getSessionContext();
        if (wsRequestPacket.isHandShake()) {
            WsResponsePacket wsResponsePacket = new WsResponsePacket();
            wsResponsePacket.setHandShake(true);
            wsResponsePacket.setCommand(Command.COMMAND_HANDSHAKE_RESP);
            wsSessionContext.setHandshaked(true);
            return wsResponsePacket;
        }
        return null;
    }

    /**
     * 握手成功后
     *
     * @param packet
     * @param imChannelContext
     * @throws Exception
     * @author Wchao
     */
    @Override
    public void onAfterHandshake(ImPacket packet, ImChannelContext imChannelContext) throws ImException {

    }

    /**
     * @Author WChao
     * @Description 判断当前连接是否属于WS协议
     * @param imChannelContext
     * @return boolean
     **/
    @Override
    public boolean isProtocol(ImChannelContext imChannelContext) {
        ImSessionContext sessionContext = imChannelContext.getSessionContext();
        if (sessionContext == null) {
            return false;
        } else if (sessionContext instanceof WsSessionContext) {
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
