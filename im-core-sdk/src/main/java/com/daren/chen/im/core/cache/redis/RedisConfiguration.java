package com.daren.chen.im.core.cache.redis;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.daren.chen.im.core.config.Config;

/**
 * @author WChao
 * @date 2018年3月9日 上午1:09:03
 */
public class RedisConfiguration implements Serializable {

    private int retryNum = 100;
    private int maxActive = 100;
    private int maxIdle = 20;
    private long maxWait = 5000L;
    private int timeout = 2000;
    private String auth;
    private String host = "";
    private int port = 0;
    /**
     * 数据库
     */
    private int database = 0;
    /**
     * 是否集群
     */
    private boolean cluster = false;
    /**
     * 集群时才有用
     */
    private String clusterAddr = "";

    public RedisConfiguration() {
        // this.retryNum = Integer.parseInt(PropUtil.get("jim.redis.retrynum", "100"));
        // this.maxActive = Integer.parseInt(PropUtil.get("jim.redis.maxactive", "100"));
        // this.maxIdle = Integer.parseInt(PropUtil.get("jim.redis.maxidle", "20"));
        // this.maxWait = Long.parseLong(PropUtil.get("jim.redis.maxwait", "5000"));
        // this.timeout = Integer.parseInt(PropUtil.get("jim.redis.timeout", "2000"));
        // this.auth = PropUtil.get("jim.redis.auth", null);
        // if (StringUtils.isEmpty(auth)) {
        // this.auth = null;
        // }
        // this.host = PropUtil.get("jim.redis.host", "");
        // this.port = Integer.parseInt(PropUtil.get("jim.redis.port", "0"));
    }

    public RedisConfiguration(Properties prop) {
        this.retryNum = Integer.parseInt(prop.getProperty("jim.redis.retrynum", "100"));
        this.maxActive = Integer.parseInt(prop.getProperty("jim.redis.maxactive", "100"));
        this.maxIdle = Integer.parseInt(prop.getProperty("jim.redis.maxidle", "20"));
        this.maxWait = Long.parseLong(prop.getProperty("jim.redis.maxwait", "5000"));
        this.timeout = Integer.parseInt(prop.getProperty("jim.redis.timeout", "2000"));
        this.auth = prop.getProperty("jim.redis.auth", null);
        if (StringUtils.isEmpty(auth)) {
            this.auth = null;
        }
        this.host = prop.getProperty("jim.redis.host", "");
        this.port = Integer.parseInt(prop.getProperty("jim.redis.port", "0"));
    }

    public RedisConfiguration(Config config) {
        Config.Redis redis = config.getRedis();
        this.retryNum = redis.getRetrynum();
        this.maxActive = redis.getMaxactive();
        this.maxIdle = redis.getMaxidle();
        this.maxWait = redis.getMaxwait();
        this.timeout = redis.getTimeout();
        this.auth = redis.getAuth();
        if (StringUtils.isEmpty(auth)) {
            this.auth = null;
        }
        this.host = redis.getHost();
        this.port = redis.getPort();
        this.cluster = redis.isCluster();
        this.clusterAddr = redis.getClusterAddr();
        this.database = redis.getDatabase();
    }

    public int getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
