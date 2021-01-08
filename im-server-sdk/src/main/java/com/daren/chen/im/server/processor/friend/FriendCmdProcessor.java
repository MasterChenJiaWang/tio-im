package com.daren.chen.im.server.processor.friend;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.AddFriendRespBody;
import com.daren.chen.im.core.packets.DeleteFriendRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName FriendCmdProcessor
 * @Description
 * @date 2020/10/21 10:16
 **/
public interface FriendCmdProcessor extends SingleProtocolCmdProcessor {

    /**
     * 新增好友
     *
     * @param curUserId
     * @param friendUserId
     * @param imChannelContext
     * @return
     */
    AddFriendRespBody addFriend(String curUserId, String friendUserId, ImChannelContext imChannelContext);

    /**
     * 删除好友
     *
     * @param curUserId
     * @param friendUserId
     * @param imChannelContext
     * @return
     */
    DeleteFriendRespBody deleteFriend(String curUserId, String friendUserId, ImChannelContext imChannelContext);
}
