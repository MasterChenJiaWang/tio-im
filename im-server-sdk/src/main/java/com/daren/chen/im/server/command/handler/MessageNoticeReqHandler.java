package com.daren.chen.im.server.command.handler;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.MessageNoticeReqBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 收到消息反馈命令处理器
 *
 * @author WChao
 * @date 2018年4月10日 下午2:40:07
 */
public class MessageNoticeReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {

        return Command.COMMAND_NOTICE_MESSAGE_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        RespBody resPacket;
        MessageNoticeReqBody messageNoticeReqBody;
        try {
            messageNoticeReqBody = JsonKit.toBean(packet.getBody(), MessageNoticeReqBody.class);
        } catch (Exception e) {
            // 用户消息格式不正确
            return getMessageFailedPacket(imChannelContext);
        }
        UserMessageData messageData = null;
        ImServerConfig imServerConfig = ImConfig.Global.get();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();

        // resPacket = new RespBody(Command.COMMAND_NOTICE_MESSAGE_RESP, ImStatus.C10022);
        // resPacket.setData(messageData);
        // return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        return null;
    }

    /**
     * 获取用户消息失败响应包;
     *
     * @param imChannelContext
     * @return
     */
    public ImPacket getMessageFailedPacket(ImChannelContext imChannelContext) throws ImException {
        RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10023.getText());
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }
}
