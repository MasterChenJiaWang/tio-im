package com.daren.chen.im.server.protocol;

import java.util.Objects;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.ProtocolCmdProcessor;

/**
 * @author WChao
 * @Desc
 * @date 2020-05-02 16:23
 */
public abstract class AbstractProtocolCmdProcessor implements ProtocolCmdProcessor {

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {

    }

    /**
     *
     * @param imChannelContext
     * @param message
     */
    @Override
    public void chatAck(ImChannelContext imChannelContext, Message message) {

    }

    /**
     *
     * @param imChannelContext
     * @param message
     */
    @Override
    public void noticeAck(ImChannelContext imChannelContext, Message message) {

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
