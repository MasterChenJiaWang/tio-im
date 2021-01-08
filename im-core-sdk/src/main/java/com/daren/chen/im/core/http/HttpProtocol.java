/**
 *
 */
package com.daren.chen.im.core.http;

import java.nio.ByteBuffer;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.http.session.HttpSession;
import com.daren.chen.im.core.protocol.AbstractProtocol;
import com.daren.chen.im.core.protocol.IProtocolConverter;
import com.daren.chen.im.core.utils.ImKit;

/**
 *
 * @desc Http协议校验器
 * @author WChao
 * @date 2018-05-01
 */
public class HttpProtocol extends AbstractProtocol {

    @Override
    public String name() {
        return Protocol.HTTP;
    }

    public HttpProtocol(IProtocolConverter protocolConverter) {
        super(protocolConverter);
    }

    @Override
    protected void init(ImChannelContext imChannelContext) {
        imChannelContext.setSessionContext(new HttpSession(imChannelContext));
        ImKit.initImClientNode(imChannelContext);
    }

    @Override
    public boolean validateProtocol(ImSessionContext imSessionContext) throws ImException {
        if (imSessionContext instanceof HttpSession) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException {
        HttpRequest request = HttpRequestDecoder.decode(buffer, imChannelContext, false);
        if (request.getHeaders().get(Http.RequestHeaderKey.Sec_WebSocket_Key) == null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateProtocol(ImPacket imPacket) throws ImException {
        if (imPacket instanceof HttpPacket) {
            return true;
        }
        return false;
    }

}
