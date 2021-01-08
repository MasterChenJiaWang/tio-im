package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.DeleteFriendRespBody;
import com.daren.chen.im.core.packets.DeleteFriendResult;
import com.daren.chen.im.core.packets.DeleteUser;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.friend.FriendCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 离开群组消息cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class DeleteFriendReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(DeleteFriendReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            // 绑定群组;
            DeleteFriendRespBody deleteFriendRespBody = null;
            DeleteUser deleteUser = JsonKit.toBean(packet.getBody(), DeleteUser.class);
            if (deleteUser == null) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                deleteFriendRespBody = DeleteFriendRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(deleteFriendRespBody, imChannelContext);
            }
            String curUserId = deleteUser.getCurUserId();
            String friendUserId = deleteUser.getFriendUserId();
            if (StringUtils.isBlank(curUserId) || StringUtils.isBlank(friendUserId)) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                deleteFriendRespBody = DeleteFriendRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(deleteFriendRespBody, imChannelContext);
            }
            // 实际绑定之前执行处理器动作
            FriendCmdProcessor friendProcessor = this.getSingleProcessor(FriendCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(friendProcessor)) {
                deleteFriendRespBody = friendProcessor.deleteFriend(curUserId, friendUserId, imChannelContext);
                boolean joinGroupIsTrue =
                    Objects.isNull(deleteFriendRespBody) || DeleteFriendResult.DELETE_FRIEND_RESULT_OK
                        .getNumber() != deleteFriendRespBody.getResult().getNumber();
                if (joinGroupIsTrue) {
                    deleteFriendRespBody = DeleteFriendRespBody.failed().setData(deleteFriendRespBody);
                    return ProtocolManager.Converter.respPacket(deleteFriendRespBody, imChannelContext);
                }
            }
            RespBody resPacket = new RespBody(Command.COMMAND_ADD_FRIEND_REQ_RESP, ImStatus.C10024.getText());
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(DeleteFriendRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    @Override
    public Command command() {

        return Command.COMMAND_DELETE_FRIEND_REQ;
    }
}
