package com.daren.chen.im.client.test;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/11/19 20:09
 */
public class UserToken {
    private String userId;

    private String token;

    public UserToken() {}

    public UserToken(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
