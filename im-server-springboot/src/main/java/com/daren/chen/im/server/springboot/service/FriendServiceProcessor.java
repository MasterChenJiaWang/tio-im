package com.daren.chen.im.server.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.AddFriendRespBody;
import com.daren.chen.im.core.packets.DeleteFriendRespBody;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.friend.FriendCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/21 11:26
 */
public class FriendServiceProcessor extends AbstractProtocolCmdProcessor implements FriendCmdProcessor {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(GroupServiceProcessor.class);

    @Override
    public AddFriendRespBody addFriend(String curUserId, String friendUserId, ImChannelContext imChannelContext) {
        //
        try {
            ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            //
            init(imChannelContext.getUserId(), messageHelper, curUserId, friendUserId);
            //
            // 通知对方添加成功
            AddFriendRespBody success = AddFriendRespBody.success(curUserId, friendUserId);
            ImPacket imPacket = ProtocolManager.Converter.respPacket(success, imChannelContext);
            JimServerAPI.sendToUser(friendUserId, imPacket);
            //
            return AddFriendRespBody.success(curUserId, friendUserId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return AddFriendRespBody.failed(curUserId, friendUserId).setMsg(e.getMessage());
        }
    }

    @Override
    public DeleteFriendRespBody deleteFriend(String curUserId, String friendUserId, ImChannelContext imChannelContext) {
        //
        try {
            ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            //
            init(imChannelContext.getUserId(), messageHelper, curUserId, friendUserId);

            // 通知对方被删除
            DeleteFriendRespBody success = DeleteFriendRespBody.success(curUserId, friendUserId);
            ImPacket imPacket = ProtocolManager.Converter.respPacket(success, imChannelContext);
            JimServerAPI.sendToUser(friendUserId, imPacket);
            //
            return DeleteFriendRespBody.success(curUserId, friendUserId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return DeleteFriendRespBody.failed(curUserId, friendUserId).setMsg(e.getMessage());
        }
    }

    /**
     *
     * @param curUserId
     * @param friendUserId
     */
    public void init(String operateUserId, MessageHelper messageHelper, String curUserId, String friendUserId)
        throws Exception {
        // 重新加载
        ImStoreBindListener bindListener = messageHelper.getBindListener();
        if (bindListener == null) {
            return;
        }
        bindListener.onAfterAddOrRemoveUser(operateUserId, curUserId);
        bindListener.onAfterAddOrRemoveUser(operateUserId, friendUserId);
    }
}
