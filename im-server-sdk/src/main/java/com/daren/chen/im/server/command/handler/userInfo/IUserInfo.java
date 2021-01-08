package com.daren.chen.im.server.command.handler.userInfo;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserReqBody;

public interface IUserInfo {
    /**
     * 获取用户信息接口
     *
     * @param userReqBody
     * @param imChannelContext
     * @return
     */
    User getUserInfo(UserReqBody userReqBody, ImChannelContext imChannelContext);
}
