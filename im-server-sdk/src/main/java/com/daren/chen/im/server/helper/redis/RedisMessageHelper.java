package com.daren.chen.im.server.helper.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.core.cache.redis.JedisTemplate;
import com.daren.chen.im.core.cache.redis.RedisCache;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.message.AbstractMessageHelper;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.MsgNoticeReq;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.NoticeOfflineReq;
import com.daren.chen.im.core.packets.NoticeOfflineRespBody;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.util.ChatKit;

/**
 * Redis获取持久化+同步消息助手;
 *
 * @author WChao
 * @date 2018年4月9日 下午4:39:30
 */
public class RedisMessageHelper extends AbstractMessageHelper {

    private final Logger log = LoggerFactory.getLogger(RedisMessageHelper.class);

    private static final String SUFFIX = ":";

    public RedisMessageHelper() {
        this.imConfig = ImConfig.Global.get();
    }

    @Override
    public ImStoreBindListener getBindListener() {

        return new RedisImStoreBindListener(imConfig, this);
    }

    /**
     * 判断用户是否在线
     *
     * @param userId
     * @return
     */
    @Override
    public boolean isOnline(String userId) {
        try {
            String keyPattern = USER + SUFFIX + userId + SUFFIX + TERMINAL;
            Set<String> terminalKeys = JedisTemplate.me().keys(keyPattern);
            if (CollectionUtils.isEmpty(terminalKeys)) {
                return false;
            }
            for (String terminalKey : terminalKeys) {
                terminalKey = terminalKey.substring(terminalKey.indexOf(userId));
                String isOnline = RedisCacheManager.getCache(USER).get(terminalKey, String.class);
                if (UserStatusType.ONLINE.getStatus().equals(isOnline)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return false;
    }

    /**
     * 获取组的所有用户
     *
     * @param groupId
     * @return
     */
    @Override
    public List<String> getGroupUsers(String operateUserId, String groupId) {
        String groupUserKey = groupId + SUFFIX + USER;
        return RedisCacheManager.getCache(GROUP).listGetAll(groupUserKey);
    }

    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        double score = chatBody.getCreateTime();
        RedisCacheManager.getCache(timelineTable).sortSetPush(timelineId, score, chatBody);
    }

    @Override
    public void saveNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {

    }

    @Override
    public void removeNoReadMessage(String timelineTable, String timelineId) {

    }

    @Override
    public void writeMessageOfSql(String operateUserId, ChatBody chatBody) {

    }

    /**
     * 往群组添加用户
     *
     * @param userId
     * @param groupId
     */
    @Override
    public void addGroupUser(String operateUserId, String userId, String groupId) {
        List<String> users = RedisCacheManager.getCache(GROUP).listGetAll(groupId);
        if (users.contains(userId)) {
            return;
        }
        RedisCacheManager.getCache(GROUP).listPushTail(groupId, userId);
    }

    /**
     * 往群组里面删除用户
     *
     * @param userId
     * @param groupId
     */
    @Override
    public void removeGroupUser(String operateUserId, String userId, String groupId) {
        RedisCacheManager.getCache(GROUP).listRemove(groupId, userId);
    }

    /**
     * 获取好友的离线消息
     *
     * @param userId
     * @param fromUserId
     * @return
     */
    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId, String fromUserId) {
        String userFriendKey = USER + SUFFIX + userId + SUFFIX + fromUserId;
        List<String> messageList = RedisCacheManager.getCache(PUSH).sortSetGetAll(userFriendKey);
        List<ChatBody> messageDataList = JsonKit.toArray(messageList, ChatBody.class);
        RedisCacheManager.getCache(PUSH).remove(userFriendKey);
        return putFriendsMessage(new UserMessageData(userId), messageDataList, null);
    }

    /**
     * 获取用户的离线消息
     *
     * @param userId
     * @return
     */
    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            Set<String> userKeys = JedisTemplate.me().keys(PUSH + SUFFIX + USER + SUFFIX + userId);
            // 获取好友离线消息;
            if (CollectionUtils.isNotEmpty(userKeys)) {
                List<ChatBody> messageList = new ArrayList<ChatBody>();
                Iterator<String> userKeyIterator = userKeys.iterator();
                while (userKeyIterator.hasNext()) {
                    String userKey = userKeyIterator.next();
                    userKey = userKey.substring(userKey.indexOf(USER + SUFFIX));
                    List<String> messages = RedisCacheManager.getCache(GROUP).sortSetGetAll(userKey);
                    RedisCacheManager.getCache(GROUP).remove(userKey);
                    messageList.addAll(JsonKit.toArray(messages, ChatBody.class));
                }
                putFriendsMessage(messageData, messageList, null);
            }
            List<String> groupIdList = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
            // 获取群组离线消息;
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupIdList.forEach(groupId -> {
                    UserMessageData groupMessageData = getGroupOfflineMessage(operateUserId, userId, groupId);
                    if (Objects.isNull(groupMessageData)) {
                        return;
                    }
                    putGroupMessage(messageData, groupMessageData.getGroups().get(groupId));
                });
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return messageData;
    }

