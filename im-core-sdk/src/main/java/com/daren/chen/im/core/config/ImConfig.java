/**
 *
 */
package com.daren.chen.im.core.config;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.tio.core.TioConfig;
import org.tio.core.ssl.SslConfig;
import org.tio.utils.prop.MapWithLockPropSupport;
import org.tio.utils.thread.pool.DefaultThreadFactory;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImHandler;
import com.daren.chen.im.core.banner.JimBanner;
import com.daren.chen.im.core.cache.redis.RedisConfiguration;
import com.daren.chen.im.core.cluster.ImCluster;
import com.daren.chen.im.core.listener.ImGroupListener;
import com.daren.chen.im.core.listener.ImGroupListenerAdapter;
import com.daren.chen.im.core.listener.ImListener;
import com.daren.chen.im.core.listener.ImUserListener;

/**
 * @author WChao
 *
 */
public abstract class ImConfig extends MapWithLockPropSupport implements ImConst {
    /**
     * IP地址
     */
    protected String bindIp = "0.0.0.0";
    /**
     * 监听端口
     */
    protected Integer bindPort = 0;

    /**
     * 默认的接收数据的buffer size
     */
    protected int readBufferSize = 1024 * 2;
    /**
     * 配置名称
     */
    protected String name = "JIM";
    // /**
    // * 集群配置 如果此值不为null，就表示要集群
    // */
    // private ImCluster cluster;
    /**
     * 集群配置 如果此值不为null，就表示要集群
     */
    private List<ImCluster> clusters;
    /**
     * tio相关配置信息
     */
    protected TioConfig tioConfig;
    /**
     * SSL配置
     */
    protected SslConfig sslConfig;
    /**
     * 连接微服务
     */
    protected ApiServerConfig apiServerConfig;
    /**
     * 心跳包发送时长heartbeatTimeout/2
     */
    protected long heartbeatTimeout = 0;
    /**
     * JIM内部线程池
     */
    protected SynThreadPoolExecutor jimExecutor;

    /**
     *
     */
    protected SynThreadPoolExecutor jimExpandExecutor;
    /**
     * 群组绑定监听器
     */
    protected ImGroupListener imGroupListener;
    /**
     * 用户绑定监听器
     */
    protected ImUserListener imUserListener;

    /**
     *
     */
    private RedisConfiguration redisConfiguration;
    /**
     *
     */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    public ImConfig() {
        this(null);
    }

