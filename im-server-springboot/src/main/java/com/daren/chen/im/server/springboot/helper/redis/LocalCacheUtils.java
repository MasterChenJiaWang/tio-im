package com.daren.chen.im.server.springboot.helper.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cache.redis.RedisCache;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.cache.redis.RedisTemplateUtils;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.util.ChatKit;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollectionUtil;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/10 8:59
 */
public class LocalCacheUtils implements ImConst, Serializable {
    private static final long serialVersionUID = 498920424611011552L;

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(LocalCacheUtils.class);

    /**
     *
     */
    private static volatile LocalCacheUtils instance = null;

    /**
     * 缓存时间
     */
    private final long timeOut = 86400000;
    /**
     *
     */
    private final int num = 10000;
    /**
     * user基础信息 缓存
     */
    private final Cache<String, User> userInfoCache = CacheUtil.newLRUCache(num);
    /**
     * user 好友id 缓存
     */
    private final Cache<String, List<String>> userFriendIdsCache = CacheUtil.newLRUCache(num);
    /**
     * user 组id 缓存
     */
    private final Cache<String, List<String>> userGroupIdsCache = CacheUtil.newLRUCache(num);

    /**
     * 组 基础信息 缓存
     */
    private final Cache<String, Group> groupInfoCache = CacheUtil.newLRUCache(num);
    /**
     * user 组id 缓存
     */
    private final Cache<String, List<String>> gruopUserIdsCache = CacheUtil.newLRUCache(num);

    /**
     * 是否 集群 如果 集群 就不使用本地缓存
     */
    private static volatile boolean IS_CLUSTER = false;
    /**
     * 是否持久化
     */
    private static volatile boolean IS_STORE = false;

    /**
     * @return
     * @throws Exception
     */
    public static LocalCacheUtils me() {
        if (instance == null) {
            synchronized (LocalCacheUtils.class) {
                if (instance == null) {
                    instance = new LocalCacheUtils();
                    ImServerConfig imServerConfig = ImServerConfig.Global.get();
                    IS_CLUSTER = ImServerConfig.ON.equals(imServerConfig.getIsCluster());
                    IS_STORE = ImServerConfig.ON.equals(imServerConfig.getIsStore());
                }
            }
        }
        return instance;
    }

    /**
     * @param userId
     * @return
     */
    private User getUserInfo(String userId) {
        if (!IS_STORE) {
            return null;
        }
        if (!IS_CLUSTER) {
            return null;
        }
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return userInfoCache.get(userId);
    }

