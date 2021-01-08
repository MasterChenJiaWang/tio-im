package com.daren.chen.im.server.springboot.handler;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.common.StatisticsUtils;
import com.daren.chen.im.server.handler.ImServerHandler;
import com.daren.chen.im.server.protocol.AbstractProtocolHandler;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 默认消息处理
 *
 * @ClassName DefaultImServerHandler
 * @Description
 * @Author WChao
 * @Date 2020/1/6 2:25
 * @Version 1.0
 **/
public class MyImServerHandler implements ImServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyImServerHandler.class);

    /**
     * 处理消息包
     *
     * @param imPacket
     * @param imChannelContext
     * @throws Exception
     */
    @Override
    public void handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {
        if (imPacket != null) {
            byte[] body = imPacket.getBody();
            if (body != null) {
                try {
                    String str = new String(body, ImConst.CHARSET);
                    Command command = imPacket.getCommand();
                    if (command != Command.COMMAND_HEARTBEAT_REQ) {
                        logger.info("----服务端收到消息:上下文ID[{}] 用户ID[{}] , {}", imChannelContext.getId(),
                            imChannelContext.getUserId(), str);
                        // TODO
                        StatisticsUtils.addTask(new StatisticsUtils.AdminTask(imChannelContext, imPacket));
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
        AbstractProtocolHandler handler = imServerChannelContext.getProtocolHandler();
        if (Objects.isNull(handler)) {
            return;
        }
        // 消息处理
        handler.handler(imPacket, imChannelContext);
    }

    /**
     * 编码
     *
     * @param imPacket
     * @param imConfig
     * @param imChannelContext
     * @return
     */
    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        if (imPacket != null) {
            byte[] body = imPacket.getBody();
            if (body != null) {
                try {
                    String str = new String(body, ImConst.CHARSET);
                    Command command = imPacket.getCommand();
                    if (command != Command.COMMAND_HEARTBEAT_REQ) {
                        logger.info(">>>>>>>服务端发送的消息:上下文ID[{}] 用户ID[{}]  Command [{}], {}", imChannelContext.getId(),
                            imChannelContext.getUserId(), command.getNumber(), str);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
        AbstractProtocolHandler handler = imServerChannelContext.getProtocolHandler();
        if (handler != null) {
            return handler.encode(imPacket, imConfig, imServerChannelContext);
        }
        return null;
    }

    /**
     * 解码
     *
     * @param buffer
     * @param limit
     * @param position
     * @param readableLength
     * @param imChannelContext
     * @return
     * @throws ImDecodeException
     */
    @Override
    public ImPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
        ImChannelContext imChannelContext) throws ImDecodeException {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
        AbstractProtocolHandler handler = imServerChannelContext.getProtocolHandler();
        if (Objects.isNull(imServerChannelContext.getSessionContext())) {
            handler = ProtocolManager.initProtocolHandler(buffer, imServerChannelContext);
        }
        if (handler != null) {
            return handler.decode(buffer, limit, position, readableLength, imServerChannelContext);
        } else {
            throw new ImDecodeException("unsupported protocol type, the protocol decoder cannot be found");
        }
    }
}
