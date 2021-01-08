/**
 *
 */
package com.daren.chen.im.core.packets;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年9月12日 下午2:49:49
 */
public class AuthReqBody extends Message {

    private static final long serialVersionUID = -5687459633884615894L;

    /**
     *
     */
    private String token;// token验证;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthReqBody() {}

    public AuthReqBody(String token) {
        this.token = token;
        this.cmd = Command.COMMAND_AUTH_REQ.getNumber();
    }

    public AuthReqBody(String token, String appId, String appKey) {
        this(token);
        this.appId = appId;
        this.appKey = appKey;
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
