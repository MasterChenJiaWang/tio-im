/**
 *
 */
package com.daren.chen.im.server.config;

import com.daren.chen.im.core.cache.redis.RedisConfiguration;
import com.daren.chen.im.core.cache.redis.RedisConfigurationFactory;
import com.daren.chen.im.core.config.ApiServerConfig;
import com.daren.chen.im.core.config.Config;
import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.ws.WsConfig;
import com.daren.chen.im.server.common.StatisticsUtils;

/**
 * @author WChao 2018/08/26
 */
public class PropertyImServerConfigBuilder
    extends ImServerConfigBuilder<ImServerConfig, PropertyImServerConfigBuilder> {

    private final Config config;

    /**
     *
     * @param config
     */
    public PropertyImServerConfigBuilder(Config config) {
        this.config = config;
    }

    @Override
    public PropertyImServerConfigBuilder configHttp(HttpConfig httpConfig) throws Exception {
        httpConfig.setBindPort(config.getPort());
        // 设置web访问路径;html/css/js等的根目录，支持classpath:，也支持绝对路径
        httpConfig.setPageRoot(config.getHttp().getPage());
        // 不缓存资源;
        httpConfig.setMaxLiveTimeOfStaticRes(config.getHttp().getMaxLiveTime());
        // 设置j-im mvc扫描目录;
        httpConfig.setScanPackages(config.getHttp().getScanPackages().split(","));
        return this;
    }

    @Override
    public PropertyImServerConfigBuilder configWs(WsConfig wsConfig) throws Exception {
        return this;
    }

    @Override
    public PropertyImServerConfigBuilder redisConfiguration() throws Exception {
        // redis 配置
        this.redisConfiguration = new RedisConfiguration(config);
        RedisConfigurationFactory.initRedisConfiguration(redisConfiguration);
        return this;
    }

    @Override
    public PropertyImServerConfigBuilder initStatisticsUtils() throws Exception {
        StatisticsUtils.setConfig(this.config);
        return this;
    }

    @Override
    protected PropertyImServerConfigBuilder getThis() {
        return this;
    }

    @Override
    public ImServerConfig build() throws Exception {
        super.build();
        // api
        ApiServerConfig apiServerConfig = new ApiServerConfig(config.getApi().getUrl(), config.getApi().getEnabled());
        //
        return ImServerConfig.newBuilder().bindIp(config.getBindIp()).bindPort(config.getPort())
            .heartbeatTimeout(config.getHeartbeatTimeOut()).isStore(config.getStore() ? "on" : "off")
            .httConfig(this.httpConfig).wsConfig(this.wsConfig).serverListener(this.serverListener)
            .imServerHandler(this.imServerHandler).messageListener(this.messageListener)
            .isCluster(config.getCluster() ? "on" : "off").isSSL(config.getSsl().getEnabled() ? "on" : "off")
            .apiServerConfig(apiServerConfig).build();
    }
}
