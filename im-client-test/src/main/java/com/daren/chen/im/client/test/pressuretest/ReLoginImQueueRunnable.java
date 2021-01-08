package com.daren.chen.im.client.test.pressuretest;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.queue.FullWaitQueue;
import org.tio.utils.queue.TioFullWaitQueue;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

import com.daren.chen.im.client.test.IMPerfClientStarter;
import com.daren.chen.im.client.test.UserToken;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/11/19 19:46
 */
public class ReLoginImQueueRunnable extends AbstractQueueRunnable<UserToken> {
    private static final Logger logger = LoggerFactory.getLogger(ReLoginImQueueRunnable.class);
    /** The msg queue. */
    private FullWaitQueue<UserToken> msgQueue = null;

    public ReLoginImQueueRunnable(Executor executor) {
        super(executor);
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
            IMPerfClientStarter.TestParams testParams = IMPerfClientStarter.testParams;
            logger.info("正在重新登录...");
            IMPerfClientStarter.loginIm(0, userToken, testParams.contextMap.get(userToken.getUserId()));
        }
    }
}
