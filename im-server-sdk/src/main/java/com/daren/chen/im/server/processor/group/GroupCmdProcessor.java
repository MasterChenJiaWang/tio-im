package com.daren.chen.im.server.processor.group;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.ExitGroupReqBody;
import com.daren.chen.im.core.packets.ExitGroupRespBody;
import com.daren.chen.im.core.packets.JoinGroupReqBody;
import com.daren.chen.im.core.packets.JoinGroupRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author ensheng
 */
public interface GroupCmdProcessor extends SingleProtocolCmdProcessor {
    /**
     * 加入群组处理
     *
     * @param joinGroupReqBody
     * @param imChannelContext
     * @return
     */
    JoinGroupRespBody join(JoinGroupReqBody joinGroupReqBody, ImChannelContext imChannelContext);

    /**
     * 退出群组
     *
     * @param exitGroup
     * @param imChannelContext
     * @return
     */
    ExitGroupRespBody exit(ExitGroupReqBody exitGroup, ImChannelContext imChannelContext);

    // /**
    // * 新增群组
    // *
    // * @param addGroup
    // * @param imChannelContext
    // * @return
    // */
    // AddGroupRespBody addGroup(AddGroupReqBody addGroup, ImChannelContext imChannelContext);

}
