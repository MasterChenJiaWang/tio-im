package com.daren.chen.im.server.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeReq;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.notice.ReceiveMsgNoticeCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/26 11:00
 */
public class ReceiveNoticeServiceProcessor extends AbstractProtocolCmdProcessor
    implements ReceiveMsgNoticeCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveNoticeServiceProcessor.class);

    @Override
    public ReceiveMsgNoticeRespBody receiveMsgNotice(ReceiveMsgNoticeReq receiveMsgNoticeReq,
        ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody =
            messageHelper.receiveMsgNotice(imChannelContext.getUserId(), receiveMsgNoticeReq.getUserId());
        return ReceiveMsgNoticeRespBody.success(receiveMsgNoticeRespBody);
    }

}
