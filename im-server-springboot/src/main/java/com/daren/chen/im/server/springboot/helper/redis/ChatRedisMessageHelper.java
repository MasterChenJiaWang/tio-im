package com.daren.chen.im.server.springboot.helper.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.message.AbstractMessageHelper;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
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
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.helper.redis.RedisMessageHelper;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.springboot.utils.ApplicationContextProvider;

import cn.hutool.core.collection.CollectionUtil;

/**
 * Redis获取持久化+同步消息助手;
 *
 * @author chendaren
 * @date 2020年10月20日10:00:45
 */
public class ChatRedisMessageHelper extends AbstractMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageHelper.class);

    private static final String SUFFIX = ":";

    private static boolean isSqlSave = false;

    public ChatRedisMessageHelper() {
        this.imConfig = ImConfig.Global.get();
        isSqlSave = imConfig.getApiServerConfig().isEnabled();
    }

    private ChatCommonMethodUtils getChatCommonMethodUtils() {
        return ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
    }

    @Override
    public ImStoreBindListener getBindListener() {
        return new ChatRedisImStoreBindListener(imConfig, this);
    }

    /**
     * 判断用户是否在线
     *
     * @param userId
     * @return
     */
    @Override
    public boolean isOnline(String userId) {
        return LocalCacheUtils.me().getOnlineStatus(userId);

    }

    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        LocalCacheUtils.me().writeMessage(timelineTable, timelineId, chatBody);
    }

    /**
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    @Override
    @Deprecated
    public void saveNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        if (chatBody == null) {
            return;
        }
        // 从 没有删除成功的ack_id 如果存在 就不保存
        String msgId = RedisCacheManager.getCache(PUSH_ACK_FAIL).get(chatBody.getId(), String.class);
        if (StringUtils.isNotBlank(msgId)) {
            RedisCacheManager.getCache(PUSH_ACK_FAIL).remove(msgId);
            return;
        }
        LocalCacheUtils.me().saveNoReadMessage(timelineTable, timelineId, chatBody);
    }

    /**
     * @param timelineTable
     * @param timelineId
     */
    @Override
    @Deprecated
    public void removeNoReadMessage(String timelineTable, String timelineId) {
        LocalCacheUtils.me().removeNoReadMessage(timelineTable, timelineId);
    }

    @Override
    public void writeMessageOfSql(String operateUserId, ChatBody chatBody) {
        try {
            //
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils = getChatCommonMethodUtils();
                chatCommonMethodUtils.writeMessage(operateUserId, chatBody);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取组的所有用户
     *
     * @param groupId
     * @return
     */
    @Override
    public List<String> getGroupUsers(String operateUserId, String groupId) {
        List<String> userList = LocalCacheUtils.me().getGroupUserIdsByCache(groupId);
        if (userList == null || userList.size() == 0) {
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils = getChatCommonMethodUtils();
                userList = chatCommonMethodUtils.getGroupUsers(operateUserId, groupId);
            }
            if (userList != null && userList.size() > 0) {
                //
                LocalCacheUtils.me().saveGroupUserIdsByCache(groupId, userList);
            }
        }
        return userList;
    }

    /**
     * 往群组添加用户
     *
     * @param userId
     * @param groupId
     */
    @Override
    public void addGroupUser(String operateUserId, String userId, String groupId) {
        //
        List<String> users = LocalCacheUtils.me().getGroupUserIdsByCache(groupId);
        if (CollectionUtils.isNotEmpty(users)) {
            if (users.contains(userId)) {
                return;
            }
        }
        LocalCacheUtils.me().saveGroupUserIdsByCache(groupId, userId);
    }

    /**
     * 往群组里面删除用户
     *
     * @param userId
     * @param groupId
     */
    @Override
    public void removeGroupUser(String operateUserId, String userId, String groupId) {
        //
        LocalCacheUtils.me().removeGroupUserIdByCache(groupId, userId);
    }

    /**
     * 获取好友离线消息
     *
     * @param userId
     *            用户ID
     * @param fromUserId
     *            目标用户ID
     * @return
     */
    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId, String fromUserId) {
        // List<ChatBody> messageDataList = LocalCacheUtils.me().getFriendsOfflineMessage(userId, fromUserId);
        List<ChatBody> messageDataList =
            LocalCacheUtils.me().getFriendsOfflineMessageOfLastMsgId(userId, fromUserId, null);
        return LocalCacheUtils.me().putFriendsMessage(new UserMessageData(userId), messageDataList, null);
    }

    /**
     * @param userId
     * @return
     */
    private UserMessageData getFriendsOfflineMessageFromCache(String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            // 获取所有好友消息key
            UserMessageData friendsOfflineMessage = LocalCacheUtils.me().getFriendsOfflineMessage(userId);
            if (friendsOfflineMessage != null) {
                messageData.setFriends(friendsOfflineMessage.getFriends());
            }
            // 获取该用户所有组ID
            UserMessageData userMessageData = LocalCacheUtils.me().getGroupsOfflineMessage(userId);
            if (userMessageData != null) {
                messageData.setGroups(userMessageData.getGroups());
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return messageData;
    }

    /**
     * @param userId
     * @return
     */
    private UserMessageData getFriendsOfflineMessageFromCacheOfLastMsgId(String userId, Double endTime) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            // 获取所有好友消息key
            UserMessageData friendsOfflineMessage =
                LocalCacheUtils.me().getFriendsOfflineMessageOfLastMsgId(userId, endTime);
            if (friendsOfflineMessage != null) {
                messageData.setFriends(friendsOfflineMessage.getFriends());
            }
            // 获取该用户所有组ID
            UserMessageData userMessageData = LocalCacheUtils.me().getGroupsOfflineMessageOfLastMsgId(userId);
            if (userMessageData != null) {
                messageData.setGroups(userMessageData.getGroups());
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return messageData;
    }

    /**
     * 获取用户的离线消息
     *
     * @param userId
     * @return
     */
    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId) {
        // 先查redis缓存
        UserMessageData userMessageData = getFriendsOfflineMessageFromCache(userId);
        if ((userMessageData.getGroups() != null && !userMessageData.getGroups().isEmpty())
            || (userMessageData.getFriends() != null && !userMessageData.getFriends().isEmpty())) {
            return userMessageData;
        }
        // 从数据库查询
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            return chatCommonMethodUtils.getOfflineMessage(operateUserId, userId);
        }
        return userMessageData;
    }

    @Override
    public UserMessageData getFriendsOfflineMessageOfLastsgId(String operateUserId, String userId, Double endTime) {
        // 先查redis缓存
        UserMessageData userMessageData = getFriendsOfflineMessageFromCacheOfLastMsgId(userId, endTime);
        if ((userMessageData.getGroups() != null && !userMessageData.getGroups().isEmpty())
            || (userMessageData.getFriends() != null && !userMessageData.getFriends().isEmpty())) {
            return userMessageData;
        }
        // 从数据库查询
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            return chatCommonMethodUtils.getOfflineMessage(operateUserId, userId);
        }
        return userMessageData;
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
        return LocalCacheUtils.me().getGroupOfflineMessageOfLastMsgId(userId, groupId, null);
        // 查数据库
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
        return LocalCacheUtils.me().getFriendHistoryMessage(userId, fromUserId, beginTime, endTime, offset, count);
        // 查数据库
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
        return LocalCacheUtils.me().getGroupHistoryMessage(userId, groupId, beginTime, endTime, offset, count);
        // 查数据库
    }

    /**
     * 修改最新消息
     *
     * @param chatBody
     * @return
     */
    @Override
    public boolean updateLastMessageId(String operateUserId, String userId, ChatAckBody chatBody) {
        if (chatBody == null) {
            return false;
        }
        String id = chatBody.getId();
        String from = chatBody.getFrom();
        String to = chatBody.getTo();
        String groupId = chatBody.getGroupId();
        String groupMemberId = "";
        // 群消息
        if (StringUtils.isNotBlank(groupId)) {
            groupMemberId = userId;
        }
        //
        updateLastMsgId(userId, chatBody);
        // updateAndRemoveOffLineMessage(userId, chatBody);
        // 修改数据库
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            return chatCommonMethodUtils.updateLastMsgId(operateUserId, from, to, groupId, groupMemberId, id);
        }
        return true;
    }

    /**
     * 修改最后id 存储消息时间
     *
     * @param userId
     * @param chatBody
     * @return
     */
    private boolean updateLastMsgId(String userId, ChatAckBody chatBody) {
        String groupId = chatBody.getGroupId();
        // 群消息
        String key = "";
        if (StringUtils.isNotBlank(groupId)) {
            key = GROUP + SUFFIX + groupId + SUFFIX + userId;
        } else {
            key = USER + ":" + userId + ":" + chatBody.getFrom();
        }
        Long createTime = RedisCacheManager.getCache(PUSH_LAST_ID).get(key, Long.class);
        if (createTime == null || createTime < chatBody.getCreateTime()) {
            RedisCacheManager.getCache(PUSH_LAST_ID).put(key, chatBody.getCreateTime());
            return true;
        }
        return false;
    }

    /**
     * @param userId
     * @param chatBody
     */
    @Deprecated
    private boolean updateAndRemoveOffLineMessage(String userId, ChatAckBody chatBody) {
        if (chatBody == null) {
            return false;
        }
        // // 群消息
        List<ChatBody> messageDataList = getHistoryChatBody(userId, chatBody);
        //
        return resendOfflineMessage(userId, chatBody, messageDataList);
    }

    private List<ChatBody> getHistoryChatBody(String userId, ChatAckBody chatBody) {
        String from = chatBody.getFrom();
        Long createTime = chatBody.getCreateTime();
        String groupId = chatBody.getGroupId();
        List<ChatBody> messageDataList = null;
        // 群消息
        if (StringUtils.isNotBlank(groupId)) {
            if (createTime == null) {
                messageDataList = LocalCacheUtils.me().getGroupOfflineChatBodyMessage(userId, groupId);
            } else {
                messageDataList = LocalCacheUtils.me().getGroupOfflineChatBodyMessage(userId, groupId, 0, createTime);
            }
        } else {
            if (createTime == null) {
                messageDataList = LocalCacheUtils.me().getFriendsOfflineMessage(userId, from);
            } else {
                messageDataList = LocalCacheUtils.me().getFriendsOfflineMessage(userId, from, 0, createTime);
            }
        }
        return messageDataList;
    }

    /**
     *
     */
    private boolean resendOfflineMessage(String userId, ChatAckBody chatBody, List<ChatBody> messageDataList) {
        try {

            String from = chatBody.getFrom();
            String groupId = chatBody.getGroupId();
            List<String> msgIds = chatBody.getMsgIds();
            if (CollectionUtils.isEmpty(msgIds)) {
                return false;
            }
            long l1 = System.currentTimeMillis();
            List<ChatBody> oldOfflineMesage = new ArrayList<>();
            boolean b = checkCountAndClear(messageDataList, msgIds, oldOfflineMesage);
            if (!b) {
                // 清空 重新查询
                messageDataList = null;
                oldOfflineMesage.clear();
                log.warn("线程[{}] 清空", Thread.currentThread().getName());
            }
            // 总共等10s
            int n = 100;
            while (n > 0 && ((messageDataList == null || messageDataList.size() == 0))) {
                n--;
                // 有时消息ack 会在消息保存之前
                // 等 消息保存后 在 查询出来 暂停时间根据消息保存耗时 确定
                Thread.sleep(100);
                messageDataList = getHistoryChatBody(userId, chatBody);
                b = checkCountAndClear(messageDataList, msgIds, oldOfflineMesage);
                if (!b) {
                    // 清空 重新查询
                    messageDataList = null;
                    oldOfflineMesage.clear();
                }
            }
            log.warn("删除离线消息查询耗时 线程[{}]  耗时 [{}]", Thread.currentThread().getName(), System.currentTimeMillis() - l1);
            if (CollectionUtils.isEmpty(messageDataList)) {
                log.warn("删除离线消息-失败 线程[{}]  ", Thread.currentThread().getName());
                // 保存 没有删成功的ack id
                for (String msgId : msgIds) {
                    RedisCacheManager.getCache(PUSH_ACK_FAIL).put(msgId, msgId);
                }
                return false;
            }
            // 群消息
            if (StringUtils.isNotBlank(groupId)) {
                String cacheKey = GROUP + SUFFIX + groupId + SUFFIX + userId;
                // 先删已读信息
                if (CollectionUtils.isNotEmpty(oldOfflineMesage)) {
                    for (ChatBody value : oldOfflineMesage) {
                        LocalCacheUtils.me().removeNoReadMessage(PUSH, cacheKey, value);
                    }
                }
            } else {
                String cacheKey = USER + ":" + userId + ":" + from;
                // 先删已读信息
                if (CollectionUtils.isNotEmpty(oldOfflineMesage)) {
                    for (ChatBody value : oldOfflineMesage) {
                        LocalCacheUtils.me().removeNoReadMessage(PUSH, cacheKey, value);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    private boolean checkCountAndClear(List<ChatBody> messageDataList, List<String> msgIds,
        List<ChatBody> oldOfflineMesage) {
        if (oldOfflineMesage == null) {
            oldOfflineMesage = new ArrayList<>(msgIds.size());
        }
        if (CollectionUtils.isNotEmpty(messageDataList)) {
            for (ChatBody body : messageDataList) {
                if (msgIds.contains(body.getId())) {
                    oldOfflineMesage.add(body);
                }
            }
            // 数量不对
            // 清空 重新查询
            return oldOfflineMesage.size() == msgIds.size();
        }
        return false;
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
        //
        Group group = LocalCacheUtils.me().getGroupInfoByCache(groupId);
        if (group == null || group.getUsers() == null) {
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                group = chatCommonMethodUtils.getGroupById(operateUserId, groupId);
            }
            if (group != null) {
                LocalCacheUtils.me().saveGroupInfoByCache(groupId, group);
                List<User> users = group.getUsers();
                if (CollectionUtils.isNotEmpty(users)) {
                    List<String> collect = users.stream().filter(user -> StringUtils.isNotBlank(user.getUserId()))
                        .map(User::getUserId).collect(Collectors.toList());
                    //
                    LocalCacheUtils.me().saveGroupUserIdsByCache(groupId, collect);
                }
            }
        }
        if (Objects.isNull(group)) {
            return null;
        }
        List<User> usersList = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<String> groupUserIds = LocalCacheUtils.me().getGroupUserIdsByCache(groupId);
        if (CollectionUtils.isNotEmpty(groupUserIds)) {
            for (String userId : groupUserIds) {
                User user = LocalCacheUtils.me().getUserInfoByCache(userId);
                if (user != null) {
                    usersList.add(user);
                }
            }
        }
        //
        if (usersList.size() > 0) {
            for (User user : usersList) {
                validateStatusByType(type, users, user);
            }
        }
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

    /**
     * 获取用户基础信息
     *
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public User getUserByType(String operateUserId, String userId, Integer type) {
        //
        User user = LocalCacheUtils.me().getUserInfoByCache(userId);
        if (Objects.isNull(user)) {
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                user = chatCommonMethodUtils.getUserById(operateUserId, userId);
            }
            if (user != null) {
                LocalCacheUtils.me().saveUserInfoByCache(userId, user);
            } else {
                return null;
            }
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
    @Override
    public List<User> getAllFriendUsers(String operateUserId, String userId, Integer type) {
        if (Objects.isNull(userId)) {
            return null;
        }
        //
        List<User> friendsResult = new ArrayList<>();
        List<String> friendIds = LocalCacheUtils.me().getUserFriendIdsByCache(userId);
        if (CollectionUtils.isEmpty(friendIds)) {
            List<User> friends = new ArrayList<>();
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                friends = refreshFriends(operateUserId, userId, chatCommonMethodUtils);
            }

            if (friends != null) {
                for (User user : friends) {
                    initUserStatus(user);
                    validateStatusByType(type, friendsResult, user);
                }
            }
        } else {
            for (String friendId : friendIds) {
                User user = User.newBuilder().id(friendId).userId(friendId).build();
                initUserStatus(user);
                validateStatusByType(type, friendsResult, user);
            }
        }
        return friendsResult;

    }

    @Override
    public List<String> getAllFriendUserIds(String operateUserId, String userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<String> friendIds = LocalCacheUtils.me().getUserFriendIdsByCache(userId);
        if (CollectionUtils.isEmpty(friendIds)) {
            friendIds = new ArrayList<>();
            List<User> friends = new ArrayList<>();
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                friends = refreshFriends(operateUserId, userId, chatCommonMethodUtils);
            }

            if (friends != null) {
                for (User user : friends) {
                    friendIds.add(user.getUserId());
                }
            }
        }
        return friendIds;
    }

    /**
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public List<Group> getAllGroupUsers(String operateUserId, String userId, Integer type) {
        if (Objects.isNull(userId)) {
            return new ArrayList<>();
        }
        List<String> groupIds = LocalCacheUtils.me().getUserGroupIdsByCache(userId);
        if (CollectionUtil.isEmpty(groupIds)) {
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                groupIds = chatCommonMethodUtils.getAllGroupUsers(operateUserId, userId);
            }
            LocalCacheUtils.me().saveUserGroupIdsByCache(userId, groupIds);
        }
        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        List<Group> groups = new ArrayList<>();
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
        //
        LocalCacheUtils.me().updateUserTerminal(user);
        initUserStatus(user);
        return true;
    }

    /**
     * 添加好友申请
     *
     * @param curUserId
     * @param friendUserId
     * @param content
     * @return
     */
    @Override
    public boolean addFriendApply(String operateUserId, String curUserId, String friendUserId, String content) {
        return false;
    }

    @Override
    public String getUserOnlineStatus(String operateUserId, String userId) {
        boolean isOnline = this.isOnline(userId);
        if (isOnline) {
            return UserStatusType.ONLINE.getStatus();
        } else {
            return UserStatusType.OFFLINE.getStatus();
        }
    }

    /**
     * 离线消息，写缓存
     *
     * @param msgNoticeReq
     * @return
     */
    @Override
    public MsgNoticeRespBody msgNoticeOffline(String operateUserId, MsgNoticeReq msgNoticeReq) {
        MsgNoticeRespBody msgNoticeRespBody = initMsgNoticeRespBodyOffLine(msgNoticeReq);
        initNoticeInfo(operateUserId, msgNoticeRespBody);
        if (msgNoticeRespBody.getGroupInfo() == null && msgNoticeReq.getGroupInfo() != null) {
            msgNoticeRespBody.setGroupInfo(msgNoticeReq.getGroupInfo());
        }
        return msgNoticeRespBody;
    }

    private void initNoticeInfo(String operateUserId, MsgNoticeRespBody msgNoticeRespBody) {
        if (StringUtils.isNotBlank(msgNoticeRespBody.getFromUserId())) {
            User user = getUserByType(operateUserId, msgNoticeRespBody.getFromUserId(), 2);
            msgNoticeRespBody.setFromUserInfo(user);
        }
        if (StringUtils.isNotBlank(msgNoticeRespBody.getNoticeUserId())) {
            User user = getUserByType(operateUserId, msgNoticeRespBody.getNoticeUserId(), 2);
            msgNoticeRespBody.setNoticeUserInfo(user);
        }

        if (StringUtils.isNotBlank(msgNoticeRespBody.getNoticeGroupId())) {
            Group group = getGroupUsers(operateUserId, msgNoticeRespBody.getNoticeGroupId(), 2);
            msgNoticeRespBody.setGroupInfo(group);
            if (group != null) {
                List<User> userList = group.getUsers();
                msgNoticeRespBody.setGroupUserInfoList(userList);
            }
        }

    }

    @Override
    public NoticeOfflineRespBody noticeOffLine(String operateUserId, NoticeOfflineReq noticeOfflineReq) {
        String userId = noticeOfflineReq.getUserId();
        User user = this.getUserByType(operateUserId, userId, 2);
        if (user != null) {
            user.setStatus(UserStatusType.OFFLINE.getStatus());
            this.updateUserTerminal(operateUserId, user);
            List<User> userList = this.getAllFriendUsers(operateUserId, userId, 0);
            List<ImChannelContext> channelContextList = JimServerAPI.getByUserId(userId);
            if (channelContextList != null && channelContextList.size() > 0) {
                for (ImChannelContext imChannelContext : channelContextList) {
                    if (imChannelContext == null) {
                        continue;
                    }
                    try {
                        if (userList != null && userList.size() > 0) {
                            notifyFriendsOffline(noticeOfflineReq.getUserId(), userList, imChannelContext);
                        }

                    } catch (ImException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return NoticeOfflineRespBody.success();
    }

    @Override
    public ReceiveMsgNoticeRespBody receiveMsgNotice(String operateUserId, String userId) {
        List<MsgNoticeRespBody> msgList = LocalCacheUtils.me().getAllMsgNotice(userId);
        ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody = new ReceiveMsgNoticeRespBody();
        receiveMsgNoticeRespBody.setList(msgList);
        return receiveMsgNoticeRespBody;
    }

    /**
     * 解算群
     *
     * @param msgNoticeReq
     * @return
     */
    @Override
    public Map<String, Object> dissolveGroup(String operateUserId, MsgNoticeReq msgNoticeReq) {
        Map<String, Object> result = new HashMap<>();

        String groupId = msgNoticeReq.getNoticeGroupId();
        List<String> userIds = this.getGroupUsers(operateUserId, groupId);
        boolean success = true;
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            Group groupInfo = chatCommonMethodUtils.getGroupById(operateUserId, groupId);
            result.put("user_ids", userIds);
            result.put("group_info", groupInfo);
            success = chatCommonMethodUtils.disbandGroup(operateUserId, msgNoticeReq.getCurUserId(),
                msgNoticeReq.getNoticeGroupId());
        }
        if (success) {
            LocalCacheUtils.me().removeGroupUserIdsByCache(groupId);
            return result;
        } else {
            return null;
        }

    }

    /**
     * @param userId
     * @param friendList
     * @param imChannelContext
     * @throws ImException
     */
    private void notifyFriendsOffline(String userId, List<User> friendList, ImChannelContext imChannelContext)
        throws ImException {

        GetUserOnlineStatusRespBody getUserOnlineStatusRespBody = new GetUserOnlineStatusRespBody();
        getUserOnlineStatusRespBody.setUserId(userId);
        getUserOnlineStatusRespBody.setStatus(UserStatusType.OFFLINE.getStatus());
        GetUserOnlineStatusRespBody getUserOnlineStatusRespBody1 =
            GetUserOnlineStatusRespBody.success(userId).setData(getUserOnlineStatusRespBody);
        ImPacket imPacket = ProtocolManager.Converter.respPacket(getUserOnlineStatusRespBody1, imChannelContext);
        if (friendList != null) {
            for (User user : friendList) {
                JimServerAPI.sendToUser(user.getUserId(), imPacket);
            }
        }

    }

    /**
     * 删除消息通知
     *
     * @param noticeMsgId
     * @param noticeUserId
     * @return
     */
    @Override
    public boolean deleteMsgNotice(String operateUserId, String noticeMsgId, String noticeUserId) {
        //
        LocalCacheUtils.me().deleteMsgNotice(noticeMsgId, noticeUserId);
        return true;
    }

    /**
     * 新增用户在线记录
     *
     * @param paramMap
     *            map参数: user_id:用户ID, online_status:在线状态（0：在线，1：离线） , phone_imei:手机imei, sys_version:系统版本,
     *            app_version:app版本, terminal_type:终端类型(0：andriod，1：IOS), context_id:上下文id, report_ime:上下线记录时间(年月日时分秒)
     * @return
     */
    @Override
    public boolean addUserOnlineStatusRecord(String operateUserId, Map<String, Object> paramMap) {
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            return chatCommonMethodUtils.addUserOnlineStatusRecord(operateUserId, paramMap);
        }
        return true;
    }

    /**
     * 添加redis 订阅主题
     *
     * @param topicName
     */
    @Override
    public boolean addTopicName(String topicName) {
        return LocalCacheUtils.me().addTopicName(topicName);
    }

    /**
     * 查询所有redis 订阅主题
     *
     * @return
     */
    @Override
    public List<String> findAllTopicName() {
        return LocalCacheUtils.me().findAllTopicName();
    }

    /**
     * 初始化通知消息
     *
     * @param msgNoticeReq
     * @return
     */
    private MsgNoticeRespBody initMsgNoticeRespBodyOffLine(MsgNoticeReq msgNoticeReq) {
        MsgNoticeRespBody msgNoticeRespBody = initMsgNoticeRespBodyData(msgNoticeReq);
        return LocalCacheUtils.me().addMsgNotice(msgNoticeRespBody);
    }

    private MsgNoticeRespBody initMsgNoticeRespBodyData(MsgNoticeReq msgNoticeReq) {
        MsgNoticeRespBody msgNoticeRespBody = new MsgNoticeRespBody();
        msgNoticeRespBody.setMsgType(msgNoticeReq.getMsgType());
        msgNoticeRespBody.setNoticeType(msgNoticeReq.getNoticeType());
        msgNoticeRespBody.setId(msgNoticeReq.getId());
        msgNoticeRespBody.setFromUserId(msgNoticeReq.getCurUserId());
        msgNoticeRespBody.setNoticeTime(msgNoticeReq.getNoticeTime());
        msgNoticeRespBody.setNoticeGroupId(msgNoticeReq.getNoticeGroupId());
        msgNoticeRespBody.setNoticeUserId(msgNoticeReq.getNoticeUserId());
        msgNoticeRespBody.setMsgId(msgNoticeReq.getMsgId());
        msgNoticeRespBody.setGroupInfo(msgNoticeReq.getGroupInfo());
        msgNoticeRespBody.setGroupUserIds(msgNoticeReq.getGroupUserIds());
        return msgNoticeRespBody;
    }

    /**
     * 刷新好友
     *
     * @param userId
     * @param chatCommonMethodUtils
     */
    private List<User> refreshFriends(String operateUserId, String userId,
        ChatCommonMethodUtils chatCommonMethodUtils) {
        if (!isSqlSave || chatCommonMethodUtils == null) {
            return null;
        }
        LocalCacheUtils.me().clearUserCache(userId);
        List<User> friends = chatCommonMethodUtils.initUserFrineds(operateUserId, userId);
        if (friends != null && friends.size() > 0) {
            List<String> friendIds = new ArrayList<>();
            for (User user : friends) {
                initUserStatus(user);
                String friendId = user.getUserId();
                if (StringUtils.isNotBlank(friendId)) {
                    friendIds.add(friendId);
                    LocalCacheUtils.me().saveUserInfoByCache(friendId, user);
                }
            }
            LocalCacheUtils.me().saveUserFriendIdsByCache(userId, friendIds);

        }
        return friends;
    }

    /**
     * 获取用户拥有的群组;
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> getGroups(String operateUserId, String userId) {
        List<String> groups = LocalCacheUtils.me().getUserGroupIdsByCache(userId);
        if (groups == null || groups.size() == 0) {
            if (isSqlSave) {
                ChatCommonMethodUtils chatCommonMethodUtils =
                    ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
                groups = chatCommonMethodUtils.getAllGroupUsers(operateUserId, userId);
            }
            LocalCacheUtils.me().saveUserGroupIdsByCache(userId, groups);
        }
        return groups == null ? new ArrayList<>() : groups;
    }

    /**
     * @param key
     * @param loginUser
     */
    @Override
    public void addUserLoginInfo(String timelineTable, String key, LoginUser loginUser) {
        LocalCacheUtils.me().addUserAuth(timelineTable, key, loginUser);
    }

    /**
     * @param key
     */
    @Override
    public LoginUser getUserLoginInfo(String timelineTable, String key) {
        return LocalCacheUtils.me().getUserAuth(timelineTable, key);
    }

    @Override
    public User getUserBaseInfoByToken(String token) {
        if (isSqlSave) {
            return ChatCommonMethodUtils.getUserBaseInfoByToken(token);
        } else {
            // TODO 测试用 给一个默认值
            return LocalCacheUtils.me().getUserInfoByCache(token);
        }
    }

    static {
        RedisCacheManager.register(USER, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(GROUP, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(USER_TOKEN, CACHE_TIME_OUT_7, CACHE_TIME_OUT_7);
        RedisCacheManager.register(USER_VERSION_INFO, CACHE_TIME_OUT_10, CACHE_TIME_OUT_10);
        RedisCacheManager.register(TOPIC_NAME, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}
