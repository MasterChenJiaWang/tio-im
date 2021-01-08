package com.daren.chen.im.server.processor.notice;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeReq;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName MsgNoticeCmdProcessor
 * @Description
 * @date 2020/10/26 20:14
 **/
public interface ReceiveMsgNoticeCmdProcessor extends SingleProtocolCmdProcessor {

    /**
     * 接收消息通知
     *
     * @param receiveMsgNoticeReq
     * @param imChannelContext
     * @return
     */
    ReceiveMsgNoticeRespBody receiveMsgNotice(ReceiveMsgNoticeReq receiveMsgNoticeReq,
        ImChannelContext imChannelContext);

}
