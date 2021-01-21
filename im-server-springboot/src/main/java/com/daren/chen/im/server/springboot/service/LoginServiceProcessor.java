/**
 *
 */
package com.daren.chen.im.server.springboot.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.packets.LoginRespBody;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.login.LoginCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.springboot.common.ApiMethodConstants;
import com.daren.chen.im.server.springboot.entity.GatewayCode;
import com.daren.chen.im.server.springboot.entity.Result;
import com.daren.chen.im.server.springboot.queue.UserOnlineStatusQueueRunnable;
import com.daren.chen.im.server.springboot.utils.HttpApiUtils;
import com.daren.chen.im.server.util.Environment;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;

/**
 * @author WChao
 *
 */
@Service
public class LoginServiceProcessor extends AbstractProtocolCmdProcessor implements LoginCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceProcessor.class);

    /**
     * 根据用户名和密码获取用户
     *
     * @param loginReqBody
     * @param imChannelContext
     * @return
     * @author: WChao
     */
    @Override
    public User getUser(LoginReqBody loginReqBody, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        String userId = loginReqBody.getUserId();
        // 根据用户ID获取 用户信息
        User userByType = messageHelper.getUserByType(imChannelContext.getUserId(), userId, 2);
        if (userByType != null) {
            List<User> allFriendUsers = messageHelper.getAllFriendUsers(imChannelContext.getUserId(), userId, 2);
            userByType.setFriends(allFriendUsers);
            List<String> groups = messageHelper.getGroups(imChannelContext.getUserId(), userId);
            userByType.setGroupIds(groups);
        }
        return userByType;
    }

    /**
     * 登陆成功返回状态码:ImStatus.C10007 登录失败返回状态码:ImStatus.C10008
     * 注意：只要返回非成功状态码(ImStatus.C10007),其他状态码均为失败,此时用户可以自定义返回状态码，定义返回前端失败信息
     */
    @Override
    public LoginRespBody doLogin(LoginReqBody loginReqBody, ImChannelContext imChannelContext) {

        try {
            String phone = loginReqBody.getPhone();
            if (Objects.nonNull(phone) && Objects.nonNull(loginReqBody.getToken())) {
                String token = loginReqBody.getToken();
                String appId = loginReqBody.getAppId();
                String appKey = loginReqBody.getAppKey();
                if (StringUtils.isBlank(token) || StringUtils.isBlank(appId) || StringUtils.isBlank(appKey)) {
                    return LoginRespBody.failed("token/appkey/appId 为空!");
                }
                // 保存token 等信息
                LoginUser loginUser = new LoginUser("", "", token, appId, appKey);
                Environment.setCurrentUser(loginUser);
                ImServerConfig imServerConfig = ImConfig.Global.get();
                MessageHelper messageHelper = imServerConfig.getMessageHelper();
                User user = messageHelper.getUserBaseInfoByToken(token);
                if (user == null) {
                    return LoginRespBody.failed();
                }
                String userId = user.getUserId();
                imChannelContext.setUserId(userId);
                token = user.getExtras() == null ? "" : user.getExtras().getString("token");
                String username = user.getNick();

                // 保存token 等信息
                loginUser = new LoginUser(userId, username, token, appId, appKey);
                loginUser.setPhoneImei(loginReqBody.getPhoneImei());
                loginUser.setSysVersion(loginReqBody.getSysVersion());
                loginUser.setAppVersion(loginReqBody.getAppVersion());
                loginUser.setTerminalType(loginReqBody.getTerminalType());
                loginUser.setContextId(imChannelContext.getId());
                loginUser.setReportTime(loginReqBody.getReportTime());
                Environment.setCurrentUser(loginUser);
                LoginUser loginUser1 = new LoginUser();
                BeanUtils.copyProperties(loginUser, loginUser1);
                AuthCacheService.saveAuth(loginUser1);
                messageHelper.addUserLoginInfo(ImConst.USER_VERSION_INFO, imChannelContext.getId(), loginUser);
                //
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("user_id", userId);
                paramMap.put("online_status", "0");
                paramMap.put("phone_imei", loginReqBody.getPhoneImei());
                paramMap.put("sys_version", loginReqBody.getSysVersion());
                paramMap.put("app_version", loginReqBody.getAppVersion());
                paramMap.put("terminal_type", loginReqBody.getTerminalType());
                paramMap.put("context_id", imChannelContext.getId());
                paramMap.put("report_time", DateUtil.formatDateTime(new Date()));

                UserOnlineStatusQueueRunnable userOnlineStatusQueueRunnable =
                    getUserOnlineStatusQueueRunnable((ImServerChannelContext)imChannelContext);
                userOnlineStatusQueueRunnable.addMsg(paramMap);
                userOnlineStatusQueueRunnable.executor.execute(userOnlineStatusQueueRunnable);
                logger.info("上下文ID [{}] 用户ID [{}]   doLogin 登录成功", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return LoginRespBody.success(user);
            } else {
                return LoginRespBody.failed();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return LoginRespBody.failed(e.getMessage());
        }
    }

    /**
     *
     * @param token
     * @return
     */
    private JSONObject getUserInfoByToken(String token) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.AUTH_101801, jsonObject);
        if (GatewayCode.SUCCESS.getCode() != result.getCode()
            || GatewayCode.SUCCESS.getCode().toString().equals(result.getBizCode())) {
            throw new RuntimeException(result.getBizMsg());
        }
        return result.getData();
    }

    @Override
    public void onSuccess(User user, ImChannelContext channelContext) {
        logger.info("上下文ID [{}] 用户ID [{}]   登录成功回调方法", channelContext.getId(), channelContext.getUserId());
    }

    @Override
    public void onFailed(ImChannelContext channelContext) {
        logger.info("上下文ID [{}] 用户ID [{}]   登录失败回调方法", channelContext.getId(), channelContext.getUserId());
    }

    /**
     *
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void offlineMesage(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper) {
        try {
            UserMessageData messageData = messageHelper.getFriendsOfflineMessage(imChannelContext.getUserId(), userId);
            if (messageData == null) {
                return;
            }
            if (CollectionUtil.isEmpty(messageData.getGroups()) && CollectionUtil.isEmpty(messageData.getFriends())) {
                return;
            }
            RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10015.getText());
            resPacket.setData(messageData);
            ImPacket imPacket = ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
            // 发送通知给 客户
            JimServerAPI.sendToUser(userId, imPacket);
        } catch (ImException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param imChannelContext
     * @param userId
     * @param messageHelper
     * @throws ImException
     */
    private void notice(ImChannelContext imChannelContext, String userId, MessageHelper messageHelper) {
        try {
            ReceiveMsgNoticeRespBody receiveMsgNoticeRespBody =
                messageHelper.receiveMsgNotice(imChannelContext.getUserId(), userId);
            if (receiveMsgNoticeRespBody == null) {
                return;
            }
            List<MsgNoticeRespBody> list = receiveMsgNoticeRespBody.getList();
            if (CollectionUtil.isEmpty(list)) {
                return;
            }
            ReceiveMsgNoticeRespBody success = ReceiveMsgNoticeRespBody.success(receiveMsgNoticeRespBody);
            // 发送通知给 客户
            JimServerAPI.sendToUser(userId,
                ProtocolManager.Converter.respPacket(ReceiveMsgNoticeRespBody.success(success), imChannelContext));
        } catch (ImException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    private UserOnlineStatusQueueRunnable
        getUserOnlineStatusQueueRunnable(ImServerChannelContext imServerChannelContext) {
        UserOnlineStatusQueueRunnable userOnlineStatusQueueRunnable =
            (UserOnlineStatusQueueRunnable)imServerChannelContext.getUserOnlineStatusQueueRunnable();
        if (userOnlineStatusQueueRunnable != null
            && Objects.nonNull(userOnlineStatusQueueRunnable.getProtocolCmdProcessor())) {
            return userOnlineStatusQueueRunnable;
        }
        synchronized (UserOnlineStatusQueueRunnable.class) {
            userOnlineStatusQueueRunnable.setProtocolCmdProcessor(new NoticeOfflineServiceProcessor());
        }
        return userOnlineStatusQueueRunnable;
    }
}