    /**
     * 获取用户群组离线消息
     *
     * @param userId
     * @param groupId
     * @return
     */
    @Override
    public UserMessageData getGroupOfflineMessage(String operateUserId, String userId, String groupId) {
        UserMessageData messageData = new UserMessageData(userId);
        String userGroupKey = GROUP + SUFFIX + groupId + SUFFIX + userId;
        List<String> messages = RedisCacheManager.getCache(PUSH).sortSetGetAll(userGroupKey);
        if (CollectionUtils.isEmpty(messages)) {
            return messageData;
        }
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        RedisCacheManager.getCache(PUSH).remove(userGroupKey);
        return messageData;
    }

    /**
     * 获取好友历史消息
     *
     * @param userId
     * @param fromUserId
     * @param beginTime
     * @param endTime
     * @param offset
     * @param count
     * @return
     */
    @Override
    public UserMessageData getFriendHistoryMessage(String operateUserId, String userId, String fromUserId,
        Double beginTime, Double endTime, Integer offset, Integer count) {
        String sessionId = ChatKit.sessionId(userId, fromUserId);
        String userSessionKey = USER + SUFFIX + sessionId;
        List<String> messages = getHistoryMessage(userSessionKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putFriendsMessage(messageData, JsonKit.toArray(messages, ChatBody.class), fromUserId);
        return messageData;
    }

    /**
     * 获取群组历史 消息
     *
     * @param userId
     * @param groupId
     * @param beginTime
     * @param endTime
     * @param offset
     * @param count
     * @return
     */
    @Override
    public UserMessageData getGroupHistoryMessage(String operateUserId, String userId, String groupId, Double beginTime,
        Double endTime, Integer offset, Integer count) {
        String groupKey = GROUP + SUFFIX + groupId;
        List<String> messages = getHistoryMessage(groupKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        return messageData;
    }

    @Override
    public boolean updateLastMessageId(String operateUserId, String userId, ChatAckBody chatBody) {
        return false;
    }

    /**
     * 获取历史消息公共方法
     *
     * @param historyKey
     * @param beginTime
     * @param endTime
     * @param offset
     * @param count
     * @return
     */
    private List<String> getHistoryMessage(String historyKey, Double beginTime, Double endTime, Integer offset,
        Integer count) {
        boolean isTimeBetween = (beginTime != null && endTime != null);
        boolean isPage = (offset != null && count != null);
        RedisCache storeCache = RedisCacheManager.getCache(STORE);
        // 消息区间，不分页
        if (isTimeBetween && !isPage) {
            return storeCache.sortSetGetAll(historyKey, beginTime, endTime);
            // 消息区间，并且分页;
        } else if (isTimeBetween && isPage) {
            return storeCache.sortSetGetAll(historyKey, beginTime, endTime, offset, count);
            // 所有消息，并且分页;
        } else if (isPage) {
            return storeCache.sortSetGetAll(historyKey, 0, Double.MAX_VALUE, offset, count);
            // 所有消息，不分页;
        } else {
            return storeCache.sortSetGetAll(historyKey);
        }
    }

    /**
     * 放入用户群组消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putGroupMessage(UserMessageData userMessage, List<ChatBody> messages) {
        if (Objects.isNull(userMessage) || CollectionUtils.isEmpty(messages)) {
            return userMessage;
        }
        messages.forEach(chatBody -> {
            String groupId = chatBody.getGroupId();
            if (StringUtils.isEmpty(groupId)) {
                return;
            }
            List<ChatBody> groupMessages = userMessage.getGroups().get(groupId);
            if (CollectionUtils.isEmpty(groupMessages)) {
                groupMessages = new ArrayList();
                userMessage.getGroups().put(groupId, groupMessages);
            }
            groupMessages.add(chatBody);
        });
        return userMessage;
    }

    /**
     * 组装放入用户好友消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putFriendsMessage(UserMessageData userMessage, List<ChatBody> messages, String friendId) {
        if (Objects.isNull(userMessage) || CollectionUtils.isEmpty(messages)) {
            return userMessage;
        }
        messages.forEach(chatBody -> {
            String fromId = chatBody.getFrom();
            if (StringUtils.isEmpty(fromId)) {
                return;
            }
            String targetFriendId = friendId;
            if (StringUtils.isEmpty(targetFriendId)) {
                targetFriendId = fromId;
            }
            List<ChatBody> friendMessages = userMessage.getFriends().get(targetFriendId);
            if (CollectionUtils.isEmpty(friendMessages)) {
                friendMessages = new ArrayList();
                userMessage.getFriends().put(targetFriendId, friendMessages);
            }
            friendMessages.add(chatBody);
        });
        return userMessage;
    }

    /**
     * 获取群组所有成员信息
     *
     * @param groupId
     *            群组ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public Group getGroupUsers(String operateUserId, String groupId, Integer type) {
        if (Objects.isNull(groupId) || Objects.isNull(type)) {
            log.warn("group:{} or type:{} is null", groupId, type);
            return null;
        }
        Group group = RedisCacheManager.getCache(GROUP).get(groupId + SUFFIX + INFO, Group.class);
        if (Objects.isNull(group)) {
            return null;
        }
        List<String> userIds = this.getGroupUsers(operateUserId, groupId);
        if (CollectionUtils.isEmpty(userIds)) {
            return null;
        }
        List<User> users = new ArrayList<>();
        userIds.forEach(userId -> {
            User user = getUserByType(operateUserId, userId, type);
            if (Objects.isNull(user)) {
                return;
            }
            validateStatusByType(type, users, user);
        });
        group.setUsers(users);
        return group;
    }

    /**
     * 根据获取type校验是否组装User
     *
     * @param type
     * @param users
     * @param user
     */
    private void validateStatusByType(Integer type, List<User> users, User user) {
        String status = user.getStatus();
        if (UserStatusType.ONLINE.getNumber() == type && UserStatusType.ONLINE.getStatus().equals(status)) {
            users.add(user);
        } else if (UserStatusType.OFFLINE.getNumber() == type && UserStatusType.OFFLINE.getStatus().equals(status)) {
            users.add(user);
        } else if (UserStatusType.ALL.getNumber() == type) {
            users.add(user);
        }
    }

    @Override
    public User getUserByType(String operateUserId, String userId, Integer type) {
        User user = RedisCacheManager.getCache(USER).get(userId + SUFFIX + INFO, User.class);
        if (Objects.isNull(user)) {
            return null;
        }
        boolean isOnline = this.isOnline(userId);
        String status = isOnline ? UserStatusType.ONLINE.getStatus() : UserStatusType.OFFLINE.getStatus();
        if (UserStatusType.ONLINE.getNumber() == type && isOnline) {
            user.setStatus(status);
            return user;
        } else if (UserStatusType.OFFLINE.getNumber() == type && !isOnline) {
            user.setStatus(status);
            return user;
        } else if (type == UserStatusType.ALL.getNumber()) {
            user.setStatus(status);
            return user;
        }
        return null;
    }

    // @Override
    // public Group getFriendUsers(String userId, String friendGroupId, Integer type) {
    // boolean isTrue = Objects.isNull(userId) || Objects.isNull(friendGroupId) || Objects.isNull(type);
    // if (isTrue) {
    // log.warn("userId:{} or friendGroupId:{} or type:{} is null");
    // return null;
    // }
    // List<Group> friends = RedisCacheManager.getCache(USER).get(userId + SUFFIX + FRIENDS, List.class);
    // if (CollectionUtils.isEmpty(friends)) {
    // return null;
    // }
    // for (Group group : friends) {
    // if (!friendGroupId.equals(group.getGroupId())) {
    // continue;
    // }
    // List<User> users = group.getUsers();
    // if (CollectionUtils.isEmpty(users)) {
    // return group;
    // }
    // List<User> userResults = new ArrayList<User>();
    // for (User user : users) {
    // initUserStatus(user);
    // validateStatusByType(type, userResults, user);
    // }
    // group.setUsers(userResults);
    // return group;
    // }
    // return null;
    // }

    /**
     * 初始化用户在线状态;
     *
     * @param user
     */
    public boolean initUserStatus(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getUserId())) {
            return false;
        }
        String userId = user.getUserId();
        boolean isOnline = this.isOnline(userId);
        if (isOnline) {
            user.setStatus(UserStatusType.ONLINE.getStatus());
        } else {
            user.setStatus(UserStatusType.OFFLINE.getStatus());
        }
        return true;
    }

