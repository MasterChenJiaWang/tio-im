package com.daren.chen.im.server.processor.notice;

import java.util.Map;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.MsgNoticeReq;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName MsgNoticeCmdProcessor
 * @Description
 * @date 2020/10/26 20:14
 **/
public interface MsgNoticeCmdProcessor extends SingleProtocolCmdProcessor {

    // /**
    // * 消息通知
    // *
    // * @param msgNoticeReq
    // * @param imChannelContext
    // * @return
    // */
    // MsgNoticeRespBody msgNotice(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext);

    /**
     * 消息通知
     *
     * @param msgNoticeReq
     * @param imChannelContext
     * @return
     */
    MsgNoticeRespBody msgNoticeOffLine(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext);

    /**
     * 解散群组
     *
     * @param msgNoticeReq
     * @param imChannelContext
     * @return
     */
    Map<String, Object> dissolveGroup(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext);
}
