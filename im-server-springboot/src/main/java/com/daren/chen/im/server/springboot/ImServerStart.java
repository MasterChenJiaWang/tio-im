package com.daren.chen.im.server.springboot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tio.core.ssl.SslConfig;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cache.redis.RedisTemplateUtils;
import com.daren.chen.im.core.cache.redis.RedissonTemplate;
import com.daren.chen.im.core.cluster.ImCluster;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServer;
import com.daren.chen.im.server.cluster.redis.RedisCluster;
import com.daren.chen.im.server.cluster.redis.RedisClusterConfig;
import com.daren.chen.im.server.command.CommandManager;
import com.daren.chen.im.server.command.handler.AddFriendReqHandler;
import com.daren.chen.im.server.command.handler.AuthReqHandler;
import com.daren.chen.im.server.command.handler.ChatAckReqHandler;
import com.daren.chen.im.server.command.handler.ChatReqHandler;
import com.daren.chen.im.server.command.handler.DeleteFriendReqHandler;
import com.daren.chen.im.server.command.handler.ExitGroupReqHandler;
import com.daren.chen.im.server.command.handler.GetUserOnlineStatusReqHandler;
import com.daren.chen.im.server.command.handler.HandshakeReqHandler;
import com.daren.chen.im.server.command.handler.JoinGroupReqHandler;
import com.daren.chen.im.server.command.handler.LoginReqHandler;
import com.daren.chen.im.server.command.handler.MsgNoticeReqHandler;
import com.daren.chen.im.server.command.handler.NoticeAckReqHandler;
import com.daren.chen.im.server.command.handler.NoticeOfflineReqHandler;
import com.daren.chen.im.server.command.handler.ReceiveMsgNoticeReqHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.config.PropertyImServerConfigBuilder;
import com.daren.chen.im.server.springboot.cluster.RedisAddTopicMessageListener;
import com.daren.chen.im.server.springboot.cluster.RedisSubMessageListener;
import com.daren.chen.im.server.springboot.command.MyWsHandshakeProcessor;
import com.daren.chen.im.server.springboot.config.JimConfig;
import com.daren.chen.im.server.springboot.handler.MyImServerHandler;
import com.daren.chen.im.server.springboot.helper.redis.ChatRedisMessageHelper;
import com.daren.chen.im.server.springboot.helper.redis.LocalCacheUtils;
import com.daren.chen.im.server.springboot.listener.MyImGroupListener;
import com.daren.chen.im.server.springboot.listener.MyImServerListener;
import com.daren.chen.im.server.springboot.listener.MyImUserListener;
import com.daren.chen.im.server.springboot.service.AsyncChatMessageServiceProcessor;
import com.daren.chen.im.server.springboot.service.AuthServiceProcessor;
import com.daren.chen.im.server.springboot.service.FriendServiceProcessor;
import com.daren.chen.im.server.springboot.service.GroupServiceProcessor;
import com.daren.chen.im.server.springboot.service.LoginServiceProcessor;
import com.daren.chen.im.server.springboot.service.NoticeOfflineServiceProcessor;
import com.daren.chen.im.server.springboot.service.NoticeServiceProcessor;
import com.daren.chen.im.server.springboot.service.ReceiveNoticeServiceProcessor;
import com.daren.chen.im.server.springboot.service.UserServiceProcessor;
import com.daren.chen.im.server.springboot.utils.ApplicationContextProvider;

import cn.hutool.core.collection.CollectionUtil;

/**
 * IM服务端DEMO演示启动类;
 *
 * @author WChao
 * @date 2018-04-05 23:50:25
 */
@Component
public class ImServerStart {

    @Autowired
    private JimConfig jimConfig;

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(ImServerStart.class);

