package com.daren.chen.im.server.processor.chat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Message;
import com.daren.chen.im.core.packets.NoticeAckBody;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.ChatKit;

/**
 * @author WChao
 * @date 2018年4月3日 下午1:13:32
 */
public abstract class BaseAsyncChatMessageProcessor implements SingleProtocolCmdProcessor {

    protected ImServerConfig imServerConfig = ImConfig.Global.get();

    /**
     * 供子类拿到消息进行业务处理(如:消息持久化到数据库等)的抽象方法
     *
     * @param chatBody
     * @param imChannelContext
     */
    protected abstract void doProcess(ChatBody chatBody, ImChannelContext imChannelContext);

    protected abstract void doChatAck(ChatAckBody chatAckBody, ImChannelContext imChannelContext);

    /**
     *
     * @param noticeAckBody
     * @param imChannelContext
     */
    protected abstract void doNoticeAck(NoticeAckBody noticeAckBody, ImChannelContext imChannelContext);

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {
        ChatBody chatBody = (ChatBody)message;
        // 设置请求线程
        String userId = chatBody.getFrom();
        if (StringUtils.isNotBlank(userId)) {
            AuthCacheService.setEnvironment(userId);
        }
        // 开启持久化
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        if (isStore) {
            // 存储群聊消息;
            if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()) {
                pushGroupMessages(imChannelContext.getUserId(), PUSH, STORE, chatBody);
            } else {
                String from = chatBody.getFrom();
                String to = chatBody.getTo();
                String sessionId = ChatKit.sessionId(from, to);
                writeMessage(STORE, USER + ":" + sessionId, chatBody);
                //
                writeNoReadMessage(PUSH, USER + ":" + to + ":" + from, chatBody);
            }
        }
        doProcess(chatBody, imChannelContext);
    }

    @Override
    public void chatAck(ImChannelContext imChannelContext, Message message) {
        ChatAckBody chatAckBody = (ChatAckBody)message;
        // 开启持久化
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        if (isStore) {
            doChatAck(chatAckBody, imChannelContext);
        }

    }

    @Override
    public void noticeAck(ImChannelContext imChannelContext, Message message) {
        NoticeAckBody noticeAckBody = (NoticeAckBody)message;
        // 开启持久化
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        if (isStore) {
            doNoticeAck(noticeAckBody, imChannelContext);
        }

    }

    /**
     * 推送持久化群组消息
     *
     * @param pushTable
     * @param storeTable
     * @param chatBody
     */
    private void pushGroupMessages(String operateUserId, String pushTable, String storeTable, ChatBody chatBody) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        String groupId = chatBody.getGroupId();
        // 先将群消息持久化到存储Timeline;
        writeMessage(storeTable, GROUP + ":" + groupId, chatBody);
        // //
        List<String> userIds = messageHelper.getGroupUsers(operateUserId, groupId);
        // 通过写扩散模式将群消息同步到所有的群成员
        for (String userId : userIds) {
            writeNoReadMessage(pushTable, GROUP + ":" + groupId + ":" + userId, chatBody);
        }
    }

    /**
     *
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    private void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        messageHelper.writeMessage(timelineTable, timelineId, chatBody);
    }

    /**
     *
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    private void writeNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        messageHelper.saveNoReadMessage(timelineTable, timelineId, chatBody);
    }
}
