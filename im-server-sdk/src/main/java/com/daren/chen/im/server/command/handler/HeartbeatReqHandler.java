package com.daren.chen.im.server.command.handler;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.HeartbeatBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 *
 */
public class HeartbeatReqHandler extends AbstractCmdHandler {
    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        RespBody heartbeatBody =
            new RespBody(Command.COMMAND_HEARTBEAT_REQ).setData(new HeartbeatBody(Protocol.HEARTBEAT_BYTE));
        return ProtocolManager.Converter.respPacket(heartbeatBody, channelContext);
    }

    @Override
    public Command command() {
        return Command.COMMAND_HEARTBEAT_REQ;
    }
}
