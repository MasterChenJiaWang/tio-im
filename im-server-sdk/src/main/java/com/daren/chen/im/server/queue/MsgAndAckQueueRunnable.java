package com.daren.chen.im.server.queue;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.BaseChatBody;
import com.daren.chen.im.server.processor.ProtocolCmdProcessor;

/**
 * @author WChao
 * @date 2018年4月3日 上午10:47:40
 */
public class MsgAndAckQueueRunnable extends AbstractQueueRunnable<BaseChatBody> {

    private final Logger log = LoggerFactory.getLogger(MsgAndAckQueueRunnable.class);

    private final ImChannelContext imChannelContext;

    private ProtocolCmdProcessor protocolCmdProcessor;

    /** The msg queue. */
    private FullWaitQueue<BaseChatBody> msgQueue = null;

    @Override
    public FullWaitQueue<BaseChatBody> getMsgQueue() {
        if (msgQueue == null) {
            synchronized (MsgAndAckQueueRunnable.class) {
                if (msgQueue == null) {
                    msgQueue = new TioFullWaitQueue<>(100000, true);
                }
            }
        }
        return msgQueue;
    }

    public MsgAndAckQueueRunnable(ImChannelContext imChannelContext, Executor executor) {
        super(executor);
        this.imChannelContext = imChannelContext;
    }

    @Override
    public void runTask() {
        BaseChatBody message;
        FullWaitQueue<BaseChatBody> msgQueue = this.getMsgQueue();
        while ((message = msgQueue.poll()) != null) {
            Short dataType = message.getDataType();
            if (protocolCmdProcessor != null && dataType != null) {
                if (dataType == 1) {
                    protocolCmdProcessor.process(imChannelContext, message);
                } else if (dataType == 2) {
                    protocolCmdProcessor.chatAck(imChannelContext, message);
                }

            }
        }
    }

    public ProtocolCmdProcessor getProtocolCmdProcessor() {
        return protocolCmdProcessor;
    }

    public void setProtocolCmdProcessor(ProtocolCmdProcessor protocolCmdProcessor) {
        this.protocolCmdProcessor = protocolCmdProcessor;
    }
}
