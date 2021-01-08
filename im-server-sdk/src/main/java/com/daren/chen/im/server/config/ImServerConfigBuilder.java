/**
 *
 */
package com.daren.chen.im.server.config;

import org.redisson.api.listener.MessageListener;

import com.daren.chen.im.core.cache.redis.RedisConfiguration;
import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.ws.WsConfig;
import com.daren.chen.im.server.handler.ImServerHandler;
import com.daren.chen.im.server.listener.ImServerListener;

/**
 * @author WChao 2018/08/26
 */
public abstract class ImServerConfigBuilder<T extends ImServerConfig, B extends ImServerConfigBuilder> {

    protected T conf;
    protected ImServerListener serverListener;

    protected ImServerHandler imServerHandler;

    /**
     *
     */
    protected MessageListener messageListener;

    protected HttpConfig httpConfig;
    protected WsConfig wsConfig;

    /**
     * redis 缓存
     */
    protected RedisConfiguration redisConfiguration;

    /**
     * 留给子类配置Http服务器相关配置
     *
     * @param httpConfig
     * @throws Exception
     * @return
     */
    public abstract B configHttp(HttpConfig httpConfig) throws Exception;

    /**
     * 配置WebSocket服务器相关配置
     *
     * @param wsConfig
     * @throws Exception
     * @return
     *
     */
    public abstract B configWs(WsConfig wsConfig) throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    public abstract B redisConfiguration() throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    public abstract B initStatisticsUtils() throws Exception;

    /**
     * 供子类获取自身builder抽象类;
     *
     * @return
     */
    protected abstract B getThis();

    public B serverListener(ImServerListener serverListener) {
        this.serverListener = serverListener;
        return getThis();
    }

    public B serverHandler(ImServerHandler imServerHandler) {
        this.imServerHandler = imServerHandler;
        return getThis();
    }

    public B messageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        return getThis();
    }

    public T build() throws Exception {
        this.httpConfig = HttpConfig.newBuilder().build();
        this.wsConfig = WsConfig.newBuilder().build();
        this.configHttp(httpConfig);
        this.configWs(wsConfig);
        this.redisConfiguration();
        this.initStatisticsUtils();
        return conf;
    }

}
