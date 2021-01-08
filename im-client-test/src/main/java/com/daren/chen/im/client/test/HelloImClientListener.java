package com.daren.chen.im.client.test;

import com.daren.chen.im.client.listener.ImClientListener;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-04 07:33
 */
public class HelloImClientListener implements ImClientListener {

    /**
     * 建链后触发本方法，注：建链不一定成功，需要关注参数isConnected
     *
     * @param imChannelContext
     * @param isConnected
     *            是否连接成功,true:表示连接成功，false:表示连接失败
     * @param isReconnect
     *            是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
     * @throws Exception
     */
    @Override
    public void onAfterConnected(ImChannelContext imChannelContext, boolean isConnected, boolean isReconnect)
        throws Exception {
        System.out.printf("建链后触发本方法  isConnected %s: isReconnect  %s ", isConnected, isReconnect);
        System.out.println();

        // 是 断开重连 就需要重新发送token
        if (isConnected && isReconnect) {
            //
        }
    }

    /**
     * 原方法名：onAfterDecoded 解码成功后触发本方法
     *
     * @param imChannelContext
     * @param packet
     * @param packetSize
     * @throws Exception
     */
    @Override
    public void onAfterDecoded(ImChannelContext imChannelContext, ImPacket packet, int packetSize) throws Exception {
        // System.out.println("解码成功后触发本方法 : " + packetSize);
    }

    /**
     * 接收到TCP层传过来的数据后
     *
     * @param imChannelContext
     * @param receivedBytes
     *            本次接收了多少字节
     * @throws Exception
     */
    @Override
    public void onAfterReceivedBytes(ImChannelContext imChannelContext, int receivedBytes) throws Exception {
        // System.out.println("接收到TCP层传过来的数据后 本次接收了多少字节: " + receivedBytes);
    }

    /**
     * 消息包发送之后触发本方法
     *
     * @param imChannelContext
     * @param packet
     * @param isSentSuccess
     *            true:发送成功，false:发送失败
     * @throws Exception
     */
    @Override
    public void onAfterSent(ImChannelContext imChannelContext, ImPacket packet, boolean isSentSuccess)
        throws Exception {
        // System.out.println("消息包发送之后触发本方法 发送状态: " + isSentSuccess);
    }

    /**
     * 处理一个消息包后
     *
     * @param imChannelContext
     * @param packet
     * @param cost
     *            本次处理消息耗时，单位：毫秒
     * @throws Exception
     */
    @Override
    public void onAfterHandled(ImChannelContext imChannelContext, ImPacket packet, long cost) throws Exception {
        // System.out.println("处理一个消息包后 本次处理消息耗时，单位：毫秒: " + cost);
    }

    /**
     * 连接关闭前触发本方法
     *
     * @param imChannelContext
     * @param throwable
     *            the throwable 有可能为空
     * @param remark
     *            the remark 有可能为空
     * @param isRemove
     * @throws Exception
     */
    @Override
    public void onBeforeClose(ImChannelContext imChannelContext, Throwable throwable, String remark, boolean isRemove)
        throws Exception {

        System.out.println("连接关闭前触发本方法  remark: " + remark);
    }

}
