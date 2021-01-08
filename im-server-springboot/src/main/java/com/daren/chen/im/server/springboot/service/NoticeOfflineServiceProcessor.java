package com.daren.chen.im.server.springboot.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.NoticeOfflineReq;
import com.daren.chen.im.core.packets.NoticeOfflineRespBody;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.notice.NoticeOfflineCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/26 11:00
 */
public class NoticeOfflineServiceProcessor extends AbstractProtocolCmdProcessor implements NoticeOfflineCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NoticeOfflineServiceProcessor.class);

    @Override
    public NoticeOfflineRespBody offlineNotice(NoticeOfflineReq noticeOfflineReq, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        NoticeOfflineRespBody msgNoticeRespBody =
            messageHelper.noticeOffLine(imChannelContext.getUserId(), noticeOfflineReq);
        return NoticeOfflineRespBody.success(msgNoticeRespBody);
    }

    /**
     *
     * @param imChannelContext
     * @param message
     */
    @Override
    public void saveUserOnlineStatus(ImChannelContext imChannelContext, Map<String, Object> message) {
        //
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        try {
            messageHelper.addUserOnlineStatusRecord(imChannelContext.getUserId(), message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
