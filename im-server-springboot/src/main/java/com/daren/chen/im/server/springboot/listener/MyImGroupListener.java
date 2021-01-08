package com.daren.chen.im.server.springboot.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.ExitGroupNotifyRespBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.ImClientNode;
import com.daren.chen.im.core.packets.JoinGroupNotifyRespBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.listener.AbstractImGroupListener;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 群组绑定监听器
 *
 * @author WChao 2017年5月13日 下午10:38:36
 */
public class MyImGroupListener extends AbstractImGroupListener {

    private static final Logger logger = LoggerFactory.getLogger(MyImGroupListener.class);

    @Override
    public void doAfterBind(ImChannelContext imChannelContext, Group group) throws ImException {
        // logger.info("上下文ID [{}] 用户ID [{}] 群组:{},绑定成功!", imChannelContext.getId(), imChannelContext.getUserId(),
        // JsonKit.toJSONString(group));
        // JoinGroupRespBody joinGroupRespBody = JoinGroupRespBody.success();
        // 回一条消息，告诉对方进群结果
        // joinGroupRespBody.setGroup(group.getGroupId());
        // ImPacket respPacket = ProtocolManager.Converter.respPacket(joinGroupRespBody, imChannelContext);
        // Jim.send(imChannelContext, respPacket);
        // 进房通知 统一在handler 里做 登录的时候就不通知
        // 发送进房间通知;
        joinGroupNotify(group, imChannelContext);
    }

    /**
     * @param imChannelContext
     * @param group
     * @throws Exception
     * @author: WChao
     */
    @Override
    public void doAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException {
        // 发退出房间通知 统一在handler 里做 登录退出的时候就不通知
        // 发退出房间通知 COMMAND_EXIT_GROUP_NOTIFY_RESP
        exitGroupNotify(imChannelContext, group);
    }

    /**
     *
     * @param imChannelContext
     * @param group
     * @throws ImException
     */
    public void exitGroupNotify(ImChannelContext imChannelContext, Group group) throws ImException {
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        ImClientNode imClientNode = imSessionContext.getImClientNode();
        if (imClientNode != null && imClientNode.isNotifyGroup()) {
            User clientUser = imClientNode.getUser();
            if (clientUser == null) {
                return;
            }
            ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
            exitGroupNotifyRespBody.setGroup(group.getGroupId());
            User notifyUser = User.newBuilder().userId(clientUser.getUserId()).nick(clientUser.getNick()).build();
            exitGroupNotifyRespBody.setUser(notifyUser);
            //
            RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
            ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
            JimServerAPI.sendToGroup(group.getGroupId(), imPacket);
        }

    }

    /**
     * 发送进房间通知;
     *
     * @param group
     *            群组对象
     * @param imChannelContext
     */
    public void joinGroupNotify(Group group, ImChannelContext imChannelContext) throws ImException {
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        ImClientNode imClientNode = imSessionContext.getImClientNode();
        if (imClientNode != null && imClientNode.isNotifyGroup()) {
            User clientUser = imClientNode.getUser();
            User notifyUser = User.newBuilder().userId(clientUser.getUserId()).nick(clientUser.getNick())
                .status(UserStatusType.ONLINE.getStatus()).build();
            String groupId = group.getGroupId();
            // 发进房间通知 COMMAND_JOIN_GROUP_NOTIFY_RESP
            JoinGroupNotifyRespBody joinGroupNotifyRespBody = JoinGroupNotifyRespBody.success();
            joinGroupNotifyRespBody.setGroup(groupId).setUser(notifyUser);
            JimServerAPI.sendToGroup(imChannelContext, groupId,
                ProtocolManager.Converter.respPacket(joinGroupNotifyRespBody, imChannelContext));
        }

    }

}
