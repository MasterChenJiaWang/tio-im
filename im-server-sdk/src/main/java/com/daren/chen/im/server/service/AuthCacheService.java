package com.daren.chen.im.server.service;

import java.util.ArrayList;
import java.util.List;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.util.Environment;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/10/19 20:01
 */
public class AuthCacheService {

    /**
     *
     */
    private static final TimedCache<String, LoginUser> TIMED_CACHE = CacheUtil.newTimedCache(1000 * 60 * 60 * 24);

    public static void saveAuth(LoginUser loginUser) {
        if (null == loginUser) {
            return;
        }
        String userId = loginUser.getUserId();
        ImServerConfig imServerConfig = ImServerConfig.Global.get();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        LoginUser loginUserTemp = messageHelper.getUserLoginInfo(ImConst.USER_TOKEN, userId);
        if (loginUserTemp != null) {
            loginUser.setPhoneImei(loginUserTemp.getPhoneImei());
            loginUser.setSysVersion(loginUserTemp.getSysVersion());
            loginUser.setAppVersion(loginUserTemp.getAppVersion());
            loginUser.setTerminalType(loginUserTemp.getTerminalType());
            loginUser.setContextId(loginUserTemp.getContextId());
            loginUser.setReportTime(loginUserTemp.getReportTime());
        }
        TIMED_CACHE.put(userId, loginUser);
        messageHelper.addUserLoginInfo(ImConst.USER_TOKEN, userId, loginUser);
    }

    /**
     * @param userId
     * @return
     */
    public static LoginUser getAuth(String userId) {
        if (null == userId) {
            return null;
        }
        //
        LoginUser loginUser = TIMED_CACHE.get(userId);
        ImServerConfig imServerConfig = ImServerConfig.Global.get();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        if (loginUser == null) {
            loginUser = messageHelper.getUserLoginInfo(ImConst.USER_TOKEN, userId);
        }
        LoginUser loginUserTemp = messageHelper.getUserLoginInfo(ImConst.USER_VERSION_INFO, userId);
        if (loginUserTemp != null) {
            loginUser.setPhoneImei(loginUserTemp.getPhoneImei());
            loginUser.setSysVersion(loginUserTemp.getSysVersion());
            loginUser.setAppVersion(loginUserTemp.getAppVersion());
            loginUser.setTerminalType(loginUserTemp.getTerminalType());
            loginUser.setContextId(loginUserTemp.getContextId());
            loginUser.setReportTime(loginUserTemp.getReportTime());
        }
        return loginUser;
    }

    /**
     *
     * @param userId
     */
    public static void setEnvironment(String userId) {
        LoginUser auth = null;
        int n = 0;
        try {
            while (n < 10 && (auth = AuthCacheService.getAuth(userId)) == null) {
                Thread.sleep(20);
                n++;
            }

            LoginUser loginUserTemp = RedisCacheManager.getCache(ImConst.USER_TOKEN).get(userId, LoginUser.class);
            if (loginUserTemp != null && auth != null) {
                auth.setPhoneImei(loginUserTemp.getPhoneImei());
                auth.setSysVersion(loginUserTemp.getSysVersion());
                auth.setAppVersion(loginUserTemp.getAppVersion());
                auth.setTerminalType(loginUserTemp.getTerminalType());
                auth.setContextId(loginUserTemp.getContextId());
                auth.setReportTime(loginUserTemp.getReportTime());
            }
            Environment.setCurrentUser(auth);
        } catch (InterruptedException e) {
        }

    }

    public static List<LoginUser> findAll() {
        List<LoginUser> list = new ArrayList<>();
        for (LoginUser loginUser : TIMED_CACHE) {
            list.add(loginUser);
        }
        return list;
    }
}
