/**
 *
 */
package com.daren.chen.im.client;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.TioClient;
import org.tio.core.Node;

import com.daren.chen.im.client.config.ImClientConfig;
import com.daren.chen.im.core.ImConst;

/**
 * JIM客户端连接类
 *
 * @author WChao
 *
 */
public class JimClient {

    private static final Logger log = LoggerFactory.getLogger(JimClient.class);

    private TioClient tioClient = null;
    private final ImClientConfig imClientConfig;

    public JimClient(ImClientConfig imClientConfig) {
        this.imClientConfig = imClientConfig;
    }

    public ImClientChannelContext connect(Node serverNode) throws Exception {
        return connect(serverNode, null);
    }

    public ImClientChannelContext connect(Node serverNode, Integer timeout) throws Exception {
        log.warn("JIM client connect");
        tioClient = new TioClient((ClientTioConfig)imClientConfig.getTioConfig());
        ClientChannelContext clientChannelContext =
            tioClient.connect(serverNode, imClientConfig.getBindIp(), imClientConfig.getBindPort(), timeout);
        if (Objects.nonNull(clientChannelContext)) {
            log.warn("JIM client connected success at serverAddress:[{}], bind localAddress:[{}]",
                serverNode.toString(), imClientConfig.toBindAddressString());
            Object o = null;
            // 为空就等1s
            int n = 100;
            while (n > 0 && (o = clientChannelContext.get(ImConst.Key.IM_CHANNEL_CONTEXT_KEY)) == null) {
                Thread.sleep(10);
                n--;
            }
            if (o == null) {
                log.warn("JIM client connected fail at serverAddress:[{}], bind localAddress:[{}]",
                    serverNode.toString(), imClientConfig.toBindAddressString());
                tioClient.stop();
                return null;
            }
            return (ImClientChannelContext)o;
        }
        log.warn("JIM client connected fail at serverAddress:[{}], bind localAddress:[{}]", serverNode.toString(),
            imClientConfig.toBindAddressString());
        return null;
    }

    public void stop() {
        tioClient.stop();
    }

}
