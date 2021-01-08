/**
 *
 */
package com.daren.chen.im.core.packets;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年9月12日 下午2:50:23
 */
public class AuthRespBody extends RespBody {

    private static final long serialVersionUID = -2985356076555121875L;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthRespBody() {
        this.setCommand(Command.COMMAND_LOGIN_RESP);
    }

    public AuthRespBody(Status status) {
        this(status, null);
    }

    public AuthRespBody(Status status, String token) {
        super(Command.COMMAND_AUTH_RESP, status);
        this.token = token;
    }

    public static AuthRespBody success() {
        return new AuthRespBody(ImStatus.C10009);
    }

    public static AuthRespBody success(String token) {
        AuthRespBody authRespBody = new AuthRespBody(ImStatus.C10009);
        authRespBody.setToken(token);
        return authRespBody;
    }

    public static AuthRespBody failed() {
        return new AuthRespBody(ImStatus.C10010);
    }

    public static AuthRespBody failed(String msg) {
        AuthRespBody authRespBody = new AuthRespBody(ImStatus.C10010);
        authRespBody.setMsg(msg);
        return authRespBody;
    }

    @Override
    public AuthRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public AuthRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }
}
