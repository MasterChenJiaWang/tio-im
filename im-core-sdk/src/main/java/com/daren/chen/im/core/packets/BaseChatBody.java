package com.daren.chen.im.core.packets;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/23 9:19
 */
public class BaseChatBody extends Message {

    private static final long serialVersionUID = 4920473909922663661L;
    /**
     * 发送用户id;
     */
    protected String from;
    /**
     * 目标用户id;
     */
    protected String to;
    /**
     * 消息发到哪个群组;
     */
    protected String groupId;

    /**
     * 数据类型 1: 消息 2 : 消息响应
     */
    protected Short dataType;

    public Short getDataType() {
        return dataType;
    }

    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
