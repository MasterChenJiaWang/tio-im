/**
 *
 */
package com.daren.chen.im.server.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.AuthReqBody;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.processor.auth.AuthCmdProcessor;
import com.daren.chen.im.server.protocol.AbstractProtocolCmdProcessor;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.Environment;

/**
 * @author WChao
 *
 */
@Service
public class AuthServiceProcessor extends AbstractProtocolCmdProcessor implements AuthCmdProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceProcessor.class);

    /**
     *
     * @param authReqBody
     * @param imChannelContext
     * @return
     */
    @Override
    public AuthRespBody checkToken(AuthReqBody authReqBody, ImChannelContext imChannelContext) {
        // 通过token 获取 userId 信息
        try {
            String token = authReqBody.getToken();
            String appId = authReqBody.getAppId();
            String appKey = authReqBody.getAppKey();
            // 保存token 等信息
            LoginUser loginUser = new LoginUser("", "", token, appId, appKey);
            Environment.setCurrentUser(loginUser);
            ImServerConfig imServerConfig = ImConfig.Global.get();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            // todo
            User user = messageHelper.getUserBaseInfoByToken(authReqBody.getToken());
            if (user == null) {
                // return AuthRespBody.failed();
                return AuthRespBody.success();
            }
            String userId = user.getUserId();
            String username = user.getNick();
            // 保存token 等信息
            loginUser = new LoginUser(userId, username, token, appId, appKey);

            LoginUser loginUserTemp =
                RedisCacheManager.getCache(ImConst.USER_VERSION_INFO).get(userId, LoginUser.class);
            if (loginUserTemp != null) {
                loginUser.setPhoneImei(loginUserTemp.getPhoneImei());
                loginUser.setSysVersion(loginUserTemp.getSysVersion());
                loginUser.setAppVersion(loginUserTemp.getAppVersion());
                loginUser.setTerminalType(loginUserTemp.getTerminalType());
                loginUser.setContextId(imChannelContext.getId());
                loginUser.setReportTime(loginUserTemp.getReportTime());
            }

            Environment.setCurrentUser(loginUser);
            AuthCacheService.saveAuth(loginUser);
            return AuthRespBody.success();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return AuthRespBody.failed(e.getMessage());
        }
    }
}
