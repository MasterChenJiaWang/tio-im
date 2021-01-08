package com.daren.chen.im.server.springboot.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.ExitGroupNotifyRespBody;
import com.daren.chen.im.core.packets.ExitGroupReqBody;
import com.daren.chen.im.core.packets.ExitGroupRespBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.JoinGroupNotifyRespBody;
import com.daren.chen.im.core.packets.JoinGroupReqBody;
import com.daren.chen.im.core.packets.JoinGroupRespBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.group.GroupCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/21 11:04
 */
public class GroupServiceProcessor extends AbstractProtocolCmdProcessor implements GroupCmdProcessor {
    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(GroupServiceProcessor.class);

    /**
     * @param joinGroupReqBody
     * @param imChannelContext
     * @return
     */
    @Override
    public JoinGroupRespBody join(JoinGroupReqBody joinGroupReqBody, ImChannelContext imChannelContext) {
        //
        try {
            // 加入的组
            String groupId = joinGroupReqBody.getGroupId();
            // 待加入的用户
            List<User> users = joinGroupReqBody.getUsers();
            // 是否是登录 登录会执行此方法 但是不需要通知群友
            boolean loginAdd = joinGroupReqBody.getLoginAdd();
            // 当前操作用户 当登录时 此为新加入用户
            String currentUserId = imChannelContext.getUserId();
            ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            // List<String> groupUsers = messageHelper.getGroupUsers(groupId);
            //
            Group group = Group.newBuilder().build();
            BeanUtil.copyProperties(joinGroupReqBody, group);
            // 登录
            if (loginAdd) {
                // 把当前用户绑入群组
                JimServerAPI.bindGroup(imChannelContext, group);
                return JoinGroupRespBody.success();
            }
            JimServerAPI.bindGroup(imChannelContext, group);
            // 非登录 需要通知群友
            List<User> newUserList = new ArrayList<>(users.size());
            if (CollectionUtil.isEmpty(users)) {
                return JoinGroupRespBody.success();
            }
            // 绑定新用户
            for (User user : users) {
                if (user == null) {
                    continue;
                }
                String userId = user.getUserId();
                if (StringUtils.isBlank(userId)) {
                    continue;
                }
                // 查询新用户的详细信息
                User userByType = messageHelper.getUserByType(imChannelContext.getUserId(), userId, 2);
                if (userByType != null) {
                    userByType.setStatus(UserStatusType.ONLINE.getStatus());
                    newUserList.add(userByType);
                }
                //
                List<ImChannelContext> byUserId = JimServerAPI.getByUserId(userId);
                if (CollectionUtil.isEmpty(byUserId)) {
                    continue;
                }
                // 绑定新用户
                for (ImChannelContext channelContext : byUserId) {
                    JimServerAPI.bindGroup(channelContext, group);
                }
            }
            // // 推送消息到其他群友
            // if (CollectionUtil.isNotEmpty(groupUsers)) {
            // // 会过滤 当前操作用户
            // joinGroupNotify(groupId, groupUsers, newUserList, imChannelContext);
            // }
            return JoinGroupRespBody.success();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return JoinGroupRespBody.failed();
        }
    }

    @Override
    public ExitGroupRespBody exit(ExitGroupReqBody exitGroup, ImChannelContext imChannelContext) {
        //
        try {
            // 退出的组
            String groupId = exitGroup.getGroupId();
            // 待退出的用户
            String userId = exitGroup.getUserId();
            // 当前操作用户
            String currentUserId = imChannelContext.getUserId();

            // 单个退出 或者 被踢出
            if (StringUtils.isNotBlank(userId)) {
                ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
                MessageHelper messageHelper = imServerConfig.getMessageHelper();
                List<String> groupUsers = messageHelper.getGroupUsers(imChannelContext.getUserId(), groupId);
                // 通知其他成员
                if (CollectionUtil.isNotEmpty(groupUsers)) {
                    // 不是主动退出 需要 通知对方
                    if (!currentUserId.equals(userId)) {
                        exitGroupUserNotify(userId, groupId, false);
                    }
                    // 会过滤 当前操作用户
                    exitGroupNotify(groupId, userId, false, groupUsers, imChannelContext);
                }
                // 解绑
                JimServerAPI.unbindGroup(userId, groupId);
            }
            // 解散
            else {
                // 通知其他成员
                // 会过滤 当前操作用户
                exitGroupNotify(groupId, userId, true, imChannelContext);
                // 解绑
                List<ImChannelContext> byGroup = JimServerAPI.getByGroup(groupId);
                if (byGroup != null) {
                    //
                    for (ImChannelContext channelContext : byGroup) {
                        JimServerAPI.unbindGroup(groupId, channelContext);
                    }
                }
            }
            return ExitGroupRespBody.success();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return ExitGroupRespBody.failed();
        }
    }

    public void exitGroupNotify(String groupId, String userId, boolean disband, ImChannelContext imChannelContext)
        throws ImException {
        ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
        exitGroupNotifyRespBody.setGroup(groupId);
        exitGroupNotifyRespBody.setDisband(disband);
        exitGroupNotifyRespBody.setUserId(userId);
        //
        RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
        ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
        JimServerAPI.sendToGroup(groupId, imPacket);
    }

    /**
     * @param groupId
     * @throws ImException
     */
    public void exitGroupNotify(String groupId, String userId, boolean disband, List<String> userIds,
        ImChannelContext imChannelContext) throws ImException {
        ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
        exitGroupNotifyRespBody.setGroup(groupId);
        exitGroupNotifyRespBody.setDisband(disband);
        exitGroupNotifyRespBody.setUserId(userId);
        //
        RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
        ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
        JimServerAPI.sendToGroupByIds(imChannelContext, groupId, userIds, imPacket);
    }

    /**
     * @param groupId
     * @throws ImException
     */
    public void exitGroupUserNotify(String userId, String groupId, boolean disband) throws ImException {
        ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
        exitGroupNotifyRespBody.setGroup(groupId);
        exitGroupNotifyRespBody.setDisband(disband);
        exitGroupNotifyRespBody.setUserId(userId);
        //
        RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
        ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
        JimServerAPI.sendToUser(userId, imPacket);
    }

    /**
     * 发送进房间通知;
     *
     * @param groupId
     *            群组对象
     * @param imChannelContext
     */
    public void joinGroupNotify(String groupId, List<String> userIds, List<User> users,
        ImChannelContext imChannelContext) throws ImException {

        if (CollectionUtil.isEmpty(userIds) || CollectionUtil.isEmpty(users)) {
            return;
        }
        for (User user : users) {
            // 发进房间通知 COMMAND_JOIN_GROUP_NOTIFY_RESP
            JoinGroupNotifyRespBody joinGroupNotifyRespBody = JoinGroupNotifyRespBody.success();
            joinGroupNotifyRespBody.setGroup(groupId).setUser(user);
            JimServerAPI.sendToGroupByIds(imChannelContext, groupId, userIds,
                ProtocolManager.Converter.respPacket(joinGroupNotifyRespBody, imChannelContext));
        }

    }
}
