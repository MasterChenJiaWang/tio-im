package com.daren.chen.im.client.test;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImUserListener;
import com.daren.chen.im.core.packets.User;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/10/23 19:51
 */
public class UserListener implements ImUserListener {

    /**
     * 绑定用户后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param user
     *            绑定用户信息
     * @throws ImException
     */
    @Override
    public void onAfterBind(ImChannelContext imChannelContext, User user) throws ImException {
        System.out.println("绑定用户后回调该方法   " + imChannelContext.getUserId());
    }

    /**
     * 解绑用户后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param user
     *            解绑用户信息
     * @throws ImException
     */
    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        System.out.println("解绑用户后回调该方法   " + imChannelContext.getUserId());
    }
}