    @PostConstruct
    public void start() {
        try {
            // String configPath = initConfigPath();
            ImServerConfig imServerConfig =
                new PropertyImServerConfigBuilder(jimConfig).serverListener(new MyImServerListener())
                    .messageListener(new RedisSubMessageListener()).serverHandler(new MyImServerHandler()).build();
            // 初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
            initSsl(imServerConfig);
            // 设置群组监听器，非必须，根据需要自己选择性实现;
            imServerConfig.setImGroupListener(new MyImGroupListener());
            // 设置绑定用户监听器，非必须，根据需要自己选择性实现;
            imServerConfig.setImUserListener(new MyImUserListener());
            // 持久化
            imServerConfig.setMessageHelper(new ChatRedisMessageHelper());
            // 选择jedistemplate
            RedisTemplateUtils.setCluster(jimConfig.getRedis().isCluster());
            // 初始化
            LocalCacheUtils.me();
            //
            serverCluster(imServerConfig);
            //
            JimServer jimServer = new JimServer(imServerConfig);

            /***************** start 以下处理器根据业务需要自行添加与扩展，每个Command都可以添加扩展,此处为demo中处理 **********************************/

            HandshakeReqHandler handshakeReqHandler =
                CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
            if (handshakeReqHandler == null) {
                throw new RuntimeException("心跳指令对象为空!");
            }
            // 登录请求指令;
            handshakeReqHandler.addMultiProtocolProcessor(new MyWsHandshakeProcessor());
            LoginReqHandler loginReqHandler =
                CommandManager.getCommand(Command.COMMAND_LOGIN_REQ, LoginReqHandler.class);
            if (loginReqHandler == null) {
                throw new RuntimeException("登录对象为空!");
            }
            // 登录业务处理器
            loginReqHandler.setSingleProcessor(new LoginServiceProcessor());

            // 鉴权请求指令;
            AuthReqHandler authReqHandler = CommandManager.getCommand(Command.COMMAND_AUTH_REQ, AuthReqHandler.class);
            if (authReqHandler == null) {
                throw new RuntimeException("鉴权信息对象为空!");
            }
            // 鉴权业务处理器;
            authReqHandler.setSingleProcessor(new AuthServiceProcessor());

            // 聊天请求指令
            ChatReqHandler chatReqHandler = CommandManager.getCommand(Command.COMMAND_CHAT_REQ, ChatReqHandler.class);
            if (chatReqHandler == null) {
                throw new RuntimeException("聊天对象为空!");
            }
            // 聊天业务处理器;
            chatReqHandler.setSingleProcessor(new AsyncChatMessageServiceProcessor());

            // 添加好友请求指令
            AddFriendReqHandler addFriendReqHandler =
                CommandManager.getCommand(Command.COMMAND_ADD_FRIEND_REQ, AddFriendReqHandler.class);
            if (addFriendReqHandler == null) {
                throw new RuntimeException("新增好友对象为空!");
            }
            // 添加好友业务处理器
            addFriendReqHandler.setSingleProcessor(new FriendServiceProcessor());

            // 删除好友请求指令
            DeleteFriendReqHandler deleteFriendReqHandler =
                CommandManager.getCommand(Command.COMMAND_DELETE_FRIEND_REQ, DeleteFriendReqHandler.class);
            if (deleteFriendReqHandler == null) {
                throw new RuntimeException("删除好友对象为空!");
            }
            // 删除好友业务处理器
            deleteFriendReqHandler.setSingleProcessor(new FriendServiceProcessor());

            // 加入群组请求指令
            JoinGroupReqHandler joinGroupReqHandler =
                CommandManager.getCommand(Command.COMMAND_JOIN_GROUP_REQ, JoinGroupReqHandler.class);
            if (joinGroupReqHandler == null) {
                throw new RuntimeException("加入群组对象为空!");
            }
            // 加入群组业务处理器
            joinGroupReqHandler.setSingleProcessor(new GroupServiceProcessor());

            // 退出群组请求指令
            ExitGroupReqHandler exitGroupReqHandler =
                CommandManager.getCommand(Command.COMMAND_EXIT_GROUP_REQ, ExitGroupReqHandler.class);
            if (exitGroupReqHandler == null) {
                throw new RuntimeException("退出群组对象为空!");
            }
            // 退出群组业务处理器
            exitGroupReqHandler.setSingleProcessor(new GroupServiceProcessor());

            // 获取用户在线状态指令
            GetUserOnlineStatusReqHandler getUserOnlineStatusReqHandler =
                CommandManager.getCommand(Command.GET_USER_ONLINE_STATUS_REQ, GetUserOnlineStatusReqHandler.class);
            if (getUserOnlineStatusReqHandler == null) {
                throw new RuntimeException("获取用户在线状态对象为空!");
            }
            // 获取用户在线状态业务处理器
            getUserOnlineStatusReqHandler.setSingleProcessor(new UserServiceProcessor());

            // 获取消息通知
            MsgNoticeReqHandler msgNoticeReqHandler =
                CommandManager.getCommand(Command.MSG_NOTICE_REQ, MsgNoticeReqHandler.class);
            if (msgNoticeReqHandler == null) {
                throw new RuntimeException("获取消息通知对象为空!");
            }
            // 获取消息通知业务处理器
            msgNoticeReqHandler.setSingleProcessor(new NoticeServiceProcessor());

            // 获取用户下线通知指令
            NoticeOfflineReqHandler noticeOfflineReqHandler =
                CommandManager.getCommand(Command.NOTICE_OFFLINE_REQ, NoticeOfflineReqHandler.class);
            if (noticeOfflineReqHandler == null) {
                throw new RuntimeException("获取下线消息通知对象为空!");
            }
            // 获取用户下线通知业务处理器
            noticeOfflineReqHandler.setSingleProcessor(new NoticeOfflineServiceProcessor());

            // 获取用户申请通知
            ReceiveMsgNoticeReqHandler receiveMsgNoticeReqHandler =
                CommandManager.getCommand(Command.RECEIVE_MSG_NOTICE_REQ, ReceiveMsgNoticeReqHandler.class);
            if (receiveMsgNoticeReqHandler == null) {
                throw new RuntimeException("获取申请消息通知对象为空!");
            }
            // 获取用户申请消息通知业务处理器
            receiveMsgNoticeReqHandler.setSingleProcessor(new ReceiveNoticeServiceProcessor());

            //
            // 消息ack通知
            ChatAckReqHandler chatAckReqHandler =
                CommandManager.getCommand(Command.CHAT_ACK_REQ, ChatAckReqHandler.class);
            if (chatAckReqHandler == null) {
                throw new RuntimeException("消息ack对象为空!");
            }
            // 获取用户申请消息通知业务处理器
            chatAckReqHandler.setSingleProcessor(new AsyncChatMessageServiceProcessor());
            //
            // 通知ack通知
            NoticeAckReqHandler noticeAckReqHandler =
                CommandManager.getCommand(Command.NOTICE_ACK_REQ, NoticeAckReqHandler.class);
            if (noticeAckReqHandler == null) {
                throw new RuntimeException("通知ack对象为空!");
            }
            // 获取用户申请消息通知业务处理器
            noticeAckReqHandler.setSingleProcessor(new AsyncChatMessageServiceProcessor());

            /*****************
             * end
             *******************************************************************************************/
            jimServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param imServerConfig
     * @throws Exception
     */
    private void serverCluster(ImServerConfig imServerConfig) throws Exception {
        // 集群部署
        if (jimConfig.getCluster()) {
            // 监听添加topicName
            RedisCluster addTopicNameRedisCluster =
                new RedisCluster(RedisClusterConfig.newInstance(ImConst.ADD_TOPIC_NAME,
                    RedissonTemplate.me().getRedissonClient(), new RedisAddTopicMessageListener()));
            //
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            // 注册主题
            String topicSuffix = jimConfig.getTopicSuffix();
            if (StringUtils.isNotBlank(topicSuffix)) {
                topicSuffix = topicSuffix + ImConst.Topic.JIM_CLUSTER_TOPIC;
                boolean b = messageHelper.addTopicName(topicSuffix);
                if (b) {
                    ImPacket imPacket =
                        new ImPacket(Command.COMMAND_UNKNOW, JsonKit.toJSONBytesEnumNoUsingName(topicSuffix));
                    addTopicNameRedisCluster.clusterToIp("0.0.0.0", imPacket);
                }
            }
            // 添加监听器
            MessageListener messageListener = imServerConfig.getMessageListener();
            List<String> allTopicName = messageHelper.findAllTopicName();
            if (CollectionUtil.isNotEmpty(allTopicName) && messageListener != null) {
                List<ImCluster> imClusters = new ArrayList<>(allTopicName.size());
                for (String topicName : allTopicName) {
                    RedisCluster redisCluster = new RedisCluster(RedisClusterConfig.newInstance(topicName,
                        RedissonTemplate.me().getRedissonClient(), messageListener));
                    if (!topicName.equals(topicSuffix)) {
                        imClusters.add(redisCluster);
                    }
                }
                imServerConfig.setClusters(imClusters);
            }
        }
    }

    /**
     *
     */
    private static String initConfigPath() {
        Environment env = ApplicationContextProvider.getBean(Environment.class);
        String profile = env.getProperty("spring.profiles.active");
        System.setProperty("spring.profiles.active", StringUtils.isBlank(profile) ? "dev" : profile);
        //
        return "config/" + profile + "/jim.properties";
    }

    /**
     * 开启SSL之前，你要保证你有SSL证书哦！
     *
     * @param imServerConfig
     * @throws Exception
     */
    private void initSsl(ImServerConfig imServerConfig) throws Exception {
        // 开启SSL
        if (ImServerConfig.ON.equals(imServerConfig.getIsSSL())) {
            String keyStorePath = jimConfig.getSsl().getKeyStorePath();
            String keyStorePwd = jimConfig.getSsl().getKeyStorePwd();
            if (StringUtils.isNotBlank(keyStorePath) && StringUtils.isNotBlank(keyStorePath)) {
                SslConfig sslConfig = SslConfig.forServer(keyStorePath, keyStorePath, keyStorePwd);
                imServerConfig.setSslConfig(sslConfig);
            }
        }
    }

}
