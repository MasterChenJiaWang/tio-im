/**
 *
 */
package com.daren.chen.im.core.tcp;

import java.nio.ByteBuffer;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.protocol.AbstractProtocol;
import com.daren.chen.im.core.protocol.IProtocolConverter;
import com.daren.chen.im.core.utils.ImKit;

/**
 * @desc Tcp协议校验器
 * @author WChao
 * @date 2018-05-01
 */
public class TcpProtocol extends AbstractProtocol {

    public TcpProtocol(IProtocolConverter converter) {
        super(converter);
    }

    @Override
    public String name() {
        return Protocol.TCP;
    }

    @Override
    protected void init(ImChannelContext imChannelContext) {
        imChannelContext.setSessionContext(new TcpSessionContext(imChannelContext));
        ImKit.initImClientNode(imChannelContext);
    }

    @Override
    public boolean validateProtocol(ImSessionContext imSessionContext) throws ImException {
        if (imSessionContext instanceof TcpSessionContext) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException {
        // 获取第一个字节协议版本号,TCP协议;
        if (buffer.get() == Protocol.VERSION) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateProtocol(ImPacket imPacket) throws ImException {
        if (imPacket instanceof TcpPacket) {
            return true;
        }
        return false;
    }

}
