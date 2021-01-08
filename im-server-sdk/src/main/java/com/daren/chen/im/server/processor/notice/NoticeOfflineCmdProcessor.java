package com.daren.chen.im.server.processor.notice;

import java.util.Map;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.NoticeOfflineReq;
import com.daren.chen.im.core.packets.NoticeOfflineRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName MsgNoticeCmdProcessor
 * @Description
 * @date 2020/10/26 20:14
 **/
public interface NoticeOfflineCmdProcessor extends SingleProtocolCmdProcessor {

    /**
     * 下线通知
     *
     * @param noticeOfflineReq
     * @param imChannelContext
     * @return
     */
    NoticeOfflineRespBody offlineNotice(NoticeOfflineReq noticeOfflineReq, ImChannelContext imChannelContext);

    /**
     * 保存用户在线状态
     *
     * @param imChannelContext
     * @param message
     */
    void saveUserOnlineStatus(ImChannelContext imChannelContext, Map<String, Object> message);
}
