package com.daren.chen.im.server.common;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.Config;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.AdminStatisticsRespBody;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.core.packets.LoginRespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.protocol.AbstractProtocolHandler;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.protocol.ws.WsProtocolHandler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/26 11:21
 */
public class StatisticsUtils {

    private static final Logger log = LoggerFactory.getLogger(StatisticsUtils.class);

    private static Config config;

    // private static final
    private static final Map<String, AtomicInteger> SEND_FRIEND_CHAT_CAHCE = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> REVEIVE_FRIEND_CHAT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> SEND_GROUP_CHAT_CAHCE = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> REVEIVE_GROUP_CHAT_CACHE = new ConcurrentHashMap<>();

    private static final ArrayBlockingQueue<AdminTask> TASK_QUEUE = new ArrayBlockingQueue<>(100000, true);

    /**
     *
     */
    private volatile static boolean isStatistics = false;
    /**
     *
     */
    private volatile static String adminUserId = "";
    /**
     *
     */
    private static final Set<String> MSG_SET = new ConcurrentHashSet<>(100);

    private volatile static boolean isInit = false;

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        StatisticsUtils.config = config;
    }

    public static class AdminTask {

        private ImChannelContext imChannelContext;

        private ImPacket imPacket;

        public ImChannelContext getImChannelContext() {
            return imChannelContext;
        }

        public void setImChannelContext(ImChannelContext imChannelContext) {
            this.imChannelContext = imChannelContext;
        }

        public ImPacket getImPacket() {
            return imPacket;
        }

        public void setImPacket(ImPacket imPacket) {
            this.imPacket = imPacket;
        }

        public AdminTask(ImChannelContext imChannelContext, ImPacket imPacket) {
            this.imChannelContext = imChannelContext;
            this.imPacket = imPacket;
        }
    }

    /**
     * @param adminTask
     */
    public static void addTask(AdminTask adminTask) {
        if (config == null) {
            return;
        }
        if (!isInit) {
            synchronized (StatisticsUtils.class) {
                if (!isInit) {
                    isStatistics = config.getStatistics().getEnabled();
                    adminUserId = config.getStatistics().getUserId();
                }
            }
        }
        if (!isStatistics) {
            isInit = true;
            return;
        }

        if (adminTask == null) {
            return;
        }
        ImPacket imPacket = adminTask.getImPacket();
        if (imPacket == null) {
            return;
        }
        //
        Command command = imPacket.getCommand();
        if (command == null) {
            return;
        }
        boolean key = false;
        switch (command) {
            // 登录响应
            case COMMAND_LOGIN_RESP:
                break;
            // 发送消息
            case COMMAND_CHAT_REQ:
                break;
            // 聊天响应
            case COMMAND_CHAT_RESP:
                break;
            case GET_USER_ONLINE_STATUS_REQ_RESP:
                break;
            default:
                key = true;
                break;

        }
        if (key) {
            return;
        }
        //
        byte[] body = imPacket.getBody();
        if (body == null) {
            return;
        }
        try {
            String str = new String(body, ImConst.CHARSET);
            boolean add = MSG_SET.add(str);
            // 重复的消息不添加
            if (!add) {
                return;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        TASK_QUEUE.add(adminTask);
        if (!isInit) {
            synchronized (StatisticsUtils.class) {
                if (!isInit) {
                    isInit = true;
                    sendTask();
                    log.info("sendTask 线程初始化完成..");
                }
            }
        }
    }

    private static void sendTask() {

        new Thread(() -> {
            while (true) {
                AdminTask poll = TASK_QUEUE.poll();
                if (poll == null) {
                    continue;
                }
                sendTask(poll.getImChannelContext(), poll.imPacket);
            }
        }, "线程...").start();

    }

    /**
     *
     * @param userId
     */
    private static void initCache(String userId) {
        AtomicInteger sendFriendChatNum = SEND_FRIEND_CHAT_CAHCE.get(userId);
        if (sendFriendChatNum == null) {
            SEND_FRIEND_CHAT_CAHCE.put(userId, new AtomicInteger(0));
        }
        AtomicInteger reveiveFriendChatNum = REVEIVE_FRIEND_CHAT_CACHE.get(userId);
        if (reveiveFriendChatNum == null) {
            REVEIVE_FRIEND_CHAT_CACHE.put(userId, new AtomicInteger(0));
        }
        AtomicInteger sendGroupChatNum = SEND_GROUP_CHAT_CAHCE.get(userId);
        if (sendGroupChatNum == null) {
            SEND_GROUP_CHAT_CAHCE.put(userId, new AtomicInteger(0));
        }
        AtomicInteger reveiveGroupChatNum = REVEIVE_GROUP_CHAT_CACHE.get(userId);
        if (reveiveGroupChatNum == null) {
            REVEIVE_GROUP_CHAT_CACHE.put(userId, new AtomicInteger(0));
        }

    }

    /**
     *
     * @param imChannelContext
     * @param imPacket
     */
    private static void sendTask(ImChannelContext imChannelContext, ImPacket imPacket) {
        if (StringUtils.isBlank(adminUserId)) {
            return;
        }
        if (Command.COMMAND_HEARTBEAT_REQ != imPacket.getCommand()) {
            Command command = imPacket.getCommand();
            byte[] body = imPacket.getBody();
            if (body == null) {
                return;
            }
            try {
                String userId = imChannelContext.getUserId();
                String str = new String(body, ImConst.CHARSET);
                MSG_SET.remove(str);
                //
                List<ImChannelContext> byUserId = JimServerAPI.getByUserId(adminUserId);
                if (CollectionUtil.isEmpty(byUserId)) {
                    return;
                }
                //
                initCache(userId);
                AdminStatisticsRespBody adminStatisticsRespBody = decodeCommand(userId, command, str);
                if (adminStatisticsRespBody == null) {
                    return;
                }
                for (ImChannelContext channelContext : byUserId) {
                    ImServerChannelContext serverChannelContext = (ImServerChannelContext)channelContext;
                    AbstractProtocolHandler protocolHandler = serverChannelContext.getProtocolHandler();
                    if (protocolHandler instanceof WsProtocolHandler) {
                        ImPacket imPacket1 =
                            ProtocolManager.Converter.respPacket(adminStatisticsRespBody, channelContext);
                        JimServerAPI.send(channelContext, imPacket1);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    private static AdminStatisticsRespBody decodeCommand(String userId, Command command, String str) {
        Object r = null;
        if (command != Command.COMMAND_HEARTBEAT_REQ) {
        }
        switch (command) {

            // 登录响应
            case COMMAND_LOGIN_RESP:
                LoginRespBody loginRespBody = JSON.parseObject(str, LoginRespBody.class);
                if (loginRespBody.getCode() == 1007) {
                    User user = loginRespBody.getUser();
                    if (user == null) {
                        return null;
                    }
                    if (!userId.equals(user.getUserId())) {
                        return null;
                    }
                    AdminStatisticsRespBody adminStatisticsRespBody = new AdminStatisticsRespBody();
                    adminStatisticsRespBody.setUserId(user.getUserId());
                    adminStatisticsRespBody.setUserName(user.getNick());
                    adminStatisticsRespBody.setAvatar(user.getAvatar());
                    adminStatisticsRespBody.setStatus(user.getStatus());
                    adminStatisticsRespBody.setTerminal(user.getTerminal());
                    List<User> friends = user.getFriends();
                    adminStatisticsRespBody.setFriendNum(friends != null ? friends.size() : 0L);
                    List<String> groupIds = user.getGroupIds();
                    adminStatisticsRespBody.setGroupNum(friends != null ? groupIds.size() : 0L);
                    log.info("发送给admin 的 信息: {}", JSON.toJSONString(adminStatisticsRespBody));
                    return adminStatisticsRespBody;
                }
                break;
            // 申请进入群组响应
            case COMMAND_JOIN_GROUP_RESP:
                break;
            // 进入群组通知
            case COMMAND_JOIN_GROUP_NOTIFY_RESP:
                break;
            // 退出群组通知
            case COMMAND_EXIT_GROUP_NOTIFY_RESP:
                break;
            // 发送消息
            case COMMAND_CHAT_REQ:
                ChatBody chatBody = JSON.parseObject(str, ChatBody.class);
                String from = chatBody.getFrom();
                String groupId = chatBody.getGroupId();
                String to = chatBody.getTo();
                AtomicInteger sendFriendChatNum = SEND_FRIEND_CHAT_CAHCE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger reveiveFriendChatNum =
                    REVEIVE_FRIEND_CHAT_CACHE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger sendGroupChatNum = SEND_GROUP_CHAT_CAHCE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger reveiveGroupChatNum = REVEIVE_GROUP_CHAT_CACHE.getOrDefault(userId, new AtomicInteger(0));
                // 好友消息
                if (StringUtils.isBlank(groupId) && StringUtils.isNotBlank(to)) {
                    AdminStatisticsRespBody adminStatisticsRespBody =
                        new AdminStatisticsRespBody(userId, (long)sendFriendChatNum.incrementAndGet(),
                            (long)reveiveFriendChatNum.get(), (long)sendGroupChatNum.get(),
                            (long)reveiveGroupChatNum.get(), UserStatusType.ONLINE.getStatus());
                    log.info("发送给admin 的 信息: {}", JSON.toJSONString(adminStatisticsRespBody));
                    return adminStatisticsRespBody;
                }
                // 组消息
                AdminStatisticsRespBody adminStatisticsRespBody =
                    new AdminStatisticsRespBody(userId, (long)sendFriendChatNum.get(), (long)reveiveFriendChatNum.get(),
                        (long)sendGroupChatNum.incrementAndGet(), (long)reveiveGroupChatNum.get(),
                        UserStatusType.ONLINE.getStatus());
                log.info("发送给admin 的 信息: {}", JSON.toJSONString(adminStatisticsRespBody));
                return adminStatisticsRespBody;
            // 聊天响应
            case COMMAND_CHAT_RESP:
                ChatBody chatBody2 = JSON.parseObject(str, ChatBody.class);
                String from2 = chatBody2.getFrom();
                String groupId2 = chatBody2.getGroupId();
                String to2 = chatBody2.getTo();
                AtomicInteger sendFriendChatNum2 = SEND_FRIEND_CHAT_CAHCE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger reveiveFriendChatNum2 =
                    REVEIVE_FRIEND_CHAT_CACHE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger sendGroupChatNum2 = SEND_GROUP_CHAT_CAHCE.getOrDefault(userId, new AtomicInteger(0));
                AtomicInteger reveiveGroupChatNum2 =
                    REVEIVE_GROUP_CHAT_CACHE.getOrDefault(userId, new AtomicInteger(0));
                // 好友消息
                if (StringUtils.isBlank(groupId2) && StringUtils.isNotBlank(to2)) {
                    AdminStatisticsRespBody adminStatisticsRespBody2 =
                        new AdminStatisticsRespBody(userId, (long)sendFriendChatNum2.get(),
                            (long)reveiveFriendChatNum2.incrementAndGet(), (long)sendGroupChatNum2.get(),
                            (long)reveiveGroupChatNum2.get(), UserStatusType.ONLINE.getStatus());
                    log.info("发送给admin 的 信息: {}", JSON.toJSONString(adminStatisticsRespBody2));
                    return adminStatisticsRespBody2;
                }
                // 组消息
                AdminStatisticsRespBody adminStatisticsRespBody2 = new AdminStatisticsRespBody(userId,
                    (long)sendFriendChatNum2.get(), (long)reveiveFriendChatNum2.get(), (long)sendGroupChatNum2.get(),
                    (long)reveiveGroupChatNum2.incrementAndGet(), UserStatusType.ONLINE.getStatus());
                log.info("发送给admin 的 信息: {}", JSON.toJSONString(adminStatisticsRespBody2));
                return adminStatisticsRespBody2;
            // 收到撤消消息指令
            case COMMAND_CANCEL_MSG_RESP:
                break;
            // 获取用户信息响应
            case COMMAND_GET_USER_RESP:
                break;
            // 获取聊天消息响应
            case COMMAND_GET_MESSAGE_RESP:
                JSON.parseObject(str, AuthRespBody.class);
                break;
            case COMMAND_UNKNOW:
                break;
            case COMMAND_HANDSHAKE_REQ:
                break;
            case COMMAND_HANDSHAKE_RESP:
                break;
            case COMMAND_AUTH_REQ:
                break;
            case COMMAND_AUTH_RESP:
                break;
            case COMMAND_LOGIN_REQ:
                break;
            case COMMAND_JOIN_GROUP_REQ:
                break;
            case COMMAND_HEARTBEAT_REQ:
                break;
            case COMMAND_CLOSE_REQ:
                break;
            case COMMAND_CANCEL_MSG_REQ:
                break;
            case COMMAND_GET_USER_REQ:
                break;
            case COMMAND_GET_MESSAGE_REQ:
                break;
            case COMMAND_NOTICE_MESSAGE_REQ:
                break;
            case COMMAND_NOTICE_MESSAGE_RESP:
                break;
            case COMMAND_EXIT_GROUP_REQ:
                break;
            case COMMAND_ADD_FRIEND_REQ:
                break;
            case COMMAND_ADD_FRIEND_REQ_RESP:
                break;
            case COMMAND_DELETE_FRIEND_REQ:
                break;
            case COMMAND_DELETE_FRIEND_REQ_RESP:
                break;
            case GET_USER_ONLINE_STATUS_REQ:
                break;
            case GET_USER_ONLINE_STATUS_REQ_RESP:
                //
                GetUserOnlineStatusRespBody getUserOnlineStatusRespBody =
                    JSON.parseObject(str, GetUserOnlineStatusRespBody.class);
                String userId1 = getUserOnlineStatusRespBody.getUserId();
                return new AdminStatisticsRespBody(userId1, getUserOnlineStatusRespBody.getStatus());
            case MSG_NOTICE_REQ:
                break;
            case MSG_NOTICE_REQ_RESP:
                break;
            case NOTICE_OFFLINE_REQ:
                break;
            case NOTICE_OFFLINE_REQ_RESP:
                break;
            case RECEIVE_MSG_NOTICE_REQ:
                break;
            case RECEIVE_MSG_NOTICE_REQ_RESP:
                break;
            case CHAT_ACK_REQ:
                break;
            case NOTICE_ACK_REQ:
                break;
            case ADMIN_MSG_REQ:
                break;
            default:
                break;
        }
        return null;
    }

    private static ImPacket convertPacket(ImChannelContext imChannelContext, ImPacket packet) {
        if (Objects.isNull(imChannelContext) || Objects.isNull(packet)) {
            return null;
        }
        try {
            ImPacket respPacket = ProtocolManager.Converter.respPacket(packet, packet.getCommand(), imChannelContext);
            if (respPacket == null) {
                return null;
            }
            respPacket.setSynSeq(packet.getSynSeq());
            return respPacket;
        } catch (ImException e) {
            return null;
        }
    }
}
