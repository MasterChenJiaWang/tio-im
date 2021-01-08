package com.daren.chen.im.server.springboot.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.cache.redis.RedisCache;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.listener.ImServerListener;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.queue.MsgAndAckQueueRunnable;
import com.daren.chen.im.server.queue.NoticeAckQueueRunnable;
import com.daren.chen.im.server.springboot.queue.UserOnlineStatusQueueRunnable;
import com.daren.chen.im.server.springboot.service.NoticeOfflineServiceProcessor;
import com.daren.chen.im.server.util.ChatKit;

import cn.hutool.core.date.DateUtil;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/20 12:52
 */
public class MyImServerListener implements ImServerListener {

    private final Logger log = LoggerFactory.getLogger(MyImServerListener.class);

    @Override
    public boolean onHeartbeatTimeout(ImChannelContext imChannelContext, Long interval, int heartbeatTimeoutCount) {
        return false;
    }

    @Override
    public void onAfterConnected(ImChannelContext imChannelContext, boolean isConnected, boolean isReconnect)
        throws Exception {
        String userId = "";
        if (imChannelContext != null) {
            userId = imChannelContext.getUserId();
        }
        log.info("上下文ID [{}] 用户ID [{}]   连接成功后触发本方法 >>>>>>>>  连接状态  isConnected : {}   是否是重连 isReconnect {} ",
            imChannelContext.getId(), userId, isConnected, isReconnect);
        //
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
        imServerChannelContext.setMsgAndAckQue(
            new MsgAndAckQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        imServerChannelContext.setNoticeAckQue(
            new NoticeAckQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExecutor()));
        imServerChannelContext.setUserOnlineStatusQueueRunnable(
            new UserOnlineStatusQueueRunnable(imChannelContext, imChannelContext.getImConfig().getJimExpandExecutor()));
    }

    @Override
    public void onAfterDecoded(ImChannelContext imChannelContext, ImPacket packet, int packetSize) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ImChannelContext imChannelContext, int receivedBytes) throws Exception {

    }

    @Override
    public void onAfterSent(ImChannelContext imChannelContext, ImPacket packet, boolean isSentSuccess)
        throws Exception {

    }

    @Override
    public void onAfterHandled(ImChannelContext imChannelContext, ImPacket packet, long cost) throws Exception {

    }

    @Override
    public void onBeforeClose(ImChannelContext imChannelContext, Throwable throwable, String remark, boolean isRemove)
        throws Exception {
        String userId = "";
        if (imChannelContext != null) {
            userId = imChannelContext.getUserId();
            ChatKit.isOnline(userId, false);
        }
        log.error("连接关闭前触发本方法 userid {}  remark : {}   异常 {}", userId, remark,
            throwable == null ? "" : throwable.getMessage());
        if (imChannelContext == null) {
            return;
        }
        try {
            /*        user_id:用户ID, online_status:在线状态（0：在线，1：离线） , phone_imei:手机imei, sys_version:系统版本,
                 app_version:app版本, terminal_type:终端类型(0：andriod，1：IOS), context_id:上下文id, report_ime:上下线记录时间(年月日时分秒)*/
            RedisCache cache = RedisCacheManager.getCache(ImConst.USER_VERSION_INFO);
            LoginUser loginUserTemp = cache.get(imChannelContext.getId(), LoginUser.class);
            Map<String, Object> paramMap = new HashMap<>();
            if (loginUserTemp != null) {
                paramMap.put("user_id", userId);
                paramMap.put("online_status", "1");
                paramMap.put("phone_imei", loginUserTemp.getPhoneImei());
                paramMap.put("sys_version", loginUserTemp.getSysVersion());
                paramMap.put("app_version", loginUserTemp.getAppVersion());
                paramMap.put("terminal_type", loginUserTemp.getTerminalType());
                paramMap.put("context_id", imChannelContext.getId());
                paramMap.put("report_time", DateUtil.formatDateTime(new Date()));
                UserOnlineStatusQueueRunnable userOnlineStatusQueueRunnable =
                    getUserOnlineStatusQueueRunnable((ImServerChannelContext)imChannelContext);
                userOnlineStatusQueueRunnable.addMsg(paramMap);
                userOnlineStatusQueueRunnable.executor.execute(userOnlineStatusQueueRunnable);
                //
                log.info("{} 清空用户在线状态缓存", imChannelContext.getId());
                // // TODO 清空
                RedisCacheManager.getCache(ImConst.USER_VERSION_INFO).remove(imChannelContext.getId());
            }
            // todo 清空所有队列
            ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
            imServerChannelContext.allEmpty();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private UserOnlineStatusQueueRunnable
        getUserOnlineStatusQueueRunnable(ImServerChannelContext imServerChannelContext) {
        UserOnlineStatusQueueRunnable userOnlineStatusQueueRunnable =
            (UserOnlineStatusQueueRunnable)imServerChannelContext.getUserOnlineStatusQueueRunnable();
        if (Objects.nonNull(userOnlineStatusQueueRunnable.getProtocolCmdProcessor())) {
            return userOnlineStatusQueueRunnable;
        }
        synchronized (UserOnlineStatusQueueRunnable.class) {
            userOnlineStatusQueueRunnable.setProtocolCmdProcessor(new NoticeOfflineServiceProcessor());
        }
        return userOnlineStatusQueueRunnable;
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
                return null;
            }
            respPacket.setSynSeq(packet.getSynSeq());
            return respPacket;
        } catch (ImException e) {
            return null;
        }
    }
}
