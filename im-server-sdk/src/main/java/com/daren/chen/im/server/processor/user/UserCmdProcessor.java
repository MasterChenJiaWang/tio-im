package com.daren.chen.im.server.processor.user;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName UserCmdProcessor
 * @Description
 * @date 2020/10/26 9:58
 **/
public interface UserCmdProcessor extends SingleProtocolCmdProcessor {
    /**
     * 获取用户状态
     *
     * @param curUserId
     * @return
     */
    GetUserOnlineStatusRespBody getUserOnlineStatus(String curUserId, ImChannelContext imChannelContext);

}
