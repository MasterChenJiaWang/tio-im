/**
 *
 */
package com.daren.chen.im.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;

import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.helper.redis.RedisMessageHelper;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.google.common.base.Stopwatch;

/**
 * JIM服务端启动类
 *
 * @author WChao
 *
 */
public class JimServer {

    private static final Logger log = LoggerFactory.getLogger(JimServer.class);
    private TioServer tioServer = null;
    private final ImServerConfig imServerConfig;

    public JimServer(ImServerConfig imServerConfig) {
        this.imServerConfig = imServerConfig;
    }

    public void init(ImServerConfig imServerConfig) {
        System.setProperty("tio.default.read.buffer.size", String.valueOf(imServerConfig.getReadBufferSize()));
        if (imServerConfig.getMessageHelper() == null) {
            imServerConfig.setMessageHelper(new RedisMessageHelper());
        }
        ProtocolManager.init();
        tioServer = new TioServer((ServerTioConfig)imServerConfig.getTioConfig());
    }

    public void start() throws IOException {
        Stopwatch timeWatch = Stopwatch.createStarted();
        log.warn("JIM Server start");
        init(imServerConfig);
        tioServer.start(this.imServerConfig.getBindIp(), this.imServerConfig.getBindPort());
        log.warn("JIM Server started at address: {} time:{}ms",
            imServerConfig.getBindIp() + ":" + imServerConfig.getBindPort(), timeWatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void stop() {
        tioServer.stop();
    }
}
