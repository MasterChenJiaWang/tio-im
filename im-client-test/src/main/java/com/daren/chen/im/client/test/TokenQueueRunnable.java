package com.daren.chen.im.client.test;

import java.util.concurrent.Executor;

import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/11/19 19:46
 */
public class TokenQueueRunnable extends AbstractQueueRunnable<UserToken> {

    /** The msg queue. */
    private FullWaitQueue<UserToken> msgQueue = null;

    private TokenToFileProcessor tokenToFileProcessor;

    public TokenQueueRunnable(Executor executor, TokenToFileProcessor tokenToFileProcessor) {
        super(executor);
        this.tokenToFileProcessor = tokenToFileProcessor;
    }

    @Override
    public FullWaitQueue<UserToken> getMsgQueue() {
        if (msgQueue == null) {
            synchronized (this) {
                if (msgQueue == null) {
                    msgQueue = new TioFullWaitQueue<>(Integer.MAX_VALUE, true);
                }
            }
        }
        return msgQueue;
    }

    @Override
    public void runTask() {
        UserToken userToken;
        while ((userToken = this.getMsgQueue().poll()) != null) {
            if (tokenToFileProcessor != null) {
                tokenToFileProcessor.process(userToken);
            }
        }
    }
}
