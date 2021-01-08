package com.daren.chen.im.core.listener;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Group;

/**
 * @ClassName ImGroupListener
 * @Description
 * @Author WChao
 * @Date 2020/1/12 14:17
 * @Version 1.0
 **/
public interface ImGroupListener {
    /**
     * 绑定群组后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param group
     *            绑定群组对象
     * @throws ImException
     * @author WChao
     */
    void onAfterBind(ImChannelContext imChannelContext, Group group) throws ImException;

    /**
     * 解绑群组后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param group
     *            绑定群组对象
     * @throws ImException
     * @author WChao
     */
    void onAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException;

}