    public ImConfig(SynThreadPoolExecutor jimExecutor) {
        this.jimExecutor = jimExecutor;
        if (this.jimExecutor == null) {
            LinkedBlockingQueue<Runnable> timQueue = new LinkedBlockingQueue<>(1000000);
            this.jimExecutor = new SynThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE * 2, 1, timQueue,
                DefaultThreadFactory.getInstance(ImConst.JIM, Thread.NORM_PRIORITY), ImConst.JIM);
            this.jimExecutor.prestartAllCoreThreads();
        }
        if (this.jimExpandExecutor == null) {
            LinkedBlockingQueue<Runnable> timQueue = new LinkedBlockingQueue<>(1000000);
            this.jimExpandExecutor = new SynThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE * 2, 1, timQueue,
                DefaultThreadFactory.getInstance(ImConst.JIM + "_user_status", Thread.NORM_PRIORITY),
                ImConst.JIM + "_user_status");
            this.jimExpandExecutor.prestartAllCoreThreads();
        }
    }

    /**
     * 获取ImHandler对象
     *
     * @return
     * @author: WChao
     */
    public abstract ImHandler getImHandler();

    /**
     * 获取ImListener对象
     *
     * @return
     * @author: WChao
     */
    public abstract ImListener getImListener();

    public static class Global {

        private static ImConfig global;

        public static <C extends ImConfig> C get() {
            return (C)global;
        }

        public static <C extends ImConfig> C set(C c) {
            global = (ImConfig)c;
            return (C)global;
        }
    }

    public abstract static class Builder<T extends ImConfig, B extends Builder<T, B>> {

        /**
         * IP地址
         */
        protected String bindIp = "0.0.0.0";
        /**
         * 监听端口
         */
        protected Integer bindPort = 0;
        /**
         * 配置名称
         */
        protected String name = "JIM";

        /**
         * 默认的接收数据的buffer size
         */
        protected int readBufferSize = 1024 * 2;

        /**
         * 心跳包发送时长heartbeatTimeout/2
         */
        protected long heartbeatTimeout = 0;
        // /**
        // * 集群配置 如果此值不为null，就表示要集群
        // */
        // protected ImCluster cluster;

        /**
         * 集群配置 如果此值不为null，就表示要集群
         */
        protected List<ImCluster> clusters = new ArrayList<>();
        /**
         * SSL配置
         */
        protected SslConfig sslConfig;

        /**
         * 连接微服务
         */
        protected ApiServerConfig apiServerConfig;
        /**
         * 群组绑定监听器
         */
        protected ImGroupListener imGroupListener;
        /**
         * 用户绑定监听器
         */
        protected ImUserListener imUserListener;

        /**
         *
         */
        private RedisConfiguration redisConfiguration;

        private final B theBuilder = this.getThis();

        /**
         * 供子类获取自身builder抽象类;
         *
         * @return
         */
        protected abstract B getThis();

        public B bindIp(String bindIp) {
            this.bindIp = bindIp;
            return theBuilder;
        }

        public B bindPort(Integer bindPort) {
            this.bindPort = bindPort;
            return theBuilder;
        }

        public B name(String name) {
            this.name = name;
            return theBuilder;
        }

        public B heartbeatTimeout(long heartbeatTimeout) {
            this.heartbeatTimeout = heartbeatTimeout;
            return theBuilder;
        }

        public B readBufferSize(int readBufferSize) {
            this.readBufferSize = readBufferSize;
            return theBuilder;
        }

        // public B cluster(ImCluster cluster) {
        // this.cluster = cluster;
        // return theBuilder;
        // }

        public B clusters(List<ImCluster> clusters) {
            this.clusters.addAll(clusters);
            return theBuilder;
        }

        public B clusters(ImCluster cluster) {
            this.clusters.add(cluster);
            return theBuilder;
        }

        public B sslConfig(SslConfig sslConfig) {
            this.sslConfig = sslConfig;
            return theBuilder;
        }

        public B apiServerConfig(ApiServerConfig apiServerConfig) {
            this.apiServerConfig = apiServerConfig;
            return theBuilder;
        }

        public B groupListener(ImGroupListener imGroupListener) {
            this.imGroupListener = imGroupListener;
            return theBuilder;
        }

        public B userListener(ImUserListener imUserListener) {
            this.imUserListener = imUserListener;
            return theBuilder;
        }

        public B redisConfiguration(RedisConfiguration redisConfiguration) {
            this.redisConfiguration = redisConfiguration;
            return theBuilder;
        }

        /**
         * 配置构建接口
         *
         * @return
         * @throws Exception
         */
        public abstract T build();
    }

    public RedisConfiguration getRedisConfiguration() {
        return redisConfiguration;
    }

    public void setRedisConfiguration(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public Integer getBindPort() {
        return bindPort;
    }

    public void setBindPort(Integer bindPort) {
        this.bindPort = bindPort;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        tioConfig.setReadBufferSize(readBufferSize);
    }

    public TioConfig getTioConfig() {
        return tioConfig;
    }

    public void setTioConfig(TioConfig tioConfig) {
        this.tioConfig = tioConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
        tioConfig.setHeartbeatTimeout(heartbeatTimeout);
    }

    public SynThreadPoolExecutor getJimExecutor() {
        return jimExecutor;
    }

    public ImGroupListener getImGroupListener() {
        return imGroupListener;
    }

    public void setImGroupListener(ImGroupListener imGroupListener) {
        this.imGroupListener = imGroupListener;
        if (imGroupListener != null) {
            this.tioConfig.setGroupListener(new ImGroupListenerAdapter(this.imGroupListener));
        }
    }

    public ImUserListener getImUserListener() {
        return imUserListener;
    }

    public void setImUserListener(ImUserListener imUserListener) {
        this.imUserListener = imUserListener;
    }

    // public ImCluster getCluster() {
    // return cluster;
    // }
    //
    // public void setCluster(ImCluster cluster) {
    // this.cluster = cluster;
    // }

    public List<ImCluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<ImCluster> clusters) {
        this.clusters = clusters;
    }

    public SslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
        this.tioConfig.setSslConfig(sslConfig);
    }

    public ApiServerConfig getApiServerConfig() {
        return apiServerConfig;
    }

    public void setApiServerConfig(ApiServerConfig apiServerConfig) {
        this.apiServerConfig = apiServerConfig;
    }

    public String toBindAddressString() {
        StringBuilder builder = new StringBuilder();
        builder.append(bindIp).append(":").append(bindPort);
        return builder.toString();
    }

    public SynThreadPoolExecutor getJimExpandExecutor() {
        return jimExpandExecutor;
    }

    public void setJimExpandExecutor(SynThreadPoolExecutor jimExpandExecutor) {
        this.jimExpandExecutor = jimExpandExecutor;
    }

    static {
        JimBanner banner = new JimBanner();
        banner.printBanner(System.out);
        PrintStream ps = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                if (filterLog(x)) {
                    return;
                }
                super.println(x);
            }

            @Override
            public void print(String s) {
                if (filterLog(s)) {
                    return;
                }
                super.print(s);
            }
        };
        System.setOut(ps);
    }

    private static boolean filterLog(String x) {
        if (x.contains("---------------------------------------------------------------------------------------")) {
            return true;
        }
        return false;
    }

}
