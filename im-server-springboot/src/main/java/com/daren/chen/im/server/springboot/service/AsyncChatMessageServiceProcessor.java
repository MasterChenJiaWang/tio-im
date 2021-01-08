package com.daren.chen.im.server.springboot.service;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.NoticeAckBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.chat.BaseAsyncChatMessageProcessor;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/29 15:34
 */
public class AsyncChatMessageServiceProcessor extends BaseAsyncChatMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncChatMessageServiceProcessor.class);
    /**
     *
     */
    private static final TimedCache<String, CountDownLatch> MSG_ID_CACHE = CacheUtil.newTimedCache(1000 * 10);

    @Override
    protected void doProcess(ChatBody chatBody, ImChannelContext imChannelContext) {
        logger.info("上下文ID [{}] 用户ID [{}]  写数据  消息:{}", imChannelContext.getId(), imChannelContext.getUserId(),
            JsonKit.toJSONString(chatBody));
        // 存储到sql
        try {
            writeMessageOfSql(imChannelContext.getUserId(), chatBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CountDownLatch synMsgIdCache(String msgId) {
        CountDownLatch countDownLatch = null;
        if ((countDownLatch = MSG_ID_CACHE.get(msgId)) == null) {
            synchronized (MSG_ID_CACHE) {
                if ((countDownLatch = MSG_ID_CACHE.get(msgId)) == null) {
                    countDownLatch = new CountDownLatch(1);
                    MSG_ID_CACHE.put(msgId, countDownLatch);
                }
            }
        }
        return countDownLatch;
    }

    /**
     *
     * @param chatBody
     */
    private void writeMessageOfSql(String operateUserId, ChatBody chatBody) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        messageHelper.writeMessageOfSql(operateUserId, chatBody);
    }

    /**
     *
     * @param chatAckBody
     * @param imChannelContext
     */
    @Override
    protected void doChatAck(ChatAckBody chatAckBody, ImChannelContext imChannelContext) {
        if (chatAckBody == null) {
            return;
        }
        String id = imChannelContext.getId();
        String userId = imChannelContext.getUserId();
        if (StringUtils.isBlank(userId)) {
            LoginUser loginUser = (LoginUser)RedisCacheManager.getCache(ImConst.USER_VERSION_INFO).get(id);
            if (loginUser != null) {
                userId = loginUser.getUserId();
            }
            logger.warn("上下文ID [{}]    修改最后ID失败! 用户ID 为空", id);
        }
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        logger.info("doChatAck 线程 {}  updateLastMessageId", Thread.currentThread().getName());
        // 先判断未读消息是否全部已读 如果没有 需要发送未读消息 如果全部 需要删除
        boolean b = messageHelper.updateLastMessageId(imChannelContext.getUserId(), userId, chatAckBody);
        if (b) {
            logger.warn("上下文ID [{}] 用户ID [{}]   修改最后ID成功! id= [{}]", id, userId, chatAckBody.getId());
        } else {
            logger.warn("上下文ID [{}] 用户ID [{}]   修改最后ID失败! id= [{}]", id, userId, chatAckBody.getId());
        }
    }

    /**
     * 消息通知
     *
     * @param noticeAckBody
     * @param imChannelContext
     */
    @Override
    protected void doNoticeAck(NoticeAckBody noticeAckBody, ImChannelContext imChannelContext) {
        if (noticeAckBody == null) {
            return;
        }
        // 设置请求线程
        String id = imChannelContext.getId();
        String userId = imChannelContext.getUserId();
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        boolean b =
            messageHelper.deleteMsgNotice(imChannelContext.getUserId(), noticeAckBody.getId(), noticeAckBody.getTo());
        if (b) {
            logger.warn("上下文ID [{}] 用户ID [{}]   修改消息通知成功! msgId [{}]", id, userId, noticeAckBody.getId());
        } else {
            logger.warn("上下文ID [{}] 用户ID [{}]   修改消息通知成功! msgId [{}]", id, userId, noticeAckBody.getId());
        }
    }
}
