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
public class AddUser extends Message implements Serializable {

    private static final long serialVersionUID = -6515237118751529195L;
    private String curUserId;
    private String friendUserId;

    public String getCurUserId() {
        return curUserId;
    }

    public void setCurUserId(String curUserId) {
        this.curUserId = curUserId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }
}
