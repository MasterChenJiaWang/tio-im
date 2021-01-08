package com.daren.chen.im.server.listener;

import java.util.Objects;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImGroupListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.server.config.ImServerConfig;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-03 00:17
 */
public abstract class AbstractImGroupListener implements ImGroupListener {

    public abstract void doAfterBind(ImChannelContext imChannelContext, Group group) throws ImException;

    public abstract void doAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException;

    @Override
    public void onAfterBind(ImChannelContext imChannelContext, Group group) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // 是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterGroupBind(imChannelContext, group);
        }
        doAfterBind(imChannelContext, group);
    }

    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // 是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterGroupUnbind(imChannelContext, group);
        }
        doAfterUnbind(imChannelContext, group);
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
