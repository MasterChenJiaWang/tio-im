package com.daren.chen.im.server.util;

import com.daren.chen.im.core.packets.LoginUser;

/**
 *
 * @author
 */
public class Environment {
    private static final ThreadLocal<LoginUser> loginUserThreadLocal = new ThreadLocal<>();

    /**
     *
     * @return
     */
    public static LoginUser getCurrentUser() {
        return loginUserThreadLocal.get();
    }

    /**
     *
     * @return
     */
    public static String getCurrentUserName() {
        LoginUser user = loginUserThreadLocal.get();
        return user == null ? null : user.getUserName();
    }

    /**
     *
     * @return
     */
    public static String getAppId() {
        LoginUser user = loginUserThreadLocal.get();
        return user == null ? null : user.getAppId();
    }

    /**
     *
     * @return
     */
    public static String getAppKey() {
        LoginUser user = loginUserThreadLocal.get();
        return user == null ? null : user.getAppKey();
    }

    /**
     *
     * @return
     */
    public static String getToken() {
        LoginUser user = loginUserThreadLocal.get();
        return user == null ? null : user.getToken();
    }

    /**
     *
     * @param user
     */
    public static void setCurrentUser(LoginUser user) {
        loginUserThreadLocal.set(user);
    }

    public static void remove() {
        loginUserThreadLocal.remove();
    }

}
