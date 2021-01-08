/**
 *
 */
package com.daren.chen.im.core.packets;

/**
 * 版本: [1.0] 功能说明: 登陆命令请求包体 作者: WChao 创建时间: 2017年9月12日 下午3:13:22
 */
public class LoginReqBody extends Message {

    private static final long serialVersionUID = -10113316720288444L;
    /**
     * 用户Id
     */
    private String userId;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 密码
     */
    private String password;
    /**
     * 登陆token
     */
    private String token;

    private String appId;

    private String appKey;

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

    public LoginReqBody() {}

    public LoginReqBody(String token) {
        this.token = token;
        this.cmd = Command.COMMAND_LOGIN_REQ.getNumber();
    }

    public LoginReqBody(String userId, String password) {
        this.userId = userId;
        this.password = password;
        this.cmd = Command.COMMAND_LOGIN_REQ.getNumber();
    }

    public LoginReqBody(String userId, String password, String token) {
        this(userId, password);
        this.token = token;
    }

    public LoginReqBody(String phone, String token, String appId, String appKey) {
        this.phone = phone;
        this.token = token;
        this.appId = appId;
        this.appKey = appKey;
        this.cmd = Command.COMMAND_LOGIN_REQ.getNumber();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
