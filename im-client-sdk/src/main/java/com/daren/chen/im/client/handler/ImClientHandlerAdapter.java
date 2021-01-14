package com.daren.chen.im.client.handler;

import java.nio.ByteBuffer;

import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;

import com.daren.chen.im.client.ImClientChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;

/**
 * @ClassName ImClientHandlerAdapter
 * @Description IM客户端回调适配器
 * @Author WChao
 * @Date 2020/1/6 2:30
 * @Version 1.0
 **/
public class ImClientHandlerAdapter implements ClientAioHandler, ImConst {

    private final ImClientHandler imClientHandler;

    public ImClientHandlerAdapter(ImClientHandler imClientHandler) {
        this.imClientHandler = imClientHandler;
    }

    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext)
        throws TioDecodeException {
        ImPacket imPacket;
        try {
            imPacket = this.imClientHandler.decode(buffer, limit, position, readableLength,
                (ImClientChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
        } catch (ImDecodeException e) {
            throw new TioDecodeException(e);
        }
        return imPacket;
    }

    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        return this.imClientHandler.encode((ImPacket)packet, ImConfig.Global.get(),
            (ImClientChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        this.imClientHandler.handler((ImPacket)packet,
            (ImClientChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
    }

    @Override
    public Packet heartbeatPacket(ChannelContext channelContext) {
        return this.imClientHandler
            .heartbeatPacket((ImClientChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
    }

}
