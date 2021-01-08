package com.daren.chen.im.client.config;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientTioConfig;
import org.tio.client.ReconnConf;

import com.daren.chen.im.client.handler.DefaultImClientHandler;
import com.daren.chen.im.client.handler.ImClientHandler;
import com.daren.chen.im.client.handler.ImClientHandlerAdapter;
import com.daren.chen.im.client.listener.DefaultImClientListener;
import com.daren.chen.im.client.listener.ImClientListener;
import com.daren.chen.im.client.listener.ImClientListenerAdapter;
import com.daren.chen.im.core.ImHandler;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.listener.ImListener;

import jdk.nashorn.internal.objects.Global;

/**
 * @ClassName ImServerConfig
 * @Description Im客户端配置
 * @Author WChao
 * @Date 2020/1/4 10:40
 * @Version 1.0
 **/
public class ImClientConfig extends ImConfig {

    private static Logger log = LoggerFactory.getLogger(ImClientConfig.class);
    /**
     * 客户端消息处理器
     */
    private ImClientHandler imClientHandler;
    /**
     * 客户端消息监听器
     */
    private ImClientListener imClientListener;
    /**
     * 重连配置
     */
    protected ReconnConf reconnConf;

    private ImClientConfig(ImClientHandler imClientHandler, ImClientListener imClientListener) {
        setImClientHandler(imClientHandler);
        setImClientListener(imClientListener);
        this.tioConfig = new ClientTioConfig(new ImClientHandlerAdapter(this.imClientHandler),
            new ImClientListenerAdapter(this.imClientListener), reconnConf);
        Global.set(this);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public ImHandler getImHandler() {
        return getImClientHandler();
    }

    @Override
    public ImListener getImListener() {
        return getImClientListener();
    }

    public static class Builder extends ImConfig.Builder<ImClientConfig, Builder> {

        private ImClientListener imClientListener;

        private ImClientHandler imClientHandler;

        protected ReconnConf reconnConf;

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder clientListener(ImClientListener imClientListener) {
            this.imClientListener = imClientListener;
            return getThis();
        }

        public Builder clientHandler(ImClientHandler imClientHandler) {
            this.imClientHandler = imClientHandler;
            return getThis();
        }

        public Builder reConnConf(ReconnConf reconnConf) {
            this.reconnConf = reconnConf;
            return getThis();
        }

        @Override
        public ImClientConfig build() {
            ImClientConfig imClientConfig = new ImClientConfig(this.imClientHandler, this.imClientListener);
            imClientConfig.setBindIp(this.bindIp);
            imClientConfig.setBindPort(this.bindPort);
            imClientConfig.setReadBufferSize(this.readBufferSize);
            imClientConfig.setSslConfig(this.sslConfig);
            imClientConfig.setReConnConf(this.reconnConf);
            imClientConfig.setHeartbeatTimeout(this.heartbeatTimeout);
            imClientConfig.setImGroupListener(this.imGroupListener);
            imClientConfig.setImUserListener(this.imUserListener);
            return imClientConfig;
        }
    }

    public ImClientHandler getImClientHandler() {
        return imClientHandler;
    }

    public void setImClientHandler(ImClientHandler imClientHandler) {
        this.imClientHandler = imClientHandler;
        if (Objects.isNull(this.imClientHandler)) {
            this.imClientHandler = new DefaultImClientHandler();
        }
    }

    public ImClientListener getImClientListener() {
        return imClientListener;
    }

    public void setImClientListener(ImClientListener imClientListener) {
        this.imClientListener = imClientListener;
        if (Objects.isNull(this.imClientListener)) {
            this.imClientListener = new DefaultImClientListener();
        }
    }

    public ReconnConf getReConnConf() {
        return reconnConf;
    }

    public void setReConnConf(ReconnConf reconnConf) {
        this.reconnConf = reconnConf;
        ClientTioConfig clientTioConfig = (ClientTioConfig)this.tioConfig;
        clientTioConfig.setReconnConf(reconnConf);
    }
}
