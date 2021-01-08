package com.daren.chen.im.server.springboot.helper.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.AbstractImStoreBindListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.springboot.utils.ApplicationContextProvider;

import cn.hutool.core.collection.CollectionUtil;

/**
 * 消息持久化绑定监听器
 *
 * @author WChao
 * @date 2018年4月8日 下午4:12:31
 */
public class ChatRedisImStoreBindListener extends AbstractImStoreBindListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatRedisImStoreBindListener.class);
    /**
     *
     */
    private static boolean isSqlSave = false;

    public ChatRedisImStoreBindListener(ImConfig imConfig, MessageHelper messageHelper) {
        super(imConfig, messageHelper);
        isSqlSave = this.imConfig.getApiServerConfig().isEnabled();
    }

    /**
     * 用户绑定群
     *
     * @param imChannelContext
     *            通道上下文
     * @param group
     *            绑定群组信息
     * @throws ImException
     */
    @Override
    public void onAfterGroupBind(ImChannelContext imChannelContext, Group group) throws ImException {
        // if (!isStore()) {
        // return;
        // }
        // initGroupUsers(group, imChannelContext);
    }

    /**
     * 用户解绑群
     *
     * @param imChannelContext
     *            通道上下文
     * @param group
     *            解绑群组信息
     * @throws ImException
     */
    @Override
    public void onAfterGroupUnbind(ImChannelContext imChannelContext, Group group) throws ImException {

    }

    /**
     * 绑定用户
     *
     * @param imChannelContext
     *            通道上下文
     * @param user
     *            绑定用户信息
     * @throws ImException
     */
    @Override
    public void onAfterUserBind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.ONLINE.getStatus());
        this.messageHelper.updateUserTerminal(imChannelContext.getUserId(), user);
        // initUserInfo(user);
    }

    /**
     * 用户解绑
     *
     * @param imChannelContext
     *            通道上下文
     * @param user
     *            解绑用户信息
     * @throws ImException
     */
    @Override
    public void onAfterUserUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.OFFLINE.getStatus());
        List<ImChannelContext> byUserId = JimServerAPI.getByUserId(imChannelContext.getUserId());
        if (CollectionUtil.isNotEmpty(byUserId) && byUserId.size() > 1) {
            return;
        }
        this.messageHelper.updateUserTerminal(imChannelContext.getUserId(), user);
    }

    /**
     * 主要给添加/删除好友调用 添加或者移出用户 此接口会重新添加缓存 缓存1:USER 用户信息 key =userId + SUFFIX + INFO value=user 详细信息 缓存2:USER 用户的好友信息 key
     * =userId + SUFFIX + FRIENDS value=好友详细信息集合 to String
     *
     * @param userId
     * @throws ImException
     */
    @Override
    public void onAfterAddOrRemoveUser(String operateUserId, String userId) throws ImException {
        if (!isStore() || Objects.isNull(userId)) {
            return;
        }
        // 删除离线消息
        LocalCacheUtils.me().removeUserMessageCache(operateUserId, userId);
        //
        User user = User.newBuilder().userId(userId).build();
        initUserInfo(operateUserId, user);
        logger.info("添加好友或者移出好友 清除缓存");
    }

    @Override
    public void onAfterAddGroup(String operateUserId, String userId, String groupId) throws ImException {
        if (!isStore() || StringUtils.isBlank(userId) || StringUtils.isBlank(groupId)) {
            return;
        }
        logger.info("添加群加载缓存");
        initGroupUsers(operateUserId, groupId, userId);
        initUserGroups(operateUserId, userId);
    }

    /**
     * 添加/删除好友群信息
     *
     * @param userId
     * @param groupId
     * @throws ImException
     */
    @Override
    public void onAfterRemoveGroup(String operateUserId, String userId, String groupId) throws ImException {
        if (!isStore() || StringUtils.isBlank(userId) || StringUtils.isBlank(groupId)) {
            return;
        }
        logger.info("移出群 清除缓存");
        //
        LocalCacheUtils.me().removeGroupUserIdCache(groupId, userId);
    }

    @Override
    public void onAfterDissolveGroup(String operateUserId, String groupId) throws ImException {
        if (!isStore() || StringUtils.isBlank(groupId)) {
            return;
        }
        logger.info("解散群 清除缓存");
        // 移除群组成员;
        List<String> list = LocalCacheUtils.me().getGroupUserIdsByCache(groupId);
        if (CollectionUtils.isNotEmpty(list)) {
            for (String userId : list) {
                onAfterRemoveGroup(operateUserId, userId, groupId);
            }
        }
        //
        LocalCacheUtils.me().dissolveGroupCache(groupId);
    }

    /**
     * 初始化群组用户;
     *
     */
    public void initGroupUsers(String operateUserId, String groupId, String userId) {
        if (!isStore()) {
            return;
        }
        if (StringUtils.isEmpty(groupId)) {
            return;
        }
        LocalCacheUtils.me().removeGroupInfoCache(groupId);
        //
        List<String> userIds = new ArrayList<>();
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            userIds = chatCommonMethodUtils.getGroupUsers(operateUserId, groupId);
            Group group1 = chatCommonMethodUtils.getGroupById(operateUserId, groupId);
            if (group1 != null) {
                LocalCacheUtils.me().saveGroupInfoByCache(groupId, group1);
            }
        }
        if (userIds != null && userIds.size() > 0) {
            LocalCacheUtils.me().saveGroupUserIdsByCache(groupId, userIds);
        }
        initUserGroups(operateUserId, userId);

    }

    /**
     * 初始化用户拥有哪些群组;
     *
     * @param userId
     */
    public void initUserGroups(String operateUserId, String userId) {
        if (!isStore()) {
            return;
        }
        if (StringUtils.isBlank(userId)) {
            return;
        }
        //
        List<String> groupIds = new ArrayList<>();
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            groupIds = chatCommonMethodUtils.getAllGroupUsers(operateUserId, userId);
        }
        if (CollectionUtil.isNotEmpty(groupIds)) {
            LocalCacheUtils.me().saveUserGroupIdsByCache(userId, groupIds);
        }
    }

    /**
     * 初始化用户终端协议类型;
     *
     * @param user
     */
    public void initUserInfo(String operateUserId, User user) {
        if (!isStore() || user == null) {
            return;
        }
        String userId = user.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        // 先清空缓存
        LocalCacheUtils.me().removeUserInfoCache(userId);
        //
        User newUser = null;
        List<User> friends = null;
        if (isSqlSave) {
            ChatCommonMethodUtils chatCommonMethodUtils =
                ApplicationContextProvider.getBean(ChatCommonMethodUtils.class);
            newUser = chatCommonMethodUtils.getUserById(operateUserId, userId);
            //
            friends = chatCommonMethodUtils.initUserFrineds(operateUserId, userId);
        }
        if (newUser == null) {
            return;
        }
        user = newUser;
        LocalCacheUtils.me().saveUserInfoByCache(userId, user);
        //
        if (CollectionUtils.isEmpty(friends)) {
            return;
        }
        List<String> friendIds = friends.stream().filter(user1 -> StringUtils.isNotBlank(user1.getUserId()))
            .map(User::getUserId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(friendIds)) {
            return;
        }
        LocalCacheUtils.me().saveUserFriendIdsByCache(userId, friendIds);
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

}
