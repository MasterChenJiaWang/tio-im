package com.daren.chen.im.server.command.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.GetUserOnlineStatusRespBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.ImClientNode;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.packets.LoginRespBody;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserStatusType;
import com.daren.chen.im.core.protocol.IProtocol;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.ImServerChannelContext;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.login.LoginCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.ChatKit;

/**
 * 登录消息命令处理器
 *
 * @author WChao
 * @date 2018年4月10日 下午2:40:07
 */
public class LoginReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
            LoginCmdProcessor loginProcessor = this.getSingleProcessor(LoginCmdProcessor.class);
            LoginRespBody loginRespBody = LoginRespBody.success();
            LoginReqBody loginReqBody = JsonKit.toBean(packet.getBody(), LoginReqBody.class);
            if (loginReqBody == null) {
                loginProcessor.onFailed(imChannelContext);
                loginRespBody = LoginRespBody.failed("登录失败!");
                return ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext);
            }
            LoginReqBody userByProcessor =
                getUserByProcessor(imChannelContext, loginProcessor, loginReqBody, loginRespBody);
            if (userByProcessor == null) {
                loginProcessor.onFailed(imChannelContext);
                loginRespBody = LoginRespBody.failed("登录失败!");
                return ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext);
            }
            // 在绑定
            User user = User.newBuilder().userId(userByProcessor.getUserId()).build();
            IProtocol protocol = imServerChannelContext.getProtocolHandler().getProtocol();
            user.setTerminal(Objects.isNull(protocol) ? Protocol.UNKNOWN : protocol.name());
            JimServerAPI.bindUser(imServerChannelContext, user);
            // 绑定LoginUser
            ImClientNode imClientNode = imChannelContext.getSessionContext().getImClientNode();
            if (imClientNode != null) {
                LoginUser auth = AuthCacheService.getAuth(userByProcessor.getUserId());
                if (auth != null) {
                    imClientNode.setLoginUser(auth);
                }
            }
            //
            loginProcessor.onSuccess(user, imChannelContext);
            //
            syncInit(imChannelContext, loginProcessor, loginReqBody);
            //
            loginRespBody.setUser(user);
            return ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            LoginRespBody failed = LoginRespBody.failed("登录失败!");
            return ProtocolManager.Converter.respPacket(failed, imChannelContext);
        }
    }

    public void syncInit(ImChannelContext imChannelContext, LoginCmdProcessor loginProcessor, LoginReqBody loginReqBody)
        throws ImException {
        String userId = loginReqBody.getUserId();
        if (StringUtils.isNotBlank(userId)) {
            AuthCacheService.setEnvironment(userId);
        }
        User user = loginProcessor.getUser(loginReqBody, imChannelContext);;
        if (user == null) {
            return;
        }
        // 通知 好友在线
        notifyFriendsOnline(user.getUserId(), user.getFriends(), imChannelContext);
        // //
        ImServerConfig imServerConfig = ImConfig.Global.get();
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        initFriendsOnline(user.getFriends(), isStore, imChannelContext);
        // 初始化绑定或者解绑群组;
        initGroup(imChannelContext, user);
        //
        loginProcessor.onSuccess(user, imChannelContext);
        // ExecutorUtils.EXECUTOR.execute(new Thread(() -> {
        // try {
        // String userId = loginReqBody.getUserId();
        // if (StringUtils.isNotBlank(userId)) {
        // AuthCacheService.setEnvironment(userId);
        // }
        // User user = loginProcessor.getUser(loginReqBody, imChannelContext);;
        // if (user == null) {
        // return;
        // }
        // // 通知 好友在线
        // notifyFriendsOnline(user.getUserId(), user.getFriends(), imChannelContext);
        // // //
        // ImServerConfig imServerConfig = ImConfig.Global.get();
        // boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        // initFriendsOnline(user.getFriends(), isStore, imChannelContext);
        // // 初始化绑定或者解绑群组;
        // initGroup(imChannelContext, user);
        // //
        // loginProcessor.onSuccess(user, imChannelContext);
        // } catch (ImException e) {
        // e.printStackTrace();
        // }
        // }));
    }

    /**
     *
     * @param friendList
     * @param isStore
     * @param imChannelContext
     * @throws ImException
     */
    private void initFriendsOnline(List<User> friendList, boolean isStore, ImChannelContext imChannelContext)
        throws ImException {

        if (friendList != null) {
            for (User user : friendList) {
                String userId1 = user.getUserId();
                GetUserOnlineStatusRespBody getUserOnlineStatusRespBody = new GetUserOnlineStatusRespBody();
                getUserOnlineStatusRespBody.setUserId(userId1);
                getUserOnlineStatusRespBody.setStatus(ChatKit.isOnline(userId1, isStore)
                    ? UserStatusType.ONLINE.getStatus() : UserStatusType.OFFLINE.getStatus());
                GetUserOnlineStatusRespBody getUserOnlineStatusRespBody1 =
                    GetUserOnlineStatusRespBody.success(userId1).setData(getUserOnlineStatusRespBody);
                ImPacket imPacket =
                    ProtocolManager.Converter.respPacket(getUserOnlineStatusRespBody1, imChannelContext);
                JimServerAPI.send(imChannelContext, imPacket);
            }
        }
    }

    /**
     *
     * @param userId
     * @param friendList
     * @param imChannelContext
     * @throws ImException
     */
    private void notifyFriendsOnline(String userId, List<User> friendList, ImChannelContext imChannelContext)
        throws ImException {

        GetUserOnlineStatusRespBody getUserOnlineStatusRespBody = new GetUserOnlineStatusRespBody();
        getUserOnlineStatusRespBody.setUserId(userId);
        getUserOnlineStatusRespBody.setStatus(UserStatusType.ONLINE.getStatus());
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
     * 根据用户配置的自定义登录处理器获取业务组装的User信息
     *
     * @param imChannelContext
     *            通道上下文
     * @param loginProcessor
     *            登录自定义业务处理器
     * @param loginReqBody
     *            登录请求体
     * @param loginRespBody
     *            登录响应体
     * @return 用户组装的User信息
     * @throws ImException
     */
    private LoginReqBody getUserByProcessor(ImChannelContext imChannelContext, LoginCmdProcessor loginProcessor,
        LoginReqBody loginReqBody, LoginRespBody loginRespBody) throws ImException {
        if (Objects.isNull(loginProcessor)) {
            return null;
        }
        // if (Objects.isNull(loginProcessor)) {
        // User user =
        // User.newBuilder().userId(loginReqBody.getUserId()).status(UserStatusType.ONLINE.getStatus()).build();
        // return user;
        // }
        loginRespBody = loginProcessor.doLogin(loginReqBody, imChannelContext);
        if (Objects.isNull(loginRespBody) || loginRespBody.getCode() != ImStatus.C10007.getCode()) {
            log.error("login failed, userId:{}, password:{}", loginReqBody.getUserId(), loginReqBody.getPassword());
            loginProcessor.onFailed(imChannelContext);
            JimServerAPI.send(imChannelContext, ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext));
            JimServerAPI.remove(imChannelContext, "userId or token is incorrect");
            return null;
        }
        User user = loginRespBody.getUser();
        if (user == null) {
            log.error("login failed, userId:{}, password:{}", loginReqBody.getUserId(), loginReqBody.getPassword());
            loginProcessor.onFailed(imChannelContext);
            JimServerAPI.send(imChannelContext, ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext));
            JimServerAPI.remove(imChannelContext, "userId or token is incorrect");
            return null;
        }
        JimServerAPI.bindUserToken(imChannelContext, loginRespBody.getToken());
        loginReqBody.setUserId(user.getUserId());
        return loginReqBody;
        // return loginProcessor.getUser(loginReqBody, imChannelContext);
    }

    /**
     * 初始化绑定或者解绑群组;
     */
    public void initGroup(ImChannelContext imChannelContext, User user) throws ImException {
        String userId = user.getUserId();
        // List<Group> groups = user.getGroups();
        List<String> groupIds = user.getGroupIds();
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        ImServerConfig imServerConfig = ImConfig.Global.get();
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // List<String> groupIds = null;
        if (isStore) {
            groupIds = messageHelper.getGroups(imChannelContext.getUserId(), userId);
        }
        List<String> newGroupIds = new ArrayList<>();
        // 绑定群组
        for (String groupId : groupIds) {
            if (isStore && CollectionUtils.isNotEmpty(groupIds)) {
                newGroupIds.remove(groupId);
            }
            JimServerAPI.bindGroup(imChannelContext, groupId);
            // JoinGroupReqBody joinGroupReqBody = JoinGroupReqBody.newBuilder().setIsLoginAdd(true).build();
            // BeanUtil.copyProperties(group, joinGroupReqBody);
            // //
            // ImPacket groupPacket = new ImPacket(Command.COMMAND_JOIN_GROUP_REQ,
            // JsonKit.toJsonBytes(joinGroupReqBody));
            // try {
            // JoinGroupReqHandler joinGroupReqHandler =
            // CommandManager.getCommand(Command.COMMAND_JOIN_GROUP_REQ, JoinGroupReqHandler.class);
            // joinGroupReqHandler.handler(groupPacket, imChannelContext);
            // } catch (Exception e) {
            // log.error(e.toString(), e);
            // }
        }
        if (isStore) {
            for (String groupId : newGroupIds) {
                messageHelper.getBindListener().onAfterGroupUnbind(imChannelContext,
                    Group.newBuilder().groupId(groupId).build());
            }
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_LOGIN_REQ;
    }

}
