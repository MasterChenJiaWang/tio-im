/**
 *
 */
package com.daren.chen.im.core.utils;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.packets.ImClientNode;

/**
 * IM工具类;
 *
 * @author WChao
 *
 */
public class ImKit {

    private static final Logger logger = LoggerFactory.getLogger(ImKit.class);

    /**
     * 设置Client对象到ImSessionContext中
     *
     * @param channelContext
     *            通道上下文
     * @return 客户端Node信息
     * @author: WChao
     */
    public static ImClientNode initImClientNode(ImChannelContext channelContext) {
        ImSessionContext imSessionContext = channelContext.getSessionContext();
        ImClientNode imClientNode = imSessionContext.getImClientNode();
        if (Objects.nonNull(imClientNode)) {
            return imClientNode;
        }
        imClientNode = ImClientNode.newBuilder().id(channelContext.getId()).ip(channelContext.getClientNode().getIp())
            .port(channelContext.getClientNode().getPort()).build();
        imSessionContext.setImClientNode(imClientNode);
        return imClientNode;
    }

}
