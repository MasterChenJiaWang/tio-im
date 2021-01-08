package com.daren.chen.im.server.processor.chat;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author WChao
 * @date 2018年4月3日 下午1:12:30
 */
public interface ChatAckMessageProcessor extends SingleProtocolCmdProcessor {

    /**
     *
     * @param chatAckBody
     * @param imChannelContext
     */
    void ack(ChatAckBody chatAckBody, ImChannelContext imChannelContext);
}
