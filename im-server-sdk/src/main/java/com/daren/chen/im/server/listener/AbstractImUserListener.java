package com.daren.chen.im.server.listener;

import java.util.Objects;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImUserListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.config.ImServerConfig;

/**
 * @author WChao
 * @Desc 绑定/解绑 用户监听器抽象类
 * @date 2020-05-02 13:43
 */
public abstract class AbstractImUserListener implements ImUserListener {

    public abstract void doAfterBind(ImChannelContext imChannelContext, User user) throws ImException;

    public abstract void doAfterUnbind(ImChannelContext imChannelContext, User user) throws ImException;

    /**
     * 绑定用户后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param user
     *            绑定用户信息
     * @throws Exception
     * @author WChao
     */
    @Override
    public void onAfterBind(ImChannelContext imChannelContext, User user) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // 是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterUserBind(imChannelContext, user);
        }
        doAfterBind(imChannelContext, user);
    }

    /**
     * 解绑用户后回调该方法
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param user
     *            解绑用户信息
     * @throws Exception
     * @author WChao
     */
    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // 是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterUserUnbind(imChannelContext, user);
        }
        doAfterUnbind(imChannelContext, user);
    }

    /**
     * 是否开启持久化;
     *
     * @return
     */
    public boolean isStore(ImServerConfig imServerConfig) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        if (ImServerConfig.ON.equals(imServerConfig.getIsStore()) && Objects.nonNull(messageHelper)) {
            return true;
        }
        return false;
    }

}
