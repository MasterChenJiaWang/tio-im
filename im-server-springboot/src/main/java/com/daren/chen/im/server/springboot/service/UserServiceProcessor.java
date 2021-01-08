package com.daren.chen.im.server.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.user.UserCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/26 11:00
 */
public class UserServiceProcessor extends AbstractProtocolCmdProcessor implements UserCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceProcessor.class);

    @Override
    public GetUserOnlineStatusRespBody getUserOnlineStatus(String curUserId, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        String userOnlineStatus = messageHelper.getUserOnlineStatus(imChannelContext.getUserId(), curUserId);
        return GetUserOnlineStatusRespBody.success(curUserId, userOnlineStatus);
    }
}
