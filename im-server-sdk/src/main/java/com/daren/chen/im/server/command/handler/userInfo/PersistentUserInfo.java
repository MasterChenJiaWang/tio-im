package com.daren.chen.im.server.command.handler.userInfo;

import java.util.Objects;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserReqBody;
import com.daren.chen.im.server.config.ImServerConfig;

/**
 * 持久化获取用户信息处理
 */
public class PersistentUserInfo implements IUserInfo {

    @Override
    public User getUserInfo(UserReqBody userReqBody, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = ImConfig.Global.get();
        String userId = userReqBody.getUserId();
        Integer type = userReqBody.getType();
        // 消息持久化助手;
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        User user = messageHelper.getUserByType(imChannelContext.getUserId(), userId, type);
        if (Objects.nonNull(user)) {
            // user.setFriends(messageHelper.getAllFriendUsers(userId, type));
            user.setFriends(messageHelper.getAllFriendUsers(imChannelContext.getUserId(), userId, type));
            user.setGroups(messageHelper.getAllGroupUsers(imChannelContext.getUserId(), userId, type));
        }
        return user;
    }

}
