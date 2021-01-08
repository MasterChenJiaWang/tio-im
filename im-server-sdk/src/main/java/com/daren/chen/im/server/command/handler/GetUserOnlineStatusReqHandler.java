package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.GetUserOnlineStatusReq;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.core.packets.GetUserOnlineStatusResult;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.user.UserCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 离开群组消息cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class GetUserOnlineStatusReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(GetUserOnlineStatusReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            // 绑定群组;
            GetUserOnlineStatusRespBody userOnlineStatusRespBody = null;
            GetUserOnlineStatusReq userOnlineStatusReq = JsonKit.toBean(packet.getBody(), GetUserOnlineStatusReq.class);
            if (userOnlineStatusReq == null) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                userOnlineStatusRespBody = GetUserOnlineStatusRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(userOnlineStatusRespBody, imChannelContext);
            }
            String userId = userOnlineStatusReq.getUserId();
            if (StringUtils.isBlank(userId)) {
                log.error("上下文ID [{}] 用户ID [{}]   user is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                userOnlineStatusRespBody = GetUserOnlineStatusRespBody.failed().setMsg("user is null");
                return ProtocolManager.Converter.respPacket(userOnlineStatusRespBody, imChannelContext);
            }
            // 实际绑定之前执行处理器动作
            UserCmdProcessor userCmdProcessor = this.getSingleProcessor(UserCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(userCmdProcessor)) {
                userOnlineStatusRespBody = userCmdProcessor.getUserOnlineStatus(userId, imChannelContext);
                boolean joinGroupIsTrue = Objects.isNull(userOnlineStatusRespBody)
                    || GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_OK
                        .getNumber() != userOnlineStatusRespBody.getResult().getNumber();
                if (joinGroupIsTrue) {
                    userOnlineStatusRespBody = GetUserOnlineStatusRespBody.failed().setData(userOnlineStatusRespBody);
                    return ProtocolManager.Converter.respPacket(userOnlineStatusRespBody, imChannelContext);
                }
            }
            GetUserOnlineStatusRespBody getUserOnlineStatusRespBody =
                GetUserOnlineStatusRespBody.success().setData(userOnlineStatusRespBody);
            return ProtocolManager.Converter.respPacket(getUserOnlineStatusRespBody, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(GetUserOnlineStatusRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    @Override
    public Command command() {

        return Command.GET_USER_ONLINE_STATUS_REQ;
    }
}
