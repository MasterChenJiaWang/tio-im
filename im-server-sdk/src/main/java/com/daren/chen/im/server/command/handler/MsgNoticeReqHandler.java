package com.daren.chen.im.server.command.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.MsgNoticeReq;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.notice.MsgNoticeCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.util.ChatKit;

import cn.hutool.core.collection.CollectionUtil;

/**
 * 版本: [1.0] 功能说明: 消息通知cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class MsgNoticeReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(MsgNoticeReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            MsgNoticeRespBody msgNoticeRespBody = null;
            MsgNoticeReq msgNoticeReq = JsonKit.toBean(packet.getBody(), MsgNoticeReq.class);

            if (msgNoticeReq == null) {
                log.error("上下文ID [{}] 用户ID [{}]   msgNoticeReq is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                msgNoticeRespBody = MsgNoticeRespBody.failed().setMsg("msgNoticeReq is null");
                return ProtocolManager.Converter.respPacket(msgNoticeRespBody, imChannelContext);
            }
            msgNoticeReq.setMsgId(UUID.randomUUID().toString().replaceAll("-", ""));
            msgNoticeReq.setNoticeTime(System.currentTimeMillis());

            // 实际绑定之前执行处理器动作
            MsgNoticeCmdProcessor msgNoticeCmdProcessor = this.getSingleProcessor(MsgNoticeCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(msgNoticeCmdProcessor)) {
                String noticeType = msgNoticeReq.getNoticeType();
                ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
                MessageHelper messageHelper = imServerConfig.getMessageHelper();
                String curUserId = msgNoticeReq.getCurUserId();
                String noticeUserId = msgNoticeReq.getNoticeUserId();
                String noticeGroupId = msgNoticeReq.getNoticeGroupId();
                //
                switch (noticeType) {
                    // 2：添加好友申请同意通知
                    case "2":
                        //
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);

                        // 刷新好友缓存
                        initFriend(imChannelContext.getUserId(), messageHelper, curUserId, noticeUserId);
                        break;

                    // 5:申请入群同意通知
                    case "5":
                        //
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);
                        //
                        if (StringUtils.isBlank(noticeGroupId)) {
                            break;
                        }
                        //
                        List<ImChannelContext> channelContexts = new ArrayList<>();
                        if (StringUtils.isNotBlank(curUserId)) {
                            List<ImChannelContext> byUserId = JimServerAPI.getByUserId(curUserId);
                            if (CollectionUtil.isNotEmpty(byUserId)) {
                                channelContexts.addAll(byUserId);
                            }
                        }
                        if (StringUtils.isNotBlank(noticeUserId)) {
                            List<ImChannelContext> byUserId = JimServerAPI.getByUserId(noticeUserId);
                            if (CollectionUtil.isNotEmpty(byUserId)) {
                                channelContexts.addAll(byUserId);
                            }
                        }
                        // 绑定组
                        if (CollectionUtil.isNotEmpty(channelContexts)) {
                            for (ImChannelContext channelContext : channelContexts) {
                                JimServerAPI.bindGroup(channelContext, noticeGroupId);
                            }
                        }
                        // 清除缓存
                        initAddGroup(imChannelContext.getUserId(), messageHelper, noticeGroupId, curUserId);
                        initAddGroup(imChannelContext.getUserId(), messageHelper, noticeGroupId, noticeUserId);
                        break;
                    // 解散群组
                    case "7":
                        Map<String, Object> result =
                            msgNoticeCmdProcessor.dissolveGroup(msgNoticeReq, imChannelContext);
                        if (result == null) {
                            return ProtocolManager.Converter.respPacket(MsgNoticeRespBody.failed(), imChannelContext);
                        }
                        List<String> userIds = result.get("user_ids") == null ? null : (List)result.get("user_ids");
                        Group groupInfo = result.get("group_info") == null ? null : (Group)result.get("group_info");
                        if (userIds == null) {
                            return ProtocolManager.Converter.respPacket(MsgNoticeRespBody.failed(), imChannelContext);
                        } else {
                            for (String userId : userIds) {
                                // 解绑
                                JimServerAPI.unbindGroup(userId, noticeGroupId);
                                //
                                msgNoticeReq.setNoticeUserId(userId);
                                msgNoticeReq.setGroupInfo(groupInfo);
                                msgNoticeReq.setGroupUserIds(userIds);;
                                sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);
                            }
                        }
                        // 清除缓存
                        dissolveGroup(imChannelContext.getUserId(), messageHelper, noticeGroupId);
                        break;
                    // 8:退出群聊，
                    case "8":
                        //
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);
                        //
                        if (StringUtils.isNotBlank(curUserId)) {
                            List<ImChannelContext> byUserId = JimServerAPI.getByUserId(curUserId);
                            if (CollectionUtil.isNotEmpty(byUserId)) {
                                for (ImChannelContext channelContext : byUserId) {
                                    JimServerAPI.unbindGroup(noticeGroupId, channelContext);
                                }
                            }
                        }
                        // 清除缓存
                        initRemoveGroup(imChannelContext.getUserId(), messageHelper, noticeGroupId, curUserId);
                        break;
                    // 9：被提出群聊
                    case "9":
                        //
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);
                        if (StringUtils.isNotBlank(noticeUserId)) {
                            List<ImChannelContext> byUserId = JimServerAPI.getByUserId(noticeUserId);
                            if (CollectionUtil.isNotEmpty(byUserId)) {
                                for (ImChannelContext channelContext : byUserId) {
                                    JimServerAPI.unbindGroup(noticeGroupId, channelContext);
                                }
                            }
                            // 清除缓存
                            initRemoveGroup(imChannelContext.getUserId(), messageHelper, noticeGroupId, noticeUserId);
                        }
                        break;
                    // 10：删除好友通知
                    case "10":
                        //
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);

                        // 刷新好友缓存
                        initFriend(imChannelContext.getUserId(), messageHelper, curUserId, noticeUserId);
                        break;
                    case "11":
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, false);
                        break;
                    default:
                        sendAndSaveNotice(msgNoticeCmdProcessor, msgNoticeReq, imChannelContext, true);

                        break;
                }

            }
            return null;
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(MsgNoticeRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    private void sendAndSaveNotice(MsgNoticeCmdProcessor msgNoticeCmdProcessor, MsgNoticeReq msgNoticeReq,
        ImChannelContext imChannelContext, boolean isSave) throws ImException {
        MsgNoticeRespBody msgNoticeRespBody = null;
        String noticeSource = msgNoticeReq.getNoticeSource();
        if ("2".equals(noticeSource)) {
            return;
        }
        // 先保存
        if (isSave) {
            msgNoticeRespBody = msgNoticeCmdProcessor.msgNoticeOffLine(msgNoticeReq, imChannelContext);
        }
        // 是否在线
        if (ChatKit.isOnline(msgNoticeReq.getNoticeUserId(), true)) {
            if (msgNoticeRespBody != null) {
                JimServerAPI.sendToUser(msgNoticeReq.getNoticeUserId(),
                    ProtocolManager.Converter.respPacket(msgNoticeRespBody, imChannelContext));
            }
        }
        //
    }

    /**
     *
     * @param curUserId
     * @param friendUserId
     */
    private void initFriend(String operateUserId, MessageHelper messageHelper, String curUserId, String friendUserId)
        throws ImException {
        // 重新加载
        ImStoreBindListener bindListener = messageHelper.getBindListener();
        if (bindListener == null) {
            return;
        }
        bindListener.onAfterAddOrRemoveUser(operateUserId, curUserId);
        bindListener.onAfterAddOrRemoveUser(operateUserId, friendUserId);
    }

    /**
     * 添加或者退出群
     *
     * @param messageHelper
     * @param groupId
     * @param userId
     * @throws ImException
     */
    private void initAddGroup(String operateUserId, MessageHelper messageHelper, String groupId, String userId)
        throws ImException {
        // 重新加载
        ImStoreBindListener bindListener = messageHelper.getBindListener();
        if (bindListener == null) {
            return;
        }
        bindListener.onAfterAddGroup(operateUserId, userId, groupId);
    }

    /**
     * 添加或者退出群
     *
     * @param messageHelper
     * @param groupId
     * @param userId
     * @throws ImException
     */
    private void initRemoveGroup(String operateUserId, MessageHelper messageHelper, String groupId, String userId)
        throws ImException {
        // 重新加载
        ImStoreBindListener bindListener = messageHelper.getBindListener();
        if (bindListener == null) {
            return;
        }
        bindListener.onAfterRemoveGroup(operateUserId, userId, groupId);
    }

    /**
     * 解散群
     *
     * @param messageHelper
     * @param groupId
     * @throws ImException
     */
    private void dissolveGroup(String operateUserId, MessageHelper messageHelper, String groupId) throws ImException {
        // 重新加载
        ImStoreBindListener bindListener = messageHelper.getBindListener();
        if (bindListener == null) {
            return;
        }
        bindListener.onAfterDissolveGroup(operateUserId, groupId);
    }

    @Override
    public Command command() {
        return Command.MSG_NOTICE_REQ;
    }
}
