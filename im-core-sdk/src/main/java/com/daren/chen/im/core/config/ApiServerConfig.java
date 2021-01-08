package com.daren.chen.im.core.config;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/19 13:47
 */

public class ApiServerConfig {

    private String url;

    /**
     * 是否使用mysql 保存数据
     */
    private boolean enabled = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ApiServerConfig(String url) {
        this.url = url;
    }

    public ApiServerConfig(String url, boolean enabled) {
        this.url = url;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ApiServerConfig() {}
}
