package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.AddFriendRespBody;
import com.daren.chen.im.core.packets.AddFriendResult;
import com.daren.chen.im.core.packets.AddUser;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.friend.FriendCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 离开群组消息cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class AddFriendReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(AddFriendReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            AddFriendRespBody addFriendRespBody = null;
            AddUser addUser = JsonKit.toBean(packet.getBody(), AddUser.class);
            if (addUser == null) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                addFriendRespBody = AddFriendRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(addFriendRespBody, imChannelContext);
            }
            String curUserId = addUser.getCurUserId();
            String friendUserId = addUser.getFriendUserId();
            if (StringUtils.isBlank(curUserId) || StringUtils.isBlank(friendUserId)) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                addFriendRespBody = AddFriendRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(addFriendRespBody, imChannelContext);
            }
            // 实际绑定之前执行处理器动作
            FriendCmdProcessor friendProcessor = this.getSingleProcessor(FriendCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(friendProcessor)) {
                addFriendRespBody = friendProcessor.addFriend(curUserId, friendUserId, imChannelContext);
                boolean joinGroupIsTrue = Objects.isNull(addFriendRespBody)
                    || AddFriendResult.ADD_FRIEND_RESULT_OK.getNumber() != addFriendRespBody.getResult().getNumber();
                if (joinGroupIsTrue) {
                    // 添加失败!
                    addFriendRespBody = AddFriendRespBody.failed().setData(addFriendRespBody);
                    return ProtocolManager.Converter.respPacket(addFriendRespBody, imChannelContext);
                } else {
                    // 添加成功!
                    addFriendRespBody = addFriendRespBody.setData(addFriendRespBody);
                    // 通知自己
                    return ProtocolManager.Converter.respPacket(addFriendRespBody, imChannelContext);
                }
            }
            // 没有添加成功
            addFriendRespBody = AddFriendRespBody.failed().setData(addFriendRespBody);
            return ProtocolManager.Converter.respPacket(addFriendRespBody, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(AddFriendRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    @Override
    public Command command() {

        return Command.COMMAND_ADD_FRIEND_REQ;
    }
}