    /**
     * @param userId
     * @param user
     */
    private void saveUserInfo(String userId, User user) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userInfoCache.put(userId, user);
    }

    /**
     * @param userId
     */
    private void removeUserInfo(String userId) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userInfoCache.remove(userId);
    }

    /**
     *
     */
    private void clearUserInfo() {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userInfoCache.clear();
    }

    /**
     * @param userId
     * @return
     */
    private List<String> getUserFriendIds(String userId) {
        if (!IS_STORE) {
            return null;
        }
        if (!IS_CLUSTER) {
            return null;
        }
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return userFriendIdsCache.get(userId);
    }

    /**
     * @param userId
     * @param idList
     */
    private void saveUserFriendIds(String userId, List<String> idList) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userFriendIdsCache.put(userId, idList);
    }

    /**
     * @param userId
     */
    private void removeUserFriendIds(String userId) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userFriendIdsCache.remove(userId);
    }

    /**
     *
     */
    private void clearUserFriendIds() {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userFriendIdsCache.clear();
    }

    /**
     * @param userId
     * @return
     */
    private List<String> getUserGroupIds(String userId) {
        if (!IS_STORE) {
            return null;
        }
        if (!IS_CLUSTER) {
            return null;
        }
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return userGroupIdsCache.get(userId);
    }

    /**
     * @param userId
     * @param idList
     */
    private void saveUserGroupIds(String userId, List<String> idList) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        if (StringUtils.isBlank(userId)) {
            return;
        }
        userGroupIdsCache.put(userId, idList);
    }

    /**
     * @param userId
     */
    private void removeUserGroupIds(String userId) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userGroupIdsCache.remove(userId);
    }

    /**
     *
     */
    private void clearUserGroupIds() {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        userGroupIdsCache.clear();
    }

    /**
     * @param groupId
     * @return
     */
    private Group getGroupInfo(String groupId) {
        if (!IS_STORE) {
            return null;
        }
        if (!IS_CLUSTER) {
            return null;
        }
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
        return groupInfoCache.get(groupId);
    }

    /**
     * @param groupId
     * @param group
     */
    private void saveGroupInfo(String groupId, Group group) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        if (StringUtils.isBlank(groupId)) {
            return;
        }
        groupInfoCache.put(groupId, group);
    }

    private void removeGroupInfo(String groupId) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        groupInfoCache.remove(groupId);
    }

    /**
     *
     */
    private void clearGroupInfo() {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        groupInfoCache.clear();
    }

    /**
     * @param groupId
     * @return
     */
    private List<String> getGroupUserIds(String groupId) {
        if (!IS_STORE) {
            return null;
        }
        if (!IS_CLUSTER) {
            return null;
        }
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
        return gruopUserIdsCache.get(groupId);
    }

    /**
     * @param groupId
     * @param idList
     */
    private void saveGroupUserIds(String groupId, List<String> idList) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        if (StringUtils.isBlank(groupId)) {
            return;
        }
        gruopUserIdsCache.put(groupId, idList);
    }

    private void removeGroupUserIds(String groupId) {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        gruopUserIdsCache.remove(groupId);
    }

    /**
     *
     */
    private void clearGroupUserIds() {
        if (!IS_STORE) {
            return;
        }
        if (!IS_CLUSTER) {
            return;
        }
        gruopUserIdsCache.clear();
    }

    /**
     * @param userId
     * @return
     */
    public User getUserInfoByCache(String userId) {
        if (!IS_STORE) {
            return null;
        }
        User user = this.getUserInfo(userId);
        if (user == null) {
            user = RedisCacheManager.getCache(USER).get(userId + SUFFIX + INFO, User.class);
            if (user != null) {
                this.saveUserInfo(userId, user);
            }
        }
        return user;
    }

    /**
     * @param userId
     * @param user
     */
    public void saveUserInfoByCache(String userId, User user) {
        if (!IS_STORE) {
            return;
        }
        if (user != null) {
            RedisCacheManager.getCache(USER).put(userId + SUFFIX + INFO, user);
            this.saveUserInfo(userId, user);
        }
    }

    /**
     * @param userId
     * @return
     */
    public List<String> getUserFriendIdsByCache(String userId) {
        if (!IS_STORE) {
            return null;
        }
        List<String> userFriendIds = this.getUserFriendIds(userId);
        if (CollectionUtils.isEmpty(userFriendIds)) {
            userFriendIds = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + FRIENDS);
            if (CollectionUtils.isNotEmpty(userFriendIds)) {
                this.saveUserFriendIds(userId, userFriendIds);
            }
        }
        return userFriendIds;
    }

    /**
     * @param userId
     * @param friendIds
     */
    public void saveUserFriendIdsByCache(String userId, List<String> friendIds) {
        if (!IS_STORE) {
            return;
        }
        String cacheKey = userId + SUFFIX + FRIENDS;
        if (CollectionUtil.isNotEmpty(friendIds)) {
            // 先删后增
            RedisCacheManager.getCache(USER).remove(cacheKey);
            for (String friendId : friendIds) {
                RedisCacheManager.getCache(USER).listPushTail(cacheKey, friendId);
            }
            this.saveUserFriendIds(userId, friendIds);
        }
    }

    /**
     * @param userId
     * @return
     */
    public List<String> getUserGroupIdsByCache(String userId) {
        if (!IS_STORE) {
            return null;
        }
        List<String> userGroupIds = this.getUserGroupIds(userId);
        if (CollectionUtils.isEmpty(userGroupIds)) {
            userGroupIds = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
            if (CollectionUtils.isNotEmpty(userGroupIds)) {
                this.saveUserGroupIds(userId, userGroupIds);
            }
        }
        return userGroupIds;
    }

    /**
     * @param userId
     * @param groupIds
     */
    public void saveUserGroupIdsByCache(String userId, List<String> groupIds) {
        if (!IS_STORE) {
            return;
        }
        String cacheKey = userId + SUFFIX + GROUP;
        if (CollectionUtil.isNotEmpty(groupIds)) {
            // 先删后增
            RedisCacheManager.getCache(USER).remove(cacheKey);
            for (String groupId : groupIds) {
                RedisCacheManager.getCache(USER).listPushTail(cacheKey, groupId);
            }
            this.saveUserGroupIds(userId, groupIds);
        }
    }

    /**
     * @param groupId
     * @return
     */
    public List<String> getGroupUserIdsByCache(String groupId) {
        if (!IS_STORE) {
            return null;
        }
        List<String> groupUserIds = this.getGroupUserIds(groupId);
        if (CollectionUtils.isEmpty(groupUserIds)) {
            groupUserIds = RedisCacheManager.getCache(GROUP).listGetAll(groupId);
            if (CollectionUtils.isNotEmpty(groupUserIds)) {
                this.saveGroupUserIds(groupId, groupUserIds);
            }
        }
        return groupUserIds;
    }

    /**
     * @param groupId
     * @param userIds
     */
    public void saveGroupUserIdsByCache(String groupId, List<String> userIds) {
        if (!IS_STORE) {
            return;
        }
        if (CollectionUtil.isNotEmpty(userIds)) {
            // 先删后增
            RedisCacheManager.getCache(GROUP).remove(groupId);
            for (String userId : userIds) {
                RedisCacheManager.getCache(GROUP).listPushTail(groupId, userId);
            }
            this.saveGroupUserIds(groupId, userIds);
        }
    }

    /**
     * @param groupId
     * @param userId
     */
    public void saveGroupUserIdsByCache(String groupId, String userId) {
        if (!IS_STORE) {
            return;
        }
        if (StringUtils.isNotBlank(userId)) {
            List<String> users = RedisCacheManager.getCache(GROUP).listGetAll(groupId);
            if (users == null) {
                users = new ArrayList<>();
            }
            users.add(userId);
            saveGroupUserIdsByCache(groupId, users);
        }
    }

    /**
     * @param groupId
     */
    public void removeGroupUserIdsByCache(String groupId) {
        if (!IS_STORE) {
            return;
        }
        this.removeGroupUserIds(groupId);
        RedisCacheManager.getCache(GROUP).remove(groupId);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.removeGroupUserIds(groupId);
    }

    /**
     * @param groupId
     * @param userId
     */
    public void removeGroupUserIdByCache(String groupId, String userId) {
        if (!IS_STORE) {
            return;
        }
        this.removeGroupUserIds(groupId);
        RedisCacheManager.getCache(GROUP).listRemove(groupId, userId);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.removeGroupUserIds(groupId);
    }

    /**
     * @param groupId
     * @return
     */
    public Group getGroupInfoByCache(String groupId) {
        if (!IS_STORE) {
            return null;
        }
        Group group = this.getGroupInfo(groupId);
        if (group == null) {
            group = RedisCacheManager.getCache(GROUP).get(groupId + SUFFIX + INFO, Group.class);
            if (group != null) {
                this.saveGroupInfo(groupId, group);
            }
        }
        return group;
    }

    /**
     * @param groupId
     * @param group
     */
    public void saveGroupInfoByCache(String groupId, Group group) {
        if (!IS_STORE) {
            return;
        }
        if (group != null) {
            Group group1 = Group.newBuilder().build();
            BeanUtils.copyProperties(group, group1);
            group1.setUsers(null);
            RedisCacheManager.getCache(GROUP).put(groupId + SUFFIX + INFO, group1);
            this.saveGroupInfo(groupId, group1);
        }
    }

    /**
     * @param userId
     */
    public void removeUserInfoCache(String userId) {
        if (!IS_STORE) {
            return;
        }
        this.removeUserGroupIds(userId);
        this.removeUserInfo(userId);
        this.removeUserFriendIds(userId);
        // 移除用户基础信息;
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + INFO);
        // 移除用户好友成员;
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + FRIENDS);
        // 移除群组成员;
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + GROUP);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //
        this.removeUserGroupIds(userId);
        this.removeUserInfo(userId);
        this.removeUserFriendIds(userId);
    }

    /**
     * @param userId
     */
    public void removeUserMessageCache(String userId, String friendId) {
        if (!IS_STORE) {
            return;
        }
        String userFriendKey = USER + SUFFIX + userId + SUFFIX + friendId;
        // 移除用户离线信息;
        RedisCacheManager.getCache(PUSH).remove(userFriendKey);
        //
        String userFriendKey2 = USER + SUFFIX + friendId + SUFFIX + userId;
        // 移除用户离线信息;
        RedisCacheManager.getCache(PUSH).remove(userFriendKey2);
    }

    /**
     * @param groupId
     */
    public void removeGroupInfoCache(String groupId) {
        if (!IS_STORE) {
            return;
        }
        //
        this.removeGroupUserIds(groupId);
        //
        this.removeGroupInfo(groupId);
        //
        List<String> list = RedisCacheManager.getCache(GROUP).listGetAll(groupId);
        if (CollectionUtil.isNotEmpty(list)) {
            for (String userId : list) {
                this.removeUserGroupIds(userId);
            }
        }
        // 移除群组成员;
        RedisCacheManager.getCache(GROUP).remove(groupId);
        // 移除群信息
        RedisCacheManager.getCache(GROUP).remove(groupId + SUFFIX + INFO);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //
        this.removeGroupUserIds(groupId);
        //
        this.removeGroupInfo(groupId);
    }

    /**
     * @param groupId
     * @param userId
     */
    public void removeGroupUserIdCache(String groupId, String userId) {
        if (!IS_STORE) {
            return;
        }
        this.removeGroupUserIds(groupId);
        this.removeGroupInfo(groupId);
        this.removeUserGroupIds(userId);
        // 移除群组成员;
        RedisCacheManager.getCache(GROUP).listRemove(groupId, userId);
        // 移除成员群组;
        RedisCacheManager.getCache(USER).listRemove(userId + SUFFIX + GROUP, groupId);
        // // 移除群组离线消息
        RedisCacheManager.getCache(PUSH).remove(GROUP + SUFFIX + groupId + SUFFIX + userId);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.removeGroupUserIds(groupId);
        this.removeGroupInfo(groupId);
        this.removeUserGroupIds(userId);
    }

    /**
     * @param groupId
     */
    public void dissolveGroupCache(String groupId) {
        if (!IS_STORE) {
            return;
        }
        //
        removeGroupInfoCache(groupId);
        // 移除群组历史消息
        RedisCacheManager.getCache(STORE).remove(GROUP + SUFFIX + groupId);
        // 移除群组未读消息
        try {
            Set<String> keys =
                RedisTemplateUtils.getRedisTemplate().keys(PUSH + SUFFIX + GROUP + SUFFIX + groupId + SUFFIX);
            if (CollectionUtil.isNotEmpty(keys)) {
                for (String key : keys) {
                    key = key.substring(key.indexOf(GROUP));
                    RedisCacheManager.getCache(PUSH).remove(key);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param userId
     */
    public void clearUserCache(String userId) {
        if (!IS_STORE) {
            return;
        }
        this.removeUserGroupIds(userId);
        this.removeUserFriendIds(userId);
        this.removeUserInfo(userId);
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + FRIENDS);
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + GROUP);
        RedisCacheManager.getCache(USER).remove(userId + SUFFIX + INFO);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.removeUserGroupIds(userId);
        this.removeUserFriendIds(userId);
        this.removeUserInfo(userId);
    }

    /**
     * 获取用户是否在线
     *
     * @param userId
     * @return
     */
    public boolean getOnlineStatus(String userId) {
        try {
            if (!IS_STORE) {
                return ChatKit.isOnline(userId);
            }
            String keyPattern = USER + SUFFIX + userId + SUFFIX + TERMINAL;
            Set<String> terminalKeys = RedisTemplateUtils.getRedisTemplate().keys(keyPattern);
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
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        if (!IS_STORE) {
            return;
        }
        if (chatBody != null) {
            RedisCacheManager.getCache(timelineTable).sortSetPush(timelineId, chatBody.getCreateTime(), chatBody);
        }
    }

    /**
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    public void saveNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        if (!IS_STORE) {
            return;
        }
        if (chatBody != null) {
            RedisCacheManager.getCache(timelineTable).sortSetPush(timelineId, chatBody.getCreateTime(), chatBody);
        }
    }

    /**
     * @param timelineTable
     * @param timelineId
     */
    public void removeNoReadMessage(String timelineTable, String timelineId) {
        if (!IS_STORE) {
            return;
        }
        RedisCacheManager.getCache(timelineTable).remove(timelineId);
    }

    /**
     * @param timelineTable
     * @param timelineId
     */
    public void removeNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        if (!IS_STORE) {
            return;
        }
        RedisCacheManager.getCache(timelineTable).sortRemove(timelineId, JsonKit.toJSONString(chatBody));
    }

    /**
     * @param userId
     * @param fromUserId
     * @return
     */
    public List<ChatBody> getFriendsOfflineMessage(String userId, String fromUserId) {
        String userFriendKey = USER + SUFFIX + userId + SUFFIX + fromUserId;
        List<String> messageList = RedisCacheManager.getCache(PUSH).sortSetGetAll(userFriendKey);
        return JsonKit.toArray(messageList, ChatBody.class);
    }

    /**
     * @param userId
     * @param fromUserId
     * @return
     */
    public List<ChatBody> getFriendsOfflineMessage(String userId, String fromUserId, double min, double max) {
        String userFriendKey = USER + SUFFIX + userId + SUFFIX + fromUserId;
        List<String> messageList = RedisCacheManager.getCache(PUSH).sortSetGetAll(userFriendKey, min, max);
        return JsonKit.toArray(messageList, ChatBody.class);
    }

    /**
     * @param userId
     * @param fromUserId
     * @return
     */
    public List<ChatBody> getFriendsOfflineMessageOfLastMsgId(String userId, String fromUserId, Double endTime) {
        String userFriendKey = USER + SUFFIX + userId + SUFFIX + fromUserId;
        Long createTime = RedisCacheManager.getCache(PUSH_LAST_ID).get(userFriendKey, Long.class);
        //
        // String sessionId = ChatKit.sessionId(userId, fromUserId);
        // String userSessionKey = USER + SUFFIX + sessionId;
        List<String> messageList = RedisCacheManager.getCache(STORE).sortSetGetAll(userFriendKey,
            createTime == null ? 0 : createTime + 1, endTime == null ? System.currentTimeMillis() : endTime);
        return JsonKit.toArray(messageList, ChatBody.class);
    }

    /**
     * @param userId
     * @return
     */
    public UserMessageData getFriendsOfflineMessage(String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            // 获取所有好友消息key
            String likeKey = PUSH + SUFFIX + USER + SUFFIX + userId;
            Set<String> userKeys = RedisTemplateUtils.getRedisTemplate().keys(likeKey);
            // 获取好友离线消息;
            if (CollectionUtils.isNotEmpty(userKeys)) {
                List<ChatBody> messageList = new ArrayList<>();
                for (String userKey : userKeys) {
                    userKey = userKey.substring(userKey.indexOf(USER + SUFFIX));
                    List<String> messages = RedisCacheManager.getCache(PUSH).sortSetGetAll(userKey);
                    if (CollectionUtils.isNotEmpty(messages)) {
                        messageList.addAll(JsonKit.toArray(messages, ChatBody.class));
                    }
                }
                putFriendsMessage(messageData, messageList, null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return messageData;
    }

    /**
     * @param userId
     * @return
     */
    public UserMessageData getFriendsOfflineMessageOfLastMsgId(String userId, Double endTime) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            // 获取所有好友消息key
            String likeKey = PUSH_LAST_ID + SUFFIX + USER + SUFFIX + userId;
            Set<String> userKeys = RedisTemplateUtils.getRedisTemplate().keys(likeKey);
            // 获取好友离线消息;
            if (CollectionUtils.isNotEmpty(userKeys)) {
                List<ChatBody> messageList = new ArrayList<>();
                for (String userKey : userKeys) {
                    String fromUserId = userKey.substring(userKey.lastIndexOf(SUFFIX) + 1);
                    List<ChatBody> messages = getFriendsOfflineMessageOfLastMsgId(userId, fromUserId, endTime);
                    if (CollectionUtils.isNotEmpty(messages)) {
                        messageList.addAll(messages);
                    }
                }
                putFriendsMessage(messageData, messageList, null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return messageData;
    }

    /**
     * @param userId
     * @return
     */
    public UserMessageData getFriendsOfflineMessageOfLastMsgId(String userId) {
        return getFriendsOfflineMessageOfLastMsgId(userId, (double)System.currentTimeMillis());
    }

    /**
     * 获取用户 群离线消息
     *
     * @param userId
     * @return
     */
    public UserMessageData getGroupsOfflineMessage(String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        // 获取该用户所有组ID
        List<String> groupIdList = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
        // 获取群组离线消息;
        if (CollectionUtils.isNotEmpty(groupIdList)) {
            groupIdList.forEach(groupId -> {
                UserMessageData groupMessageData = getGroupOfflineMessage(userId, groupId);
                if (Objects.isNull(groupMessageData)) {
                    return;
                }
                putGroupMessage(messageData, groupMessageData.getGroups().get(groupId));
            });
        }
        return messageData;
    }

    public UserMessageData getGroupsOfflineMessageOfLastMsgId(String userId, Double endTime) {
        UserMessageData messageData = new UserMessageData(userId);
        // 获取该用户所有组ID
        List<String> groupIdList = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
        // 获取群组离线消息;
        if (CollectionUtils.isNotEmpty(groupIdList)) {
            groupIdList.forEach(groupId -> {
                UserMessageData groupMessageData = getGroupOfflineMessageOfLastMsgId(userId, groupId, endTime);
                if (Objects.isNull(groupMessageData)) {
                    return;
                }
                putGroupMessage(messageData, groupMessageData.getGroups().get(groupId));
            });
        }
        return messageData;
    }

    /**
     * 获取用户 群离线消息
     *
     * @param userId
     * @return
     */
    public UserMessageData getGroupsOfflineMessageOfLastMsgId(String userId) {
        return getGroupsOfflineMessageOfLastMsgId(userId, (double)System.currentTimeMillis());
    }

    /**
     * @param userId
     * @param groupId
     * @return
     */
    public List<ChatBody> getGroupOfflineChatBodyMessage(String userId, String groupId) {
        List<String> messages =
            RedisCacheManager.getCache(PUSH).sortSetGetAll(GROUP + SUFFIX + groupId + SUFFIX + userId);
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        return JsonKit.toArray(messages, ChatBody.class);
    }

    /**
     * @param userId
     * @param groupId
     * @return
     */
    public List<ChatBody> getGroupOfflineChatBodyMessage(String userId, String groupId, double min, double max) {
        List<String> messages =
            RedisCacheManager.getCache(PUSH).sortSetGetAll(GROUP + SUFFIX + groupId + SUFFIX + userId, min, max);
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        return JsonKit.toArray(messages, ChatBody.class);
    }

    /**
     * @param userId
     * @param groupId
     * @return
     */
    public UserMessageData getGroupOfflineMessage(String userId, String groupId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            List<String> messages =
                RedisCacheManager.getCache(PUSH).sortSetGetAll(GROUP + SUFFIX + groupId + SUFFIX + userId);
            if (CollectionUtils.isEmpty(messages)) {
                return messageData;
            }
            putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
            return messageData;
        } catch (Exception e) {
            e.printStackTrace();
            return messageData;
        }
    }

    /**
     * @param userId
     * @param groupId
     * @return
     */
    public UserMessageData getGroupOfflineMessageOfLastMsgId(String userId, String groupId, Double endTime) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            String key = GROUP + SUFFIX + groupId + SUFFIX + userId;
            Long createTime = RedisCacheManager.getCache(PUSH_LAST_ID).get(key, Long.class);
            List<String> messages = RedisCacheManager.getCache(STORE).sortSetGetAll(GROUP + SUFFIX + groupId,
                createTime == null ? 0 : createTime + 1, endTime == null ? System.currentTimeMillis() : endTime);
            if (CollectionUtils.isEmpty(messages)) {
                return messageData;
            }
            putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
            return messageData;
        } catch (Exception e) {
            e.printStackTrace();
            return messageData;
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
                groupMessages = new ArrayList<>();
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
                friendMessages = new ArrayList<>();
                userMessage.getFriends().put(targetFriendId, friendMessages);
            }
            friendMessages.add(chatBody);
        });
        return userMessage;
    }

    public UserMessageData getFriendHistoryMessage(String userId, String fromUserId, Double beginTime, Double endTime,
        Integer offset, Integer count) {
        String sessionId = ChatKit.sessionId(userId, fromUserId);
        String userSessionKey = USER + SUFFIX + sessionId;
        List<String> messages = getHistoryMessage(userSessionKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putFriendsMessage(messageData, JsonKit.toArray(messages, ChatBody.class), fromUserId);
        return messageData;
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
     * @param userId
     * @param groupId
     * @param beginTime
     * @param endTime
     * @param offset
     * @param count
     * @return
     */
    public UserMessageData getGroupHistoryMessage(String userId, String groupId, Double beginTime, Double endTime,
        Integer offset, Integer count) {
        String groupKey = GROUP + SUFFIX + groupId;
        List<String> messages = getHistoryMessage(groupKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        return messageData;
    }

    /**
     * @param user
     */
    public void updateUserTerminal(User user) {
        String userId = user.getUserId();
        String terminal = user.getTerminal();
        String status = user.getStatus();
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(terminal) || StringUtils.isEmpty(status)) {
            logger.error("userId:{},terminal:{},status:{} must not null", userId, terminal, status);
            return;
        }
        RedisCacheManager.getCache(USER).put(userId + SUFFIX + TERMINAL + SUFFIX + terminal, user.getStatus());
    }

    /**
     * @param userId
     * @return
     */
    public List<MsgNoticeRespBody> getAllMsgNotice(String userId) {
        List<MsgNoticeRespBody> msgList = new ArrayList<>();
        String userApplyKey = userId + SUFFIX + APPLY;
        List<String> list = RedisCacheManager.getCache(USER).listGetAll(userApplyKey);

        if (list != null && list.size() > 0) {
            for (String msgId : list) {
                MsgNoticeRespBody msg = RedisCacheManager.getCache(USER)
                    .get(MSG + SUFFIX + INFO + SUFFIX + userId + SUFFIX + msgId, MsgNoticeRespBody.class);
                if (msg != null) {
                    msgList.add(msg);
                }
            }
        }
        return msgList;
    }

    /**
     * @param noticeMsgId
     * @param noticeUserId
     */
    public void deleteMsgNotice(String noticeMsgId, String noticeUserId) {
        //
        String userApplyKey = noticeUserId + SUFFIX + APPLY;
        RedisCacheManager.getCache(USER).listRemove(userApplyKey, noticeMsgId);
        String key = MSG + SUFFIX + INFO + SUFFIX + noticeUserId + SUFFIX + noticeMsgId;
        RedisCacheManager.getCache(USER).remove(key);
    }

    /**
     * @param msgNoticeRespBody
     * @return
     */
    public MsgNoticeRespBody addMsgNotice(MsgNoticeRespBody msgNoticeRespBody) {
        String noticeMsgId = msgNoticeRespBody.getMsgId();
        String noticeUserId = msgNoticeRespBody.getNoticeUserId();

        String userApplyKey = noticeUserId + SUFFIX + APPLY;
        RedisCacheManager.getCache(USER).listPushTail(userApplyKey, noticeMsgId);
        RedisCacheManager.getCache(USER).put(MSG + SUFFIX + INFO + SUFFIX + noticeUserId + SUFFIX + noticeMsgId,
            msgNoticeRespBody);
        return msgNoticeRespBody;
    }

    /**
     * @param topicName
     * @return
     */
    public boolean addTopicName(String topicName) {
        String cacheKey = SYSTEM + SUFFIX + TOPIC;
        List<String> list = RedisCacheManager.getCache(TOPIC_NAME).listGetAll(cacheKey);
        if (CollectionUtils.isEmpty(list)) {
            RedisCacheManager.getCache(TOPIC_NAME).listPushTail(cacheKey, topicName);
            return true;
        }
        if (list.contains(topicName)) {
            return false;
        }
        RedisCacheManager.getCache(TOPIC_NAME).listPushTail(cacheKey, topicName);
        return true;
    }

    /**
     * @return
     */
    public List<String> findAllTopicName() {
        String cacheKey = SYSTEM + SUFFIX + TOPIC;
        List<String> list = RedisCacheManager.getCache(TOPIC_NAME).listGetAll(cacheKey);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list.parallelStream().distinct().collect(Collectors.toList());
    }

    /**
     * @param userId
     * @param loginUser
     */
    public void addUserAuth(String timelineTable, String userId, LoginUser loginUser) {
        RedisCacheManager.getCache(timelineTable).put(userId, loginUser);
    }

    /**
     * @param userId
     */
    public LoginUser getUserAuth(String timelineTable, String userId) {
        return RedisCacheManager.getCache(timelineTable).get(userId, LoginUser.class);
    }

    static {
        logger.info("正在初始化 RedisCacheManager.....");
        RedisCacheManager.register(USER, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(GROUP, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PUSH_ACK_FAIL, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(PUSH_LAST_ID, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(USER_TOKEN, CACHE_TIME_OUT_7, CACHE_TIME_OUT_7);
        RedisCacheManager.register(USER_VERSION_INFO, CACHE_TIME_OUT_10, CACHE_TIME_OUT_10);
        RedisCacheManager.register(TOPIC_NAME, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
