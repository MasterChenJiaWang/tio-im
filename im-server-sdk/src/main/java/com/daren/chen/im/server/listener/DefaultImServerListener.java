package com.daren.chen.im.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.server.util.ChatKit;

/**
 * @ClassName DefaultImServerListener
 * @Description 默认IM服务端连接监听器
 * @Author WChao
 * @Date 2020/1/4 11:15
 * @Version 1.0
 **/
public class DefaultImServerListener implements ImServerListener {

    private final Logger log = LoggerFactory.getLogger(DefaultImServerListener.class);

    /**
     *
     * 服务器检查到心跳超时时，会调用这个函数（一般场景，该方法只需要直接返回false即可）
     *
     * @param channelContext
     * @param interval
     *            已经多久没有收发消息了，单位：毫秒
     * @param heartbeatTimeoutCount
     *            心跳超时次数，第一次超时此值是1，以此类推。此值被保存在：channelContext.stat.heartbeatTimeoutCount
     * @return 返回true，那么服务器则不关闭此连接；返回false，服务器将按心跳超时关闭该连接
     */
    @Override
    public boolean onHeartbeatTimeout(ImChannelContext channelContext, Long interval, int heartbeatTimeoutCount) {
        return false;
    }

    /**
     * 原方法名：onAfterDecoded 解码成功后触发本方法
     *
     * @param channelContext
     * @param isConnected
     *            是否连接成功,true:表示连接成功，false:表示连接失败
     * @param isReconnect
     *            是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
     * @throws Exception
     */
    @Override
    public void onAfterConnected(ImChannelContext channelContext, boolean isConnected, boolean isReconnect)
        throws Exception {
        String userId = "";
        if (channelContext != null) {
            userId = channelContext.getUserId();
        }
        log.info("上下文ID [{}] 用户ID [{}]   连接成功后触发本方法 >>>>>>>>  连接状态  isConnected : {}   是否是重连 isReconnect {} ",
            channelContext.getId(), userId, isConnected, isReconnect);
        //

    }

    @Override
    public void onAfterDecoded(ImChannelContext channelContext, ImPacket packet, int packetSize) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ImChannelContext channelContext, int receivedBytes) throws Exception {

    }

    @Override
    public void onAfterSent(ImChannelContext channelContext, ImPacket packet, boolean isSentSuccess) throws Exception {

    }

    /**
     * 处理一个消息包后
     *
     * @param channelContext
     * @param packet
     * @param cost
     *            本次处理消息耗时，单位：毫秒
     * @throws Exception
     */
    @Override
    public void onAfterHandled(ImChannelContext channelContext, ImPacket packet, long cost) throws Exception {

    }

    /**
     * 连接关闭前触发本方法
     *
     * @param channelContext
     * @param throwable
     *            the throwable 有可能为空
     * @param remark
     *            the remark 有可能为空
     * @param isRemove
     * @throws Exception
     */
    @Override
    public void onBeforeClose(ImChannelContext channelContext, Throwable throwable, String remark, boolean isRemove)
        throws Exception {
        String userId = "";
        if (channelContext != null) {
            userId = channelContext.getUserId();
            ChatKit.isOnline(userId, false);
        }
        log.error("连接关闭前触发本方法 userid {}  remark : {}   异常 {}", userId, remark,
            throwable == null ? "" : throwable.getMessage());

    }

}
