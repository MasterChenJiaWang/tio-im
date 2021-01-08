package com.daren.chen.im.server.processor.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.core.packets.NoticeAckBody;
import com.daren.chen.im.core.utils.JsonKit;

/**
 * @author WChao
 * @date 2018年4月3日 下午1:12:30
 */
public class DefaultAsyncChatMessageProcessor extends BaseAsyncChatMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncChatMessageProcessor.class);

    @Override
    public void doProcess(ChatBody chatBody, ImChannelContext imChannelContext) {
        logger.info("消息:{}", JsonKit.toJSONString(chatBody));
        // 存储到sql
        writeMessageOfSql(imChannelContext.getUserId(), chatBody);
    }

    @Override
    protected void doChatAck(ChatAckBody chatAckBody, ImChannelContext imChannelContext) {

    }

    @Override
    protected void doNoticeAck(NoticeAckBody noticeAckBody, ImChannelContext imChannelContext) {

    }

    /**
     *
     * @param chatBody
     */
    private void writeMessageOfSql(String operateUserId, ChatBody chatBody) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        messageHelper.writeMessageOfSql(operateUserId, chatBody);
    }

    @Override
    public void noticeAck(ImChannelContext imChannelContext, Message message) {

    }
}
