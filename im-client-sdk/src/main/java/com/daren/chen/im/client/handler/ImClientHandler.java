package com.daren.chen.im.client.handler;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImHandler;
import com.daren.chen.im.core.ImPacket;

/**
 *
 * 客户端回调
 *
 * @author WChao
 *
 */
public interface ImClientHandler extends ImHandler {
    /**
     * 心跳包接口
     *
     * @param imChannelContext
     * @return
     */
    ImPacket heartbeatPacket(ImChannelContext imChannelContext);
}
