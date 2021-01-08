package com.daren.chen.im.server.helper.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.cache.redis.RedisCache;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.AbstractImStoreBindListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.config.ImServerConfig;

/**
 * 消息持久化绑定监听器
 *
 * @author WChao
 * @date 2018年4月8日 下午4:12:31
 */
public class RedisImStoreBindListener extends AbstractImStoreBindListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisImStoreBindListener.class);

    private static final String SUFFIX = ":";

    public RedisImStoreBindListener(ImConfig imConfig, MessageHelper messageHelper) {
        super(imConfig, messageHelper);
    }

    @Override
    public void onAfterGroupBind(ImChannelContext imChannelContext, Group group) throws ImException {
        if (!isStore()) {
            return;
        }
        initGroupUsers(group, imChannelContext);
    }

    @Override
    public void onAfterGroupUnbind(ImChannelContext imChannelContext, Group group) throws ImException {
        if (!isStore()) {
            return;
        }
        String userId = imChannelContext.getUserId();
        String groupId = group.getGroupId();
        // 移除群组成员;
        RedisCacheManager.getCache(GROUP).listRemove(groupId + SUFFIX + USER, userId);
        // 移除成员群组;
        RedisCacheManager.getCache(USER).listRemove(userId + SUFFIX + GROUP, groupId);
        // // 移除群组离线消息
        // RedisCacheManager.getCache(PUSH).remove(GROUP + SUFFIX + group + SUFFIX + userId);
    }

    @Override
    public void onAfterUserBind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.ONLINE.getStatus());
        this.messageHelper.updateUserTerminal(imChannelContext.getUserId(), user);
        initUserInfo(user);
    }

    @Override
    public void onAfterUserUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.OFFLINE.getStatus());
        this.messageHelper.updateUserTerminal(imChannelContext.getUserId(), user);
    }

    @Override
    public void onAfterAddOrRemoveUser(String operateUserId, String userId) throws ImException {
        if (!isStore() || Objects.isNull(userId)) {
            return;
        }
        User user = User.newBuilder().userId(userId).build();
        initUserInfo(user);
    }

    @Override
    public void onAfterAddGroup(String operateUserId, String userId, String groupId) throws ImException {

    }

    @Override
    public void onAfterRemoveGroup(String operateUserId, String userId, String groupId) throws ImException {

    }

    @Override
    public void onAfterDissolveGroup(String operateUserId, String groupId) throws ImException {

    }

    /**
     * 初始化群组用户;
     *
     * @param group
     * @param imChannelContext
     */
    public void initGroupUsers(Group group, ImChannelContext imChannelContext) {
        String groupId = group.getGroupId();
        if (!isStore()) {
            return;
        }
        String userId = imChannelContext.getUserId();
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
            return;
        }
        String groupUserKey = groupId + SUFFIX + USER;
        RedisCache groupCache = RedisCacheManager.getCache(GROUP);
        List<String> users = groupCache.listGetAll(groupUserKey);
        if (!users.contains(userId)) {
            groupCache.listPushTail(groupUserKey, userId);
        }
        initUserGroups(userId, groupId);
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        User onlineUser = imSessionContext.getImClientNode().getUser();
        if (onlineUser == null) {
            return;
        }
        List<Group> groups = onlineUser.getGroups();
        if (groups == null) {
            return;
        }
        for (Group storeGroup : groups) {
            if (!groupId.equals(storeGroup.getGroupId())) {
                continue;
            }
            groupCache.put(groupId + SUFFIX + INFO, storeGroup);
            break;
        }
    }

    /**
     * 初始化用户拥有哪些群组;
     *
     * @param userId
     * @param group
     */
    public void initUserGroups(String userId, String group) {
        if (!isStore()) {
            return;
        }
        if (StringUtils.isEmpty(group) || StringUtils.isEmpty(userId)) {
            return;
        }
        List<String> groups = RedisCacheManager.getCache(USER).listGetAll(userId + SUFFIX + GROUP);
        if (groups != null && groups.contains(group)) {
            return;
        }
        RedisCacheManager.getCache(USER).listPushTail(userId + SUFFIX + GROUP, group);
    }

    /**
     * 初始化用户终端协议类型;
     *
     * @param user
     */
    public void initUserInfo(User user) {
        if (!isStore() || user == null) {
            return;
        }
        String userId = user.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        RedisCache userCache = RedisCacheManager.getCache(USER);
        userCache.put(userId + SUFFIX + INFO, user.clone());
        List<User> friends = user.getFriends();
        if (CollectionUtils.isEmpty(friends)) {
            return;
        }
        userCache.put(userId + SUFFIX + FRIENDS, (Serializable)friends);
    }

    /**
     * 是否开启持久化;
     *
     * @return
     */
    public boolean isStore() {
        ImServerConfig imServerConfig = ImServerConfig.Global.get();
        return ImServerConfig.ON.equals(imServerConfig.getIsStore());
    }

    static {
        RedisCacheManager.register(USER, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(GROUP, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(STORE, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
        RedisCacheManager.register(PUSH, CACHE_TIME_OUT_100, CACHE_TIME_OUT_100);
    }

}
