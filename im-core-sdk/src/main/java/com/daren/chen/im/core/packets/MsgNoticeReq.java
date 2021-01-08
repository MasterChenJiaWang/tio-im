/**
 *
 */
package com.daren.chen.im.core.packets;

import java.io.Serializable;
import java.util.List;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
public class MsgNoticeReq extends Message implements Serializable {

    private static final long serialVersionUID = -2856553189724631808L;
    /**
     * 用户ID
     */
    private String curUserId;

    /**
     * 消息类型 1：群组，2：个人
     */
    private String msgType;

    /**
     * 消息类型 1：添加好友申请通知，2：添加好友申请同意通知，3：添加好友拒绝通知,4:申请入群通知,5:申请入群同意通知,6:申请入群拒绝通知,7:解散群聊,8:退出群聊，9：被提出群聊，10：删除好友通知 11:解绑通知
     * 断开蓝牙
     */
    private String noticeType;

    /**
     * 通知的人
     */
    private String noticeUserId;

    /**
     * 通知的群组
     */
    private String noticeGroupId;

    /**
     * 消息通知ID
     */
    private String id;

    /**
     * 通知时间
     */
    private Long noticeTime;

    /**
     * 群对象
     */
    private Group groupInfo;

    private List<String> groupUserIds;

    /**
     * 1 安卓 2 server
     */
    private String noticeSource = "1";

    private String msgId;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Long getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(Long noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getCurUserId() {
        return curUserId;
    }

    public void setCurUserId(String curUserId) {
        this.curUserId = curUserId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeUserId() {
        return noticeUserId;
    }

    public void setNoticeUserId(String noticeUserId) {
        this.noticeUserId = noticeUserId;
    }

    public String getNoticeGroupId() {
        return noticeGroupId;
    }

    public void setNoticeGroupId(String noticeGroupId) {
        this.noticeGroupId = noticeGroupId;
    }

    public Group getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(Group groupInfo) {
        this.groupInfo = groupInfo;
    }

    public List<String> getGroupUserIds() {
        return groupUserIds;
    }

    public void setGroupUserIds(List<String> groupUserIds) {
        this.groupUserIds = groupUserIds;
    }

    public String getNoticeSource() {
        return noticeSource;
    }

    public void setNoticeSource(String noticeSource) {
        this.noticeSource = noticeSource;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
