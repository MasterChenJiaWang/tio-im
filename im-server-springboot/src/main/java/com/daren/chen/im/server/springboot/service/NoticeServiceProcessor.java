package com.daren.chen.im.server.springboot.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.MsgNoticeReq;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.notice.MsgNoticeCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/26 11:00
 */
public class NoticeServiceProcessor extends AbstractProtocolCmdProcessor implements MsgNoticeCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NoticeServiceProcessor.class);

    // @Override
    // public MsgNoticeRespBody msgNotice(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext) {
    // ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
    // MessageHelper messageHelper = imServerConfig.getMessageHelper();
    // MsgNoticeRespBody msgNoticeRespBody = messageHelper.msgNotice(msgNoticeReq);
    // return MsgNoticeRespBody.success(msgNoticeRespBody);
    // }

    @Override
    public MsgNoticeRespBody msgNoticeOffLine(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        MsgNoticeRespBody msgNoticeRespBody =
            messageHelper.msgNoticeOffline(imChannelContext.getUserId(), msgNoticeReq);
        return MsgNoticeRespBody.success(msgNoticeRespBody);
    }

    @Override
    public Map<String, Object> dissolveGroup(MsgNoticeReq msgNoticeReq, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        return messageHelper.dissolveGroup(imChannelContext.getUserId(), msgNoticeReq);
    }
}
