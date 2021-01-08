package com.daren.chen.im.server.handler;

import java.nio.ByteBuffer;

import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.Environment;

/**
 * @ClassName ImServerHandlerAdapter
 * @Description
 * @Author WChao
 * @Date 2020/1/6 2:30
 * @Version 1.0
 **/
public class ImServerHandlerAdapter implements ServerAioHandler, ImConst {

    private ImServerHandler imServerHandler;

    public ImServerHandlerAdapter(ImServerHandler imServerHandler) {
        this.imServerHandler = imServerHandler;
    }

    /**
     * @param buffer
     * @param limit
     * @param position
     * @param readableLength
     * @param channelContext
     * @return
     * @throws AioDecodeException
     */
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext)
        throws AioDecodeException {
        ImPacket imPacket;
        try {
            imPacket = this.imServerHandler.decode(buffer, limit, position, readableLength,
                (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
        } catch (ImDecodeException e) {
            throw new AioDecodeException(e);
        }
        return imPacket;
    }

    /**
     * @param packet
     * @param tioConfig
     * @param channelContext
     * @return
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        return this.imServerHandler.encode((ImPacket)packet, ImConfig.Global.get(),
            (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY));
    }

    /**
     * 消息处理
     *
     * @param packet
     * @param channelContext
     * @throws Exception
     */
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {

        try {
            // 处理消息
            ImPacket packet1 = (ImPacket)packet;
            ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
            // 初始化本地线程
            setEnvironment(imChannelContext);
            //
            this.imServerHandler.handler(packet1, imChannelContext);
        } finally {
            //
            removeEnvironment();
        }
    }

    /**
     * @param imChannelContext
     */
    private void setEnvironment(ImChannelContext imChannelContext) {
        String userId = imChannelContext.getUserId();
        LoginUser loginUser = AuthCacheService.getAuth(userId);
        Environment.setCurrentUser(loginUser);
    }

    /**
     *
     */
    private void removeEnvironment() {
        Environment.remove();
    }
}
