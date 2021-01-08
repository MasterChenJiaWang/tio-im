package com.daren.chen.im.server.springboot.queue;

import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.server.processor.notice.NoticeOfflineCmdProcessor;

/**
 * @author WChao
 * @date 2018年4月3日 上午10:47:40
 */
public class UserOnlineStatusQueueRunnable extends AbstractQueueRunnable<Map<String, Object>> {

    private final Logger log = LoggerFactory.getLogger(UserOnlineStatusQueueRunnable.class);

    private final ImChannelContext imChannelContext;

    private NoticeOfflineCmdProcessor protocolCmdProcessor;

    /** The msg queue. */
    private FullWaitQueue<Map<String, Object>> msgQueue = null;

    @Override
    public FullWaitQueue<Map<String, Object>> getMsgQueue() {
        if (msgQueue == null) {
            synchronized (this) {
                if (msgQueue == null) {
                    msgQueue = new TioFullWaitQueue<>(100000, true);
                }
            }
        }
        return msgQueue;
    }

    public UserOnlineStatusQueueRunnable(ImChannelContext imChannelContext, Executor executor) {
        super(executor);
        this.imChannelContext = imChannelContext;
    }

    @Override
    public void runTask() {
        Map<String, Object> message;
        while ((message = this.getMsgQueue().poll()) != null) {
            if (protocolCmdProcessor != null) {
                protocolCmdProcessor.saveUserOnlineStatus(imChannelContext, message);
            }
        }
    }

    public NoticeOfflineCmdProcessor getProtocolCmdProcessor() {
        return protocolCmdProcessor;
    }

    public void setProtocolCmdProcessor(NoticeOfflineCmdProcessor protocolCmdProcessor) {
        this.protocolCmdProcessor = protocolCmdProcessor;
    }
}
