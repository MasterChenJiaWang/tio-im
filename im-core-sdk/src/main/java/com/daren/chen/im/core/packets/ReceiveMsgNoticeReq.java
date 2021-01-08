/**
 *
 */
package com.daren.chen.im.core.packets;

import java.io.Serializable;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
public class ReceiveMsgNoticeReq extends Message implements Serializable {

    private static final long serialVersionUID = 1005221685659660186L;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
