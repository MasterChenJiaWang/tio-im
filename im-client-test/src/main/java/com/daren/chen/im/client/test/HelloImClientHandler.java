package com.daren.chen.im.client.test;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.client.handler.ImClientHandler;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.tcp.TcpPacket;
import com.daren.chen.im.core.tcp.TcpServerDecoder;
import com.daren.chen.im.core.tcp.TcpServerEncoder;

/**
 *
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年8月30日 下午1:10:28
 */
public class HelloImClientHandler implements ImClientHandler, ImConst {
    private static Logger logger = LoggerFactory.getLogger(HelloImClientHandler.class);

    /**
     * 处理消息
     */
    @Override
    public void handler(ImPacket imPacket, ImChannelContext channelContext) {
        TcpPacket helloPacket = (TcpPacket)imPacket;
        byte[] body = helloPacket.getBody();
        if (body != null) {
            try {
                String str = new String(body, ImConst.CHARSET);
                decodeCommand(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return;
    }

    private void decodeCommand(String str) {
        RespBody r = JSON.parseObject(str, RespBody.class);
        Command command = r.getCommand();
        if (command != Command.COMMAND_HEARTBEAT_REQ) {
            // logger.info("客户端收到消息:[{}], {}", command.getNumber(), str);
        }
        switch (command) {
            // 握手响应，含http的websocket握手响应
            case COMMAND_HANDSHAKE_RESP:
                r = JSON.parseObject(str, RespBody.class);
                break;
            // 鉴权响应
            case COMMAND_AUTH_RESP:
                r = JSON.parseObject(str, AuthRespBody.class);
                break;
            // 登录响应
            case COMMAND_LOGIN_RESP:
                break;
            // 申请进入群组响应
            case COMMAND_JOIN_GROUP_RESP:
                break;
            // 进入群组通知
            case COMMAND_JOIN_GROUP_NOTIFY_RESP:
                break;
            // 退出群组通知
            case COMMAND_EXIT_GROUP_NOTIFY_RESP:
                break;
            // 聊天响应
            case COMMAND_CHAT_RESP:
                break;
            // 收到撤消消息指令
            case COMMAND_CANCEL_MSG_RESP:
                break;
            // 获取用户信息响应
            case COMMAND_GET_USER_RESP:
                break;
            // 获取聊天消息响应
            case COMMAND_GET_MESSAGE_RESP:
                JSON.parseObject(str, AuthRespBody.class);
                break;
            case COMMAND_LOGIN_REQ:
                break;
            default:
                break;
        }
    }

    /**
     * 编码：把业务消息包编码为可以发送的ByteBuffer 总的消息结构：消息头 + 消息体 消息头结构： 4个字节，存储消息体的长度 消息体结构： 对象的json串的byte[]
     */
    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        TcpPacket tcpPacket = (TcpPacket)imPacket;
        return TcpServerEncoder.encode(tcpPacket, imConfig, imChannelContext);
    }

    @Override
    public TcpPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
        ImChannelContext imChannelContext) throws ImDecodeException, ImDecodeException {
        TcpPacket tcpPacket = TcpServerDecoder.decode(buffer, imChannelContext);
        return tcpPacket;
    }

    private static TcpPacket heartbeatPacket =
        new TcpPacket(Command.COMMAND_HEARTBEAT_REQ, new byte[] {Protocol.HEARTBEAT_BYTE});

    /**
     * 此方法如果返回null，框架层面则不会发心跳；如果返回非null，框架层面会定时发本方法返回的消息包
     */
    @Override
    public TcpPacket heartbeatPacket(ImChannelContext imChannelContext) {
        return heartbeatPacket;
    }

}
