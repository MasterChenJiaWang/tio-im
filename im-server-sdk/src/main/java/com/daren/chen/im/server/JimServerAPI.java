package com.daren.chen.im.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.ChannelContextFilter;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.lock.SetWithLock;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cluster.ImCluster;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImUserListener;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.ImClientNode;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.common.StatisticsUtils;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.google.common.collect.Lists;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ExecutorBuilder;

/**
 * 版本: [1.0] 功能说明: JIM
 *
 * @author : WChao 创建时间: 2017年9月22日 上午9:07:18
 */
public class JimServerAPI implements ImConst {

    public static ImConfig imConfig = ImConfig.Global.get();

    private static final Logger log = LoggerFactory.getLogger(JimServerAPI.class);

    /**
     *
     */
    public static final ExecutorService JPUSH_EXECUTOR =
        ExecutorBuilder.create().setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2)
            .setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4)
            .setWorkQueue(new LinkedBlockingQueue<>(1000000)).build();

    /**
     * 根据群组ID获取该群组下所有Channel
     *
     * @param groupId
     *            群组ID
     * @return 群组下所有通道集合
     */
    public static List<ImChannelContext> getByGroup(String groupId) {
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByGroup(imConfig.getTioConfig(), groupId);
        List<ImChannelContext> imChannelContextList = convertChannelToImChannel(channelContextSetWithLock);
        if (CollectionUtils.isEmpty(imChannelContextList)) {
            // log.info("{}, there is no bind channel with groupId[{}]", imConfig.getName(), groupId);
            return imChannelContextList;
        }
        return imChannelContextList;
    }

    /**
     * 根据用户ID获取用户下所有channel
     *
     * @param userId
     *            用户ID
     * @return 用户绑定所有通道集合
     */
    public static List<ImChannelContext> getByUserId(String userId) {
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(imConfig.getTioConfig(), userId);
        List<ImChannelContext> imChannelContextList = convertChannelToImChannel(channelContextSetWithLock);
        if (CollectionUtils.isEmpty(imChannelContextList)) {
            // log.info("{}, there is no bind channel with userId[{}]", imConfig.getName(), userId);
            return imChannelContextList;
        }
        return imChannelContextList;
    }

    /**
     * 根据指定IP获取所有绑定的channel
     *
     * @param ip
     *            指定IP地址
     * @return 所有绑定ip的通道集合
     */
    public static List<ImChannelContext> getByIp(String ip) {
        SetWithLock<ChannelContext> channelContextSetWithLock =
            imConfig.getTioConfig().ips.clients(imConfig.getTioConfig(), ip);
        List<ImChannelContext> imChannelContextList = convertChannelToImChannel(channelContextSetWithLock);
        if (CollectionUtils.isEmpty(imChannelContextList)) {
            // log.info("{}, there is no bind channel with ip[{}]", imConfig.getName(), ip);
            return imChannelContextList;
        }
        return imChannelContextList;
    }

    /**
     * 功能描述：[发送到群组(所有不同协议端)]
     *
     * @param groupId
     *            群组ID
     * @param packet
     *            消息包
     */
    public static Boolean sendToGroup(String groupId, ImPacket packet) {
        List<ImChannelContext> imChannelContextList = getByGroup(groupId);
        if (CollectionUtils.isEmpty(imChannelContextList)) {
            sendGroupByCluster(groupId, packet);
            return true;
        }
        try {
            imChannelContextList.forEach(imChannelContext -> {
                send(imChannelContext, packet);
            });
            return true;
        } finally {
            sendGroupByCluster(groupId, packet);
        }
    }

    /**
     *
     * @param groupId
     * @param packet
     */
    private static void sendGroupByCluster(String groupId, ImPacket packet) {
        List<ImCluster> clusters = imConfig.getClusters();
        if (CollectionUtil.isNotEmpty(clusters) && !packet.isFromCluster()) {
            for (ImCluster cluster : clusters) {
                cluster.clusterToGroup(groupId, packet);
            }
        }
    }

    /**
     *
     * @param userId
     * @param packet
     */
    private static void sendUserByCluster(String userId, ImPacket packet) {
        List<ImCluster> clusters = imConfig.getClusters();
        if (CollectionUtil.isNotEmpty(clusters) && !packet.isFromCluster()) {
            for (ImCluster cluster : clusters) {
                cluster.clusterToUser(userId, packet);
            }
        }
    }

    private static void sendIpByCluster(String ip, ImPacket packet) {
        List<ImCluster> clusters = imConfig.getClusters();
        if (CollectionUtil.isNotEmpty(clusters) && !packet.isFromCluster()) {
            for (ImCluster cluster : clusters) {
                cluster.clusterToIp(ip, packet);
            }
        }
    }

    /**
     * @param channelContext
     *            发送消息的上下文 需要过滤自己
     * @param groupId
     * @param packet
     * @return
     */
    public static Boolean sendToGroup(ImChannelContext channelContext, String groupId, ImPacket packet) {
        List<ImChannelContext> imChannelContextList = getByGroup(groupId);
        if (CollectionUtils.isEmpty(imChannelContextList)) {
            // ImCluster cluster = imConfig.getCluster();
            // if (cluster != null && !packet.isFromCluster()) {
            // cluster.clusterToGroup(groupId, packet);
            // }
            sendGroupByCluster(groupId, packet);
            return true;
        }
        String id = channelContext == null ? "" : channelContext.getId();
        try {
            imChannelContextList.forEach(imChannelContext -> {
                if (!id.equals(imChannelContext.getId())) {
                    send(imChannelContext, packet);
                }

            });
            return true;
        } finally {
            // ImCluster cluster = imConfig.getCluster();
            // if (Objects.nonNull(cluster) && !packet.isFromCluster()) {
            // cluster.clusterToGroup(groupId, packet);
            // }
            sendGroupByCluster(groupId, packet);
        }
    }

    public static Boolean sendToGroupByIds(ImChannelContext channelContext, String groupId, List<String> userIds,
        ImPacket packet) {
        List<ImChannelContext> imChannelContextList = getByGroup(groupId);
        if (CollectionUtils.isEmpty(imChannelContextList) || CollectionUtils.isEmpty(userIds)) {
            // ImCluster cluster = imConfig.getCluster();
            // if (cluster != null && !packet.isFromCluster()) {
            // cluster.clusterToGroup(groupId, packet);
            // }
            sendGroupByCluster(groupId, packet);
            return true;
        }
        String id = channelContext == null ? "" : channelContext.getId();
        try {
            if (CollectionUtils.isEmpty(userIds)) {
                return true;
            }
            for (String userId : userIds) {
                List<ImChannelContext> byUserId = getByUserId(userId);
                if (CollectionUtils.isEmpty(byUserId)) {
                    continue;
                }
                for (ImChannelContext imChannelContext : byUserId) {
                    if (!id.equals(imChannelContext.getId())) {
                        send(imChannelContext, packet);
                    }
                }
            }
            return true;
        } finally {
            // ImCluster cluster = imConfig.getCluster();
            // if (Objects.nonNull(cluster) && !packet.isFromCluster()) {
            // cluster.clusterToGroup(groupId, packet);
            // }
            sendGroupByCluster(groupId, packet);
        }
    }

    /**
     * 发送到指定通道
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param imPacket
     *            消息包
     */
    public static boolean send(ImChannelContext imChannelContext, ImPacket imPacket) {
        if (imChannelContext == null) {
            return false;
        }
        ImPacket convertPacket = convertPacket(imChannelContext, imPacket);
        if (convertPacket == null) {
            return false;
        }

        try {
            if (Command.COMMAND_HEARTBEAT_REQ != convertPacket.getCommand()) {
                Command command = convertPacket.getCommand();
                int number = 0;
                if (command != null) {
                    number = command.getNumber();
                }

                byte[] body1 = convertPacket.getBody();
                if (body1 != null) {
                    String userId = imChannelContext.getUserId();
                    Object msgNoticeReq = JsonKit.toBean(body1, Object.class);
                    if (msgNoticeReq != null) {
                        log.info("上下文ID [{}] 用户ID [{}]   发送的消息：cmd 【{}】 发送内容 【{}】", imChannelContext.getId(), userId,
                            number, JSON.toJSONString(msgNoticeReq));
                    }
                }
                // TODO
                if (Command.ADMIN_MSG_REQ != convertPacket.getCommand()) {
                    StatisticsUtils.addTask(new StatisticsUtils.AdminTask(imChannelContext, convertPacket));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Tio.send(imChannelContext.getTioChannelContext(), convertPacket);
    }

    /**
     * 阻塞发送（确认把packet发送到对端后再返回）
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param imPacket
     *            消息包
     * @return
     */
    public static boolean bSend(ImChannelContext imChannelContext, ImPacket imPacket) {
        if (imChannelContext == null) {
            return false;
        }
        ImPacket convertPacket = convertPacket(imChannelContext, imPacket);
        if (convertPacket == null) {
            return false;
        }
        return Tio.bSend(imChannelContext.getTioChannelContext(), convertPacket);
    }

    /**
     * 发送到指定用户(所有不同协议端)
     *
     * @param userId
     *            用户ID
     * @param packet
     *            消息包
     */
    public static boolean sendToUser(String userId, ImPacket packet) {
        List<ImChannelContext> imChannelContexts = getByUserId(userId);
        if (CollectionUtils.isEmpty(imChannelContexts)) {
            // ImCluster cluster = imConfig.getCluster();
            // if (cluster != null && !packet.isFromCluster()) {
            // cluster.clusterToUser(userId, packet);
            // }
            sendUserByCluster(userId, packet);
            return true;
        }
        try {
            imChannelContexts.forEach(imChannelContext -> {
                send(imChannelContext, packet);
            });
            return true;
        } catch (Exception e) {
            log.error("an exception occurred when sending to the specified user", e);
            return false;
        } finally {
            // ImCluster cluster = imConfig.getCluster();
            // if (Objects.nonNull(cluster) && !packet.isFromCluster()) {
            // cluster.clusterToUser(userId, packet);
            // }
            sendUserByCluster(userId, packet);
        }
    }

    /**
     * 发送到指定ip对应的集合
     *
     * @param ip
     *            客户端IP地址
     * @param packet
     *            消息包
     */
    public static Boolean sendToIp(String ip, ImPacket packet) {
        return sendToIp(ip, packet, null);
    }

    /**
     * 发送到指定ip对应的集合
     *
     * @param ip
     *            客户端IP地址
     * @param packet
     *            消息包
     * @param channelContextFilter
     *            通道过滤器
     */
    public static Boolean sendToIp(String ip, ImPacket packet, ChannelContextFilter channelContextFilter) {
        List<ImChannelContext> imChannelContexts = getByIp(ip);
        if (CollectionUtils.isEmpty(imChannelContexts)) {
            // ImCluster cluster = imConfig.getCluster();
            // if (cluster != null && !packet.isFromCluster()) {
            // cluster.clusterToIp(ip, packet);
            // }
            sendIpByCluster(ip, packet);
            return true;
        }
        try {
            imChannelContexts.forEach(imChannelContext -> {
                send(imChannelContext, packet);
            });
            return true;
        } catch (Exception e) {
            log.error("an exception occurred when sending to the specified ip:[{}]", ip, e);
            return false;
        } finally {
            // ImCluster cluster = imConfig.getCluster();
            // if (Objects.nonNull(cluster) && !packet.isFromCluster()) {
            // cluster.clusterToIp(ip, packet);
            // }
            sendIpByCluster(ip, packet);
        }
    }

    /**
     * @param imChannelContext
     * @param token
     * @return
     */
    public static boolean bindUserToken(ImChannelContext imChannelContext, String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        Tio.bindToken(imChannelContext.getTioChannelContext(), token);
        return true;
    }

    /**
     * 绑定用户(如果配置了回调函数执行回调)
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param userId
     *            用户ID
     */
    public static boolean bindUser(ImChannelContext imChannelContext, String userId) {
        User user = imChannelContext.getSessionContext().getImClientNode().getUser();
        if (Objects.isNull(user)) {
            user = User.newBuilder().userId(userId).status(UserStatusType.ONLINE.getStatus()).build();
        }
        return bindUser(imChannelContext, user);
    }

    /**
     * 绑定用户(如果配置了回调函数执行回调)
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param user
     *            绑定用户信息
     */
    public static boolean bindUser(ImChannelContext imChannelContext, User user) {
        if (Objects.isNull(user) || StringUtils.isEmpty(user.getUserId())) {
            log.error("user or userId is null");
            return false;
        }
        String userId = user.getUserId();
        Tio.bindUser(imChannelContext.getTioChannelContext(), userId);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(imConfig.getTioConfig(), userId);
        ReadLock lock = channelContextSetWithLock.getLock().readLock();
        try {
            lock.lock();
            if (CollectionUtils.isEmpty(channelContextSetWithLock.getObj())) {
                return false;
            }
            ImClientNode imClientNode = imChannelContext.getSessionContext().getImClientNode();
            if (imClientNode != null) {
                imClientNode.setUser(user);
            }
            ImUserListener imUserListener = imConfig.getImUserListener();
            if (Objects.nonNull(imUserListener)) {
                imUserListener.onAfterBind(imChannelContext, user);
            }
        } catch (ImException e) {
            log.error(e.getMessage(), e);
            return false;
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 解除userId的绑定。一般用于多地登录，踢掉前面登录的场景
     *
     * @param userId
     *            解绑用户ID
     * @author: WChao
     */
    public static boolean unbindUser(String userId) {
        return unbindUser(User.newBuilder().userId(userId).build());
    }

    /**
     * 解除userId的绑定。一般用于多地登录，踢掉前面登录的场景
     *
     * @param user
     *            解绑用户信息
     * @author: WChao
     */
    public static boolean unbindUser(User user) {
        if (Objects.isNull(user) || StringUtils.isEmpty(user.getUserId())) {
            log.error("user or userId is null");
            return false;
        }
        String userId = user.getUserId();
        TioConfig tioConfig = imConfig.getTioConfig();
        SetWithLock<ChannelContext> userChannels = Tio.getByUserid(tioConfig, userId);
        if (userChannels == null) {
            return false;
        }
        Set<ChannelContext> channelContexts = userChannels.getObj();
        if (channelContexts.isEmpty()) {
            return true;
        }
        ReadLock readLock = userChannels.getLock().readLock();
        try {
            readLock.lock();
            for (ChannelContext channelContext : channelContexts) {
                ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
                ImUserListener imUserListener = imConfig.getImUserListener();
                if (Objects.isNull(imUserListener)) {
                    continue;
                }
                User existUser = imChannelContext.getSessionContext().getImClientNode().getUser();
                imUserListener.onAfterUnbind(imChannelContext, existUser);
            }
            Tio.unbindUser(tioConfig, userId);
        } catch (ImException e) {
            log.error("unbind user failed", e);
            return false;
        } finally {
            readLock.unlock();
        }
        return true;
    }

    /**
     * 绑定群组(如果配置了群组监听器,执行回调)
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param groupId
     *            绑定群组ID
     */
    public static boolean bindGroup(ImChannelContext imChannelContext, String groupId) {
        return bindGroup(imChannelContext, Group.newBuilder().groupId(groupId).build());
    }

    /**
     * 绑定群组(如果配置了群组监听器,执行回调)
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param group
     *            绑定群组对象
     */
    public static boolean bindGroup(ImChannelContext imChannelContext, Group group) {
        try {
            String groupId = group.getGroupId();
            if (StringUtils.isEmpty(groupId)) {
                log.error("groupId is null");
                return false;
            }
            Tio.bindGroup(imChannelContext.getTioChannelContext(), group.getGroupId());
        } catch (Exception e) {
            log.error("an exception occurred in the binding group", e);
            return false;
        }
        return true;
    }

    /**
     * 与指定组解除绑定关系
     *
     * @param groupId
     *            解绑群组ID
     * @param imChannelContext
     *            IM通道上下文
     * @author WChao
     */
    public static boolean unbindGroup(String groupId, ImChannelContext imChannelContext) {
        try {
            if (StringUtils.isEmpty(groupId)) {
                log.error("groupId is null");
                return false;
            }
            Tio.unbindGroup(groupId, imChannelContext.getTioChannelContext());
        } catch (Exception e) {
            log.error("an exception occurred in the unBinding group", e);
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    public static List<ImChannelContext> findAll() {
        SetWithLock<ChannelContext> all = Tio.getAll(imConfig.getTioConfig());
        if (all == null) {
            return new ArrayList<>();
        }
        return convertChannelToImChannel(all);
    }

    /**
     * 与所有组解除解绑关系
     *
     * @param imChannelContext
     *            IM通道上下文
     * @author WChao
     */
    public static boolean unbindGroup(ImChannelContext imChannelContext) {
        try {
            Tio.unbindGroup(imChannelContext.getTioChannelContext());
        } catch (Exception e) {
            log.error("an exception occurred in the unBinding group", e);
            return false;
        }
        return true;
    }

    /**
     * 将指定用户从指定群组解除绑定
     *
     * @param userId
     *            用户ID
     * @param groupId
     *            群组ID
     */
    public static boolean unbindGroup(String userId, String groupId) {
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
            log.error("groupId or userId is null");
            return false;
        }
        try {
            Tio.unbindGroup(imConfig.getTioConfig(), userId, groupId);
        } catch (Exception e) {
            log.error("an exception occurred in the unBinding group", e);
            return false;
        }
        return true;
    }

    /**
     * 移除用户, 和close方法一样，只不过不再进行重连等维护性的操作
     *
     * @param userId
     *            用户ID
     * @param remark
     *            移除原因描述
     */
    public static void remove(String userId, String remark) {
        SetWithLock<ChannelContext> userChannelContexts = Tio.getByUserid(imConfig.getTioConfig(), userId);
        if (userChannelContexts == null) {
            return;
        }
        Set<ChannelContext> channels = userChannelContexts.getObj();
        if (channels.isEmpty()) {
            return;
        }
        ReadLock readLock = userChannelContexts.getLock().readLock();
        try {
            readLock.lock();
            for (ChannelContext channelContext : channels) {
                ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
                remove(imChannelContext, remark);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 移除指定channel, 和close方法一样，只不过不再进行重连等维护性的操作
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param remark
     *            移除描述
     */
    public static void remove(ImChannelContext imChannelContext, String remark) {
        Tio.remove(imChannelContext.getTioChannelContext(), remark);
    }

    /**
     * 关闭连接
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param remark
     *            移除描述
     */
    public static void close(ImChannelContext imChannelContext, String remark) {
        Tio.close(imChannelContext.getTioChannelContext(), remark);
    }

    /**
     * 转换Channel为ImChannel
     *
     * @param channelContextSetWithLock
     *            channel上下文对象
     * @return
     */
    private static List<ImChannelContext>
        convertChannelToImChannel(SetWithLock<ChannelContext> channelContextSetWithLock) {
        List<ImChannelContext> imChannelContexts = Lists.newArrayList();
        if (Objects.isNull(channelContextSetWithLock)) {
            return imChannelContexts;
        }
        ReadLock lock = channelContextSetWithLock.getLock().readLock();
        try {
            lock.lock();
            Set<ChannelContext> channelContexts = channelContextSetWithLock.getObj();
            if (CollectionUtils.isEmpty(channelContexts)) {
                return imChannelContexts;
            }
            for (ChannelContext channelContext : channelContexts) {
                ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
                imChannelContexts.add(imChannelContext);
            }
            return imChannelContexts;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        return imChannelContexts;
    }

    /**
     * 转换协议包同时设置Packet包信息;
     *
     * @param imChannelContext
     *            IM通道上下文
     * @param packet
     *            消息包
     * @return
     */
    private static ImPacket convertPacket(ImChannelContext imChannelContext, ImPacket packet) {
        if (Objects.isNull(imChannelContext) || Objects.isNull(packet)) {
            return null;
        }
        try {
            ImPacket respPacket = ProtocolManager.Converter.respPacket(packet, packet.getCommand(), imChannelContext);
            if (respPacket == null) {
                log.error("convert protocol package is empty, please check the protocol");
                return null;
            }
            respPacket.setSynSeq(packet.getSynSeq());
            return respPacket;
        } catch (ImException e) {
            log.error("convert protocol packet is abnormal, please check the protocol");
            return null;
        }
    }

}
