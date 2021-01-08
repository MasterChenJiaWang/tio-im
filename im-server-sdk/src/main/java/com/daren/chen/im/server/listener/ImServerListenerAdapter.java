package com.daren.chen.im.server.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioListener;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImSessionContext;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.ChatKit;
import com.daren.chen.im.server.util.Environment;

/**
 * @ClassName ImServerListenerAdapter
 * @Description IM服务端连接监听适配器
 * @Author WChao
 * @Date 2020/1/4 9:35
 * @Version 1.0
 **/
public class ImServerListenerAdapter implements ServerAioListener, ImConst {
    /**
     * 服务端监听器
     */
    private final ImServerListener imServerListener;

    /**
     * @author: WChao 2016年12月16日 下午5:52:06
     *
     */
    public ImServerListenerAdapter(ImServerListener imServerListener) {
        this.imServerListener = imServerListener == null ? new DefaultImServerListener() : imServerListener;
    }

    /**
     *
     * 建链后触发本方法，注：建链不一定成功，需要关注参数isConnected
     *
     * @param channelContext
     * @param isConnected
     *            是否连接成功,true:表示连接成功，false:表示连接失败
     * @param isReconnect
     *            是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
     * @throws Exception
     * @author: WChao
     */
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect)
        throws Exception {
        ImServerChannelContext imChannelContext =
            new ImServerChannelContext(ImServerConfig.Global.get(), channelContext);
        channelContext.set(Key.IM_CHANNEL_CONTEXT_KEY, imChannelContext);
        initEnvironment(imChannelContext);
        // // imChannelContext
        // // .setMsgQue(new MsgQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        // // imChannelContext
        // // .setMsgAckQue(new MsgAckQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        // imChannelContext.setMsgAndAckQue(
        // new MsgAndAckQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        // imChannelContext.setNoticeAckQue(
        // new NoticeAckQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        imServerListener.onAfterConnected(imChannelContext, isConnected, isReconnect);
    }

    /**
     * 消息包发送之后触发本方法
     *
     * @param channelContext
     * @param packet
     * @param isSentSuccess
     *            true:发送成功，false:发送失败
     * @throws Exception
     * @author WChao
     */
    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        imServerListener.onAfterSent((ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY), (ImPacket)packet,
            isSentSuccess);
    }

    private void initEnvironment(ImServerChannelContext imServerChannelContext) {
        if (imServerChannelContext == null) {
            return;
        }
        ImSessionContext imSessionContext = imServerChannelContext.getSessionContext();
        if (Objects.nonNull(imSessionContext)) {
            String userId = imServerChannelContext.getUserId();
            if (StringUtils.isBlank(userId)) {
                return;
            }
            LoginUser loginUserTemp = imSessionContext.getImClientNode().getLoginUser();
            if (loginUserTemp == null) {
                loginUserTemp = AuthCacheService.getAuth(userId);
                if (loginUserTemp == null) {
                    loginUserTemp = RedisCacheManager.getCache(ImConst.USER_VERSION_INFO).get(userId, LoginUser.class);
                }
            }
            if (loginUserTemp != null) {
                Environment.setCurrentUser(loginUserTemp);
            }
        }
    }

    /**
     *
     *
     * @param channelContext
     *            the channelContext
     * @param throwable
     *            the throwable 有可能为空
     * @param remark
     *            the remark 有可能为空
     * @param isRemove
     * @author WChao
     * @throws Exception
     */
    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove)
        throws Exception {
        String userIdTemp = "";
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        ImSessionContext imSessionContext = imServerChannelContext.getSessionContext();
        if (Objects.nonNull(imSessionContext)) {
            ImServerConfig imServerConfig = ImConfig.Global.get();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            User user = imServerChannelContext.getSessionContext().getImClientNode().getUser();
            //
            List<User> userFriendsList = new ArrayList<>();
            if (user != null) {
                //
                String userId = user.getUserId();
                userIdTemp = userId;
                //
                userFriendsList = messageHelper.getAllFriendUsers(imServerChannelContext.getUserId(), userId, 2);
                if (StringUtils.isNotBlank(user.getUserId())) {
                    ChatKit.isOnline(user.getUserId(), false);
                }
            }
            boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore()) && Objects.nonNull(messageHelper)
                && Objects.nonNull(user);
            if (isStore) {
                imServerConfig.getImUserListener().onAfterUnbind(imServerChannelContext, user);
                // 通知好友离线
                notifyFriendsOffline(user.getUserId(), userFriendsList, imServerChannelContext,
                    UserStatusType.OFFLINE.getStatus());
            }
        }
        imServerListener.onBeforeClose(imServerChannelContext, throwable, remark, isRemove);
    }

    /**
     * 解码成功后触发本方法
     *
     * @param channelContext
     * @param packet
     * @param packetSize
     * @throws Exception
     * @author: WChao
     */
    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        imServerListener.onAfterDecoded((ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY),
            (ImPacket)packet, packetSize);
    }

    /**
     *
     * @param userId
     * @param friendList
     * @param imChannelContext
     * @throws ImException
     */
    private void notifyFriendsOffline(String userId, List<User> friendList, ImChannelContext imChannelContext,
        String status) throws ImException {

        GetUserOnlineStatusRespBody getUserOnlineStatusRespBody = new GetUserOnlineStatusRespBody();
        getUserOnlineStatusRespBody.setUserId(userId);
        getUserOnlineStatusRespBody.setStatus(status);
        GetUserOnlineStatusRespBody getUserOnlineStatusRespBody1 =
            GetUserOnlineStatusRespBody.success(userId).setData(getUserOnlineStatusRespBody);
        ImPacket imPacket = ProtocolManager.Converter.respPacket(getUserOnlineStatusRespBody1, imChannelContext);
        if (friendList != null) {
            for (User user : friendList) {
                JimServerAPI.sendToUser(user.getUserId(), imPacket);
            }
        }
    }

    /**
     * 接收到TCP层传过来的数据后
     *
     * @param channelContext
     * @param receivedBytes
     *            本次接收了多少字节
     * @throws Exception
     */
    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        imServerListener.onAfterReceivedBytes((ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY),
            receivedBytes);
    }

    /**
     * 处理一个消息包后
     *
     * @param channelContext
     * @param packet
     * @param cost
     *            本次处理消息耗时，单位：毫秒
     * @throws Exception
     */
    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        imServerListener.onAfterHandled((ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY),
            (ImPacket)packet, cost);
    }

    /**
     *
     * @param channelContext
     * @param aLong
     * @param i
     * @return
     */
    @Override
    public boolean onHeartbeatTimeout(ChannelContext channelContext, Long aLong, int i) {
        ImServerChannelContext imServerChannelContext =
            (ImServerChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        initEnvironment(imServerChannelContext);
        return imServerListener.onHeartbeatTimeout((ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY),
            aLong, i);
    }

}
