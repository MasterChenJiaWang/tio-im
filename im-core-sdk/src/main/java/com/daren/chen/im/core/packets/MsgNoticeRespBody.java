/**
 *
 */
package com.daren.chen.im.core.packets;

import java.io.Serializable;
import java.util.List;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
public class MsgNoticeRespBody extends RespBody implements Serializable {

    private static final long serialVersionUID = 6682424850320282897L;
    public MsgNoticeResult result;

    /**
     * 消息来源人
     */
    private String fromUserId;
    /**
     * 消息类型 1：群组，2：个人
     */
    private String msgType;

    /**
     * 消息类型 1：添加好友申请通知，2：添加好友申请同意通知，3：添加好友拒绝通知,4:申请入群通知,5:申请入群同意通知,6:申请入群拒绝通知,7:解散群聊,8:退出群聊，9：被提出群聊，10：删除好友通知 11:解绑通知
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

    private List<String> groupUserIds;

    private List<User> groupUserInfoList;

    private User noticeUserInfo;

    private User fromUserInfo;

    private Group groupInfo;

    private String msgId;

    public Long getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(Long noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getGroupUserIds() {
        return groupUserIds;
    }

    public Group getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(Group groupInfo) {
        this.groupInfo = groupInfo;
    }

    public void setGroupUserIds(List<String> groupUserIds) {
        this.groupUserIds = groupUserIds;
    }

    public List<User> getGroupUserInfoList() {
        return groupUserInfoList;
    }

    public void setGroupUserInfoList(List<User> groupUserInfoList) {
        this.groupUserInfoList = groupUserInfoList;
    }

    public User getNoticeUserInfo() {
        return noticeUserInfo;
    }

    public void setNoticeUserInfo(User noticeUserInfo) {
        this.noticeUserInfo = noticeUserInfo;
    }

    public User getFromUserInfo() {
        return fromUserInfo;
    }

    public void setFromUserInfo(User fromUserInfo) {
        this.fromUserInfo = fromUserInfo;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MsgNoticeRespBody() {
        this(Command.MSG_NOTICE_REQ_RESP, null);
    }

    public MsgNoticeRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.MSG_NOTICE_REQ_RESP;
    }

    public MsgNoticeRespBody(Status status) {
        this(Command.MSG_NOTICE_REQ_RESP, status);
    }

    public MsgNoticeRespBody(Command command, Status status) {
        super(command, status);
    }

    public MsgNoticeResult getResult() {
        return result;
    }

    public MsgNoticeRespBody setResult(MsgNoticeResult result) {
        this.result = result;
        return this;
    }

    @Override
    public MsgNoticeRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public MsgNoticeRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static MsgNoticeRespBody success() {
        MsgNoticeRespBody msgNoticeRespBody = new MsgNoticeRespBody(ImStatus.C10034);
        msgNoticeRespBody.setResult(MsgNoticeResult.MSG_NOTICE_RESULT_OK);
        return msgNoticeRespBody;
    }

    public static MsgNoticeRespBody success(MsgNoticeRespBody data) {
        MsgNoticeRespBody msgNoticeRespBody = new MsgNoticeRespBody(ImStatus.C10034);
        msgNoticeRespBody.setResult(MsgNoticeResult.MSG_NOTICE_RESULT_OK);
        msgNoticeRespBody.setData(data);
        return msgNoticeRespBody;
    }

    public static MsgNoticeRespBody failed() {
        MsgNoticeRespBody msgNoticeRespBody = new MsgNoticeRespBody(ImStatus.C10035);
        msgNoticeRespBody.setResult(MsgNoticeResult.MSG_NOTICE_RESULT_UNKNOWN);
        return msgNoticeRespBody;
    }

}