    /**
     * 获取好友分组所有成员信息
     *
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    // @Override
    // public List<Group> getAllFriendUsers(String userId, Integer type) {
    // if (Objects.isNull(userId)) {
    // return null;
    // }
    // List<JSONObject> friendJsonArray = RedisCacheManager.getCache(USER).get(userId + SUFFIX + FRIENDS, List.class);
    // if (CollectionUtils.isEmpty(friendJsonArray)) {
    // return null;
    // }
    // List<Group> friends = new ArrayList<Group>();
    // friendJsonArray.forEach(groupJson -> {
    // Group group = JSONObject.toJavaObject(groupJson, Group.class);
    // List<User> users = group.getUsers();
    // if (CollectionUtils.isEmpty(users)) {
    // return;
    // }
    // List<User> userResults = new ArrayList<User>();
    // for (User user : users) {
    // initUserStatus(user);
    // validateStatusByType(type, userResults, user);
    // }
    // group.setUsers(userResults);
    // friends.add(group);
    // });
    // return friends;
    // }
    @Override
    public List<User> getAllFriendUsers(String operateUserId, String userId, Integer type) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<JSONObject> friendJsonArray = RedisCacheManager.getCache(USER).get(userId + SUFFIX + FRIENDS, List.class);
        if (CollectionUtils.isEmpty(friendJsonArray)) {
            return null;
        }
        List<User> friends = new ArrayList<>();
        friendJsonArray.forEach(groupJson -> {
            User user = JSONObject.toJavaObject(groupJson, User.class);
            initUserStatus(user);
            validateStatusByType(type, friends, user);
        });
        return friends;
    }

    @Override
    public List<String> getAllFriendUserIds(String operateUserId, String userId) {
        return null;
    }

    @Override
    public List<Group> getAllGroupUsers(String operateUserId, String userId, Integer type) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<String> groupIds = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
        if (CollectionUtils.isEmpty(groupIds)) {
            return null;
        }
        List<Group> groups = new ArrayList<Group>();
        groupIds.forEach(groupId -> {
            Group group = getGroupUsers(operateUserId, groupId, type);
            if (Objects.isNull(group)) {
                return;
            }
            groups.add(group);
        });
        return groups;
    }

    /**
     * 更新用户终端协议类型及在线状态;
     *
     * @param user
     *            用户信息
     */
    @Override
    public boolean updateUserTerminal(String operateUserId, User user) {
        String userId = user.getUserId();
        String terminal = user.getTerminal();
        String status = user.getStatus();
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(terminal) || StringUtils.isEmpty(status)) {
            log.error("userId:{},terminal:{},status:{} must not null", userId, terminal, status);
            return false;
        }
        RedisCacheManager.getCache(USER).put(userId + SUFFIX + TERMINAL + SUFFIX + terminal, user.getStatus());
        return true;
    }

    @Override
    public boolean addFriendApply(String operateUserId, String curUserId, String friendUserId, String content) {
        return false;
    }

    @Override
    public String getUserOnlineStatus(String operateUserId, String userId) {
        return null;
    }

    // @Override
    // public MsgNoticeRespBody msgNotice(MsgNoticeReq msgNoticeReq) {
    // return null;
    // }

    @Override
    public MsgNoticeRespBody msgNoticeOffline(String operateUserId, MsgNoticeReq msgNoticeReq) {
        return null;
    }

    @Override
    public NoticeOfflineRespBody noticeOffLine(String operateUserId, NoticeOfflineReq noticeOfflineReq) {
        return null;
    }

    @Override
    public ReceiveMsgNoticeRespBody receiveMsgNotice(String operateUserId, String userId) {
        return null;
    }

    @Override
    public Map<String, Object> dissolveGroup(String operateUserId, MsgNoticeReq msgNoticeReq) {
        return null;
    }

    @Override
    public boolean deleteMsgNotice(String operateUserId, String noticeMsgId, String noticeUserId) {
        return false;
    }

    @Override
    public boolean addUserOnlineStatusRecord(String operateUserId, Map<String, Object> paramMap) {
        return false;
    }

    @Override
    public boolean addTopicName(String topicName) {
        return true;
    }

    @Override
    public List<String> findAllTopicName() {
        return null;
    }

    @Override
    public void addUserLoginInfo(String timelineTable, String userId, LoginUser loginUser) {

    }

    @Override
    public LoginUser getUserLoginInfo(String timelineTable, String userId) {
        return null;
    }

    @Override
    public User getUserBaseInfoByToken(String token) {
        return null;
    }

    /**
     * 获取用户拥有的群组;
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> getGroups(String operateUserId, String userId) {
        return RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
    }

    static {
        RedisCacheManager.register(USER, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(GROUP, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(STORE, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(PUSH, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
    }

}
