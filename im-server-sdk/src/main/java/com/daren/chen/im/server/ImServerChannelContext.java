package com.daren.chen.im.server;

import org.tio.core.ChannelContext;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.server.protocol.AbstractProtocolHandler;

/**
 * @ClassName ImServerChannelContext
 * @Description 服务端通道上下文
 * @Author WChao
 * @Date 2020/1/5 23:56
 * @Version 1.0
 **/
public class ImServerChannelContext extends ImChannelContext {

    // /**
    // *
    // */
    // protected AbstractQueueRunnable msgQue;
    // /**
    // *
    // */
    // protected AbstractQueueRunnable msgAckQue;

    /**
     *
     */
    protected AbstractQueueRunnable noticeAckQue;

    /**
     *
     */
    protected AbstractQueueRunnable userOnlineStatusQueueRunnable;

    /**
     *
     */
    protected AbstractQueueRunnable msgAndAckQue;

    protected AbstractProtocolHandler protocolHandler;

    public ImServerChannelContext(ImConfig imConfig, ChannelContext tioChannelContext) {
        super(imConfig, tioChannelContext);
    }

    // public AbstractQueueRunnable getMsgQue() {
    // return msgQue;
    // }
    //
    // public void setMsgQue(AbstractQueueRunnable msgQue) {
    // this.msgQue = msgQue;
    // }
    //
    // public AbstractQueueRunnable getMsgAckQue() {
    // return msgAckQue;
    // }
    //
    // public void setMsgAckQue(AbstractQueueRunnable msgAckQue) {
    // this.msgAckQue = msgAckQue;
    // }

    public AbstractQueueRunnable getMsgAndAckQue() {
        return msgAndAckQue;
    }

    public void setMsgAndAckQue(AbstractQueueRunnable msgAndAckQue) {
        this.msgAndAckQue = msgAndAckQue;
    }

    public AbstractQueueRunnable getNoticeAckQue() {
        return noticeAckQue;
    }

    public void setNoticeAckQue(AbstractQueueRunnable noticeAckQue) {
        this.noticeAckQue = noticeAckQue;
    }

    public AbstractProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(AbstractProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    public AbstractQueueRunnable getUserOnlineStatusQueueRunnable() {
        return userOnlineStatusQueueRunnable;
    }

    public void setUserOnlineStatusQueueRunnable(AbstractQueueRunnable userOnlineStatusQueueRunnable) {
        this.userOnlineStatusQueueRunnable = userOnlineStatusQueueRunnable;
    }

    public void allEmpty() {
        this.noticeAckQue = null;
        this.userOnlineStatusQueueRunnable = null;
        this.msgAndAckQue = null;
    }
}
