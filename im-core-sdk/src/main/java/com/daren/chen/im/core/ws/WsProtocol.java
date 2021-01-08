/**
 *
 */
package com.daren.chen.im.core.ws;

import java.nio.ByteBuffer;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.http.HttpRequestDecoder;
import com.daren.chen.im.core.protocol.AbstractProtocol;
import com.daren.chen.im.core.protocol.IProtocolConverter;
import com.daren.chen.im.core.utils.ImKit;

/**
 * WebSocket协议判断器
 *
 * @author WChao
 *
 */
public class WsProtocol extends AbstractProtocol {

    @Override
    public String name() {
        return Protocol.WEB_SOCKET;
    }

    public WsProtocol(IProtocolConverter converter) {
        super(converter);
    }

    @Override
    protected void init(ImChannelContext imChannelContext) {
        imChannelContext.setSessionContext(new WsSessionContext(imChannelContext));
        ImKit.initImClientNode(imChannelContext);
    }

    @Override
    protected boolean validateProtocol(ImSessionContext imSessionContext) throws ImException {
        if (imSessionContext instanceof WsSessionContext) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean validateProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException {
        // 第一次连接;
        HttpRequest request = HttpRequestDecoder.decode(buffer, imChannelContext, false);
        if (request.getHeaders().get(Http.RequestHeaderKey.Sec_WebSocket_Key) != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateProtocol(ImPacket imPacket) throws ImException {
        if (imPacket instanceof WsPacket) {
            return true;
        }
        return false;
    }

}