package com.daren.chen.im.server.springboot.timer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.util.Environment;

import nl.basjes.shaded.org.springframework.util.CollectionUtils;
import nl.basjes.shaded.org.springframework.util.StringUtils;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/21 10:36
 */
@Component
public class UserTokenTimer {
    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(UserTokenTimer.class);

    @Scheduled(cron = "0 0 0 */1 * ?")
    public void updateUserToken() {
        List<LoginUser> all = AuthCacheService.findAll();
        if (!CollectionUtils.isEmpty(all)) {
            for (LoginUser loginUser : all) {
                String token = loginUser.getToken();
                if (StringUtils.isEmpty(token)) {
                    continue;
                }

                Environment.setCurrentUser(loginUser);
                User user = null;
                try {
                    ImServerConfig imServerConfig = ImConfig.Global.get();
                    MessageHelper messageHelper = imServerConfig.getMessageHelper();
                    user = messageHelper.getUserBaseInfoByToken(token);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    continue;
                }
                if (user == null) {
                    continue;
                }

                JSONObject extras = user.getExtras();
                if (extras == null) {
                    continue;
                }
                String token1 = extras.getString("token");
                loginUser.setToken(token1);
                ImServerConfig imServerConfig = ImServerConfig.Global.get();
                MessageHelper messageHelper = imServerConfig.getMessageHelper();
                LoginUser loginUserTemp = messageHelper.getUserLoginInfo(ImConst.USER_TOKEN, user.getUserId());
                if (loginUserTemp != null) {
                    loginUser.setPhoneImei(loginUserTemp.getPhoneImei());
                    loginUser.setSysVersion(loginUserTemp.getSysVersion());
                    loginUser.setAppVersion(loginUserTemp.getAppVersion());
                    loginUser.setTerminalType(loginUserTemp.getTerminalType());
                    loginUser.setContextId(loginUserTemp.getContextId());
                    loginUser.setReportTime(loginUserTemp.getReportTime());
                    Environment.setCurrentUser(loginUser);
                }

                AuthCacheService.saveAuth(loginUser);
                log.info("更新token成功!");
            }
        }
    }
}
