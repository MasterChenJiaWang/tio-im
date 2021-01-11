package com.daren.chen.im.core.config;

import java.io.Serializable;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/7 10:08
 */
public class Config implements Serializable {

    private static final long serialVersionUID = 1812091913088481777L;
    /**
     * 绑定IP
     */
    private String bindIp = "0.0.0.0";

    /**
     * 绑定端口
     */
    private Integer port = 8080;

    /**
     * 心跳超时时间
     */
    private Integer heartbeatTimeOut = 0;
    /**
     * 是否持久化存储
     */
    private Boolean store = true;

    /**
     * 是否使用集群
     */
    private Boolean cluster = false;
    /**
     *
     */
    private String topicSuffix;
    /**
     * ssl 配置
     */
    private Ssl ssl = new Ssl();
    /**
     * http 配置
     */
    private Http http = new Http();
    /**
     * redis 配置
     */
    private Redis redis = new Redis();

    /**
     * 后台api 配置
     */
    private Api api = new Api();
    /**
     * 统计配置
     */
    private Statistics statistics = new Statistics();

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getHeartbeatTimeOut() {
        return heartbeatTimeOut;
    }

    public void setHeartbeatTimeOut(Integer heartbeatTimeOut) {
        this.heartbeatTimeOut = heartbeatTimeOut;
    }

    public Boolean getStore() {
        return store;
    }

    public void setStore(Boolean store) {
        this.store = store;
    }

    public Boolean getCluster() {
        return cluster;
    }

    public void setCluster(Boolean cluster) {
        this.cluster = cluster;
    }

    public String getTopicSuffix() {
        return topicSuffix;
    }

    public void setTopicSuffix(String topicSuffix) {
        this.topicSuffix = topicSuffix;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 访问后台url
     */
    public static class Ssl implements Serializable {
        private static final long serialVersionUID = 486951117450780670L;
        /**
         *
         */
        private Boolean enabled = false;

        /**
         *
         */
        private String keyStorePath;
        /**
         *
         */
        private String keyStorePwd;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getKeyStorePath() {
            return keyStorePath;
        }

        public void setKeyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
        }

        public String getKeyStorePwd() {
            return keyStorePwd;
        }

        public void setKeyStorePwd(String keyStorePwd) {
            this.keyStorePwd = keyStorePwd;
        }
    }

    public static class Http implements Serializable {

        private static final long serialVersionUID = 4472483148896530937L;
        /**
         *
         */
        private String page = "pages";

        /**
         *
         */
        private Integer maxLiveTime = 0;

        /**
         *
         */
        private String scanPackages = "com.daren.chen.im.server.springboot.ImServerCupStart";

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public Integer getMaxLiveTime() {
            return maxLiveTime;
        }

        public void setMaxLiveTime(Integer maxLiveTime) {
            this.maxLiveTime = maxLiveTime;
        }

        public String getScanPackages() {
            return scanPackages;
        }

        public void setScanPackages(String scanPackages) {
            this.scanPackages = scanPackages;
        }
    }

    public static class Redis implements Serializable {

        private static final long serialVersionUID = -1721103193215851769L;
        /**
         *
         */
        private Integer retrynum = 100;

        /**
         *
         */
        private Integer maxactive = 1;

        /**
         *
         */
        private Integer maxidle = 20;
        /**
         *
         */
        private Integer maxwait = 5000;
        /**
         *
         */
        private Integer timeout = 5000;

        /**
         *
         */
        private String host = "127.0.0.1";
        /**
         *
         */
        private Integer port = 6379;

        /**
         * 数据库
         */
        private int database = 0;
        /**
         *
         */
        private String auth;

        /**
         * 是否redis 集群
         */
        private boolean cluster = false;

        /**
         * 集群时才有用 格式: ip1:port1,ip2:port2
         */
        private String clusterAddr = "";

        public Integer getRetrynum() {
            return retrynum;
        }

        public void setRetrynum(Integer retrynum) {
            this.retrynum = retrynum;
        }

        public Integer getMaxactive() {
            return maxactive;
        }

        public void setMaxactive(Integer maxactive) {
            this.maxactive = maxactive;
        }

        public Integer getMaxidle() {
            return maxidle;
        }

        public void setMaxidle(Integer maxidle) {
            this.maxidle = maxidle;
        }

        public Integer getMaxwait() {
            return maxwait;
        }

        public void setMaxwait(Integer maxwait) {
            this.maxwait = maxwait;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getClusterAddr() {
            return clusterAddr;
        }

        public void setClusterAddr(String clusterAddr) {
            this.clusterAddr = clusterAddr;
        }

        public boolean isCluster() {
            return cluster;
        }

        public void setCluster(boolean cluster) {
            this.cluster = cluster;
        }
    }

    /**
     * 访问后台url
     */
    public static class Api implements Serializable {
        private static final long serialVersionUID = -4545845266610498722L;
        /**
         * api url
         */
        private String url;
        /**
         * 是否使用mysql 保存数据
         */
        private boolean enabled = false;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 访问后台url
     */
    public static class Statistics implements Serializable {
        private static final long serialVersionUID = -1200886783940858388L;
        /**
         * 是否开启统计
         */
        private Boolean enabled = false;

        /**
         * 统计传给的 用户ID
         */
        private String userId;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
