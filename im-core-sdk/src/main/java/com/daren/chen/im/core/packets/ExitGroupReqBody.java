/**
 *
 */
package com.daren.chen.im.core.packets;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年9月12日 下午2:49:49
 */
public class ExitGroupReqBody extends Message {

    private static final long serialVersionUID = 7564469465865731199L;
    /**
     * 群组ID
     */
    private String groupId;

    /**
     *
     */
    private String userId;

    public ExitGroupReqBody() {}

    public ExitGroupReqBody(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
