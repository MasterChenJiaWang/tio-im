/**
 *
 */
package com.daren.chen.im.server.config;

import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.ws.WsConfig;

/**
 * @author WChao
 *
 */
public class DefaultImConfigBuilder extends ImServerConfigBuilder {

    @Override
    public ImServerConfigBuilder configHttp(HttpConfig httpConfig) {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public ImServerConfigBuilder configWs(WsConfig wsServerConfig) {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public ImServerConfigBuilder redisConfiguration() throws Exception {
        return null;
    }

    @Override
    public ImServerConfigBuilder initStatisticsUtils() throws Exception {
        return null;
    }

    @Override
    protected ImServerConfigBuilder getThis() {
        return this;
    }

}
