package com.daren.chen.im.server.util;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.session.id.impl.UUIDSessionIdGenerator;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;

/**
 * IM聊天命令工具类
 *
 * @date 2018-09-05 23:29:30
 * @author WChao
 *
 */
public class ChatKit {

    private static final Logger log = Logger.getLogger(ChatKit.class);

    /**
     * 转换为聊天消息结构;
     *
     * @param body
     * @param imChannelContext
     * @return
     */
    public static ChatBody toChatBody(byte[] body, ImChannelContext imChannelContext) {
        ChatBody chatReqBody = parseChatBody(body);
        if (chatReqBody != null) {
            if (StringUtils.isEmpty(chatReqBody.getFrom())) {
                ImSessionContext imSessionContext = imChannelContext.getSessionContext();
                User user = imSessionContext.getImClientNode().getUser();
                if (user != null) {
                    chatReqBody.setFrom(user.getNick());
                } else {
                    chatReqBody.setFrom(imChannelContext.getId());
                }
            }
        }
        return chatReqBody;
    }

    /**
     * 判断是否属于指定格式聊天消息;
     *
     * @param body
     * @return
     */
    private static ChatBody parseChatBody(byte[] body) {
        if (body == null) {
            return null;
        }
        ChatBody chatReqBody = null;
        try {
            String text = new String(body, ImConst.CHARSET);
            chatReqBody = JsonKit.toBean(text, ChatBody.class);
            if (chatReqBody != null) {
                if (chatReqBody.getCreateTime() == null) {
                    // TODO 时间戳 改成纳秒
                    chatReqBody.setCreateTime(System.currentTimeMillis());
                }
                if (StringUtils.isEmpty(chatReqBody.getId())) {
                    chatReqBody.setId(UUIDSessionIdGenerator.INSTANCE.sessionId(null));
                }
                return chatReqBody;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return chatReqBody;
    }

    /**
     * 判断是否属于指定格式聊天消息;
     *
     * @param bodyStr
     * @return
     */
    public static ChatBody parseChatBody(String bodyStr) {
        if (bodyStr == null) {
            return null;
        }
        try {
            return parseChatBody(bodyStr.getBytes(ImConst.CHARSET));
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    /**
     * 判断用户是否在线
     *
     * @param userId
     *            用户ID
     * @param isStore
     *            是否开启持久化(true:开启,false:未开启)
     * @return
     */
    public static boolean isOnline(String userId, boolean isStore) {
        if (isStore) {
            ImServerConfig imServerConfig = ImConfig.Global.get();
            return imServerConfig.getMessageHelper().isOnline(userId);
        }
        List<ImChannelContext> imChannelContexts = JimServerAPI.getByUserId(userId);
        if (CollectionUtils.isNotEmpty(imChannelContexts)) {
            return true;
        }
        return false;
    }

    public static boolean isOnline(String userId) {
        List<ImChannelContext> imChannelContexts = JimServerAPI.getByUserId(userId);
        if (CollectionUtils.isNotEmpty(imChannelContexts)) {
            return true;
        }
        return false;
    }

    /**
     * 获取双方会话ID
     *
     * @param from
     * @param to
     * @return
     */
    public static String sessionId(String from, String to) {
        if (from.compareTo(to) <= 0) {
            return from + "-" + to;
        } else {
            return to + "-" + from;
        }
    }
}
