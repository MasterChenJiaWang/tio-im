/**
 *
 */
package com.daren.chen.im.server.processor.login;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.packets.LoginRespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 *
 * @author WChao
 */
public interface LoginCmdProcessor extends SingleProtocolCmdProcessor {
    /**
     * 执行登录操作接口方法
     *
     * @param loginReqBody
     * @param imChannelContext
     * @return
     */
    LoginRespBody doLogin(LoginReqBody loginReqBody, ImChannelContext imChannelContext);

    /**
     * 获取用户信息接口方法
     *
     * @param loginReqBody
     * @param imChannelContext
     * @return
     */
    User getUser(LoginReqBody loginReqBody, ImChannelContext imChannelContext);

    /**
     * 登录成功(指的是JIM会在用户校验完登陆逻辑后进行JIM内部绑定)回调方法
     *
     * @param imChannelContext
     */
    void onSuccess(User user, ImChannelContext imChannelContext);

    /**
     * 登陆失败回调方法
     *
     * @param imChannelContext
     */
    void onFailed(ImChannelContext imChannelContext);
}
