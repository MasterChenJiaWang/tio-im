package com.daren.chen.im.server.queue;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.server.processor.ProtocolCmdProcessor;

/**
 * @author WChao
 * @date 2018年4月3日 上午10:47:40
 */
public class MsgQueueRunnable extends AbstractQueueRunnable<Message> {

    private final Logger log = LoggerFactory.getLogger(MsgQueueRunnable.class);

    private final ImChannelContext imChannelContext;

    private ProtocolCmdProcessor protocolCmdProcessor;

    /** The msg queue. */
    private FullWaitQueue<Message> msgQueue = null;

    @Override
    public FullWaitQueue<Message> getMsgQueue() {
        if (msgQueue == null) {
            synchronized (this) {
                if (msgQueue == null) {
                    msgQueue = new TioFullWaitQueue<>(100000, true);
                }
            }
        }
        return msgQueue;
    }

    public MsgQueueRunnable(ImChannelContext imChannelContext, Executor executor) {
        super(executor);
        this.imChannelContext = imChannelContext;
    }

    @Override
    public void runTask() {
        Message message;
        FullWaitQueue<Message> msgQueue = this.getMsgQueue();
        while ((message = msgQueue.poll()) != null) {
            if (protocolCmdProcessor != null) {
                protocolCmdProcessor.process(imChannelContext, message);
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
