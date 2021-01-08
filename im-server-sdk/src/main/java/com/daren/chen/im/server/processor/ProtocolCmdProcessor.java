package com.daren.chen.im.server.processor;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.packets.Message;

/**
 * @author WChao
 * @Desc 不同协议CMD命令处理器接口
 * @date 2020-05-02 14:31
 */
public interface ProtocolCmdProcessor extends ImConst {
    /**
     * cmd命令处理器方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param message
     *            消息
     */
    void process(ImChannelContext imChannelContext, Message message);

    /**
     *
     * @param imChannelContext
     * @param message
     */
    void chatAck(ImChannelContext imChannelContext, Message message);

    /**
     *
     * @param imChannelContext
     * @param message
     */
    void noticeAck(ImChannelContext imChannelContext, Message message);

}
