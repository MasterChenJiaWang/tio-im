/**
 *
 */
package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.UserReqBody;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.command.handler.userInfo.IUserInfo;
import com.daren.chen.im.server.command.handler.userInfo.NonPersistentUserInfo;
import com.daren.chen.im.server.command.handler.userInfo.PersistentUserInfo;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 获取用户信息消息命令
 *
 * @author : WChao 创建时间: 2017年9月18日 下午4:08:47
 */
public class UserReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(UserReqHandler.class);
    /**
     * 非持久化用户信息接口
     */
    private IUserInfo nonPersistentUserInfo;
    /**
     * 持久化用户信息接口
     */
    private IUserInfo persistentUserInfo;

    public UserReqHandler() {
        persistentUserInfo = new PersistentUserInfo();
        nonPersistentUserInfo = new NonPersistentUserInfo();
    }

    @Override
    public Command command() {
        return Command.COMMAND_GET_USER_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            UserReqBody userReqBody = JsonKit.toBean(packet.getBody(), UserReqBody.class);
            if (userReqBody == null) {
                return ProtocolManager.Converter.respPacket(
                    new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004.getText()), imChannelContext);
            }
            String userId = userReqBody.getUserId();
            if (StringUtils.isEmpty(userId)) {
                return ProtocolManager.Converter.respPacket(
                    new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004.getText()), imChannelContext);
            }
            // (0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线]);
            int type = userReqBody.getType() == null ? UserStatusType.ALL.getNumber() : userReqBody.getType();
            if (Objects.isNull(UserStatusType.valueOf(type))) {
                return ProtocolManager.Converter.respPacket(
                    new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004.getText()), imChannelContext);
            }
            RespBody resPacket = new RespBody(Command.COMMAND_GET_USER_RESP);
            ImServerConfig imServerConfig = ImConfig.Global.get();
            // 是否开启持久化;
            boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
            if (isStore) {
                resPacket.setData(persistentUserInfo.getUserInfo(userReqBody, imChannelContext));
            } else {
                resPacket.setData(nonPersistentUserInfo.getUserInfo(userReqBody, imChannelContext));
            }
            // 在线用户
            if (UserStatusType.ONLINE.getNumber() == userReqBody.getType()) {
                resPacket.setCode(ImStatus.C10005.getCode()).setMsg(ImStatus.C10005.getText());
                // 离线用户;
            } else if (UserStatusType.OFFLINE.getNumber() == userReqBody.getType()) {
                resPacket.setCode(ImStatus.C10006.getCode()).setMsg(ImStatus.C10006.getText());
                // 在线+离线用户;
            } else if (UserStatusType.ALL.getNumber() == userReqBody.getType()) {
                resPacket.setCode(ImStatus.C10003.getCode()).setMsg(ImStatus.C10003.getText());
            }
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            RespBody resPacket = new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004.getText());
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        }
    }

    public IUserInfo getNonPersistentUserInfo() {
        return nonPersistentUserInfo;
    }

    public void setNonPersistentUserInfo(IUserInfo nonPersistentUserInfo) {
        this.nonPersistentUserInfo = nonPersistentUserInfo;
    }

    public IUserInfo getPersistentUserInfo() {
        return persistentUserInfo;
    }

    public void setPersistentUserInfo(IUserInfo persistentUserInfo) {
        this.persistentUserInfo = persistentUserInfo;
    }

}
