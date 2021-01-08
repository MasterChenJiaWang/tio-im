package com.daren.chen.im.core.packets;

import java.io.Serializable;

/**
 * @author
 */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 4169798225409678616L;
    private String userId;

    private String userName;

    private String appId = "222222";

    private String appKey = "e10adc3949ba59abbe56e057f20f883e";

    private String token;

    /**
     * 手机imei
     */
    private String phoneImei;
    /**
     * 系统版本
     */
    private String sysVersion;
    /**
     * app版本
     */
    private String appVersion;
    /**
     * 终端类型(0：andriod，1：IOS)
     */
    private String terminalType;
    /**
     * 上下文id
     */
    private String contextId;
    /**
     * 上下线记录时间
     */
    private String reportTime;

    public String getPhoneImei() {
        return phoneImei;
    }

    public void setPhoneImei(String phoneImei) {
        this.phoneImei = phoneImei;
    }

    public String getSysVersion() {
        return sysVersion;
    }

    public void setSysVersion(String sysVersion) {
        this.sysVersion = sysVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LoginUser() {}

    public LoginUser(String userId, String token, String appId, String appKey) {
        this.userId = userId;
        this.appId = appId;
        this.appKey = appKey;
        this.token = token;
    }

    public LoginUser(String userId, String userName, String token, String appId, String appKey) {
        this.userId = userId;
        this.userName = userName;
        this.appId = appId;
        this.appKey = appKey;
        this.token = token;
    }

    public LoginUser(String appKey, String token) {
        this.appKey = appKey;
        this.token = token;
    }

    public LoginUser(String token, String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
        this.token = token;
    }
}
