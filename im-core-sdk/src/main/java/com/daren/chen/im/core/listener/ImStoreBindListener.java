package com.daren.chen.im.core.listener;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.User;

/**
 * IM持久化绑定用户及群组监听器;
 *
 * @author WChao
 * @date 2018年4月8日 下午4:09:14
 */
public interface ImStoreBindListener {
    /**
     * 绑定群组后持久化回调该方法
     *
     * @param imChannelContext
     *            通道上下文
     * @param group
     *            绑定群组信息
     * @throws Exception
     */
    void onAfterGroupBind(ImChannelContext imChannelContext, Group group) throws ImException;

    /**
     * 解绑群组后持久化回调该方法
     *
     * @param imChannelContext
     *            通道上下文
     * @param group
     *            解绑群组信息
     * @throws Exception
     */
    void onAfterGroupUnbind(ImChannelContext imChannelContext, Group group) throws ImException;

    /**
     * 绑定用户后持久化回调该方法
     *
     * @param imChannelContext
     *            通道上下文
     * @param user
     *            绑定用户信息
     * @throws Exception
     */
    void onAfterUserBind(ImChannelContext imChannelContext, User user) throws ImException;

    /**
     * 解绑用户后回调该方法
     *
     * @param imChannelContext
     *            通道上下文
     * @param user
     *            解绑用户信息
     * @throws Exception
     */
    void onAfterUserUnbind(ImChannelContext imChannelContext, User user) throws ImException;

    /**
     * 添加或者删除用户 执行的操作
     *
     * @param userId
     * @throws ImException
     */
    void onAfterAddOrRemoveUser(String operateUserId, String userId) throws ImException;

    /**
     *
     * @param userId
     * @param groupId
     * @throws ImException
     */
    void onAfterAddGroup(String operateUserId, String userId, String groupId) throws ImException;

    /**
     * 添加或者删除用户组信息 执行的操作
     *
     * @param userId
     * @param groupId
     * @throws ImException
     */
    void onAfterRemoveGroup(String operateUserId, String userId, String groupId) throws ImException;

    /**
     * 解散组信息 执行的操作
     *
     * @param groupId
     * @throws ImException
     */
    void onAfterDissolveGroup(String operateUserId, String groupId) throws ImException;
}
