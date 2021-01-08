package com.daren.chen.im.core.packets;

import java.io.Serializable;

import com.daren.chen.im.core.Status;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/26 11:14
 */
public class AdminStatisticsRespBody extends RespBody implements Serializable {

    private static final long serialVersionUID = -7225775789958041925L;
    /**
     *
     */
    private String userId;

    /**
     *
     */
    private String userName;

    /**
     * 在线状态(online、offline)
     */
    private String status = UserStatusType.OFFLINE.getStatus();

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户所属终端;(ws、tcp、http、android、ios等)
     */
    private String terminal;

    /**
     *
     */
    private Long friendNum;

    /**
     *
     */
    private Long groupNum;

    /**
     * 总消息数
     */
    private Long chatTotalNum;

    /**
     * 好友消息数量
     */
    private Long chatfriendNum;

    /**
     * 发送的好友消息
     */
    private Long sendChatfriendNum;

    /**
     * 收到的好友消息
     */
    private Long receiveChatfriendNum;

    /**
     * 群组消息数量
     */
    private Long chatGroupNum;

    /**
     * 发送的组消息
     */
    private Long sendchatGroupNum;

    /**
     * 收到的组消息
     */
    private Long receivechatGroupNum;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public Long getFriendNum() {
        return friendNum;
    }

    public void setFriendNum(Long friendNum) {
        this.friendNum = friendNum;
    }

    public Long getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(Long groupNum) {
        this.groupNum = groupNum;
    }

    public Long getChatTotalNum() {
        // return chatTotalNum;
        return (this.chatfriendNum == null ? 0 : this.chatfriendNum)
            + (this.chatGroupNum == null ? 0 : this.chatGroupNum);
    }

    public void setChatTotalNum(Long chatTotalNum) {
        this.chatTotalNum = chatTotalNum;
    }

    public Long getChatfriendNum() {
        // return chatfriendNum;
        return (this.sendChatfriendNum == null ? 0L : this.sendChatfriendNum)
            + (this.receiveChatfriendNum == null ? 0L : this.receiveChatfriendNum);
    }

    public void setChatfriendNum(Long chatfriendNum) {
        this.chatfriendNum = chatfriendNum;
    }

    public Long getSendChatfriendNum() {
        return sendChatfriendNum;
    }

    public void setSendChatfriendNum(Long sendChatfriendNum) {
        this.sendChatfriendNum = sendChatfriendNum;
    }

    public Long getReceiveChatfriendNum() {
        return receiveChatfriendNum;
    }

    public void setReceiveChatfriendNum(Long receiveChatfriendNum) {
        this.receiveChatfriendNum = receiveChatfriendNum;
    }

    public Long getChatGroupNum() {
        // return chatGroupNum;
        return (this.sendchatGroupNum == null ? 0 : this.sendchatGroupNum)
            + (this.receivechatGroupNum == null ? 0 : this.receivechatGroupNum);
    }

    public void setChatGroupNum(Long chatGroupNum) {
        this.chatGroupNum = chatGroupNum;
    }

    public Long getSendchatGroupNum() {
        return sendchatGroupNum;
    }

    public void setSendchatGroupNum(Long sendchatGroupNum) {
        this.sendchatGroupNum = sendchatGroupNum;
    }

    public Long getReceivechatGroupNum() {
        return receivechatGroupNum;
    }

    public void setReceivechatGroupNum(Long receivechatGroupNum) {
        this.receivechatGroupNum = receivechatGroupNum;
    }

    public AdminStatisticsRespBody(String userId, String userName, String status, String avatar, String terminal,
        Long friendNum, Long groupNum, Long chatTotalNum, Long chatfriendNum, Long chatGroupNum) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.avatar = avatar;
        this.terminal = terminal;
        this.friendNum = friendNum;
        this.groupNum = groupNum;
        this.chatTotalNum = chatTotalNum;
        this.chatfriendNum = chatfriendNum;
        this.chatGroupNum = chatGroupNum;
    }

    public AdminStatisticsRespBody(String userId, Long sendChatfriendNum, Long receiveChatfriendNum,
        Long sendchatGroupNum, Long receivechatGroupNum, String status) {
        this();
        this.userId = userId;
        this.sendChatfriendNum = sendChatfriendNum;
        this.receiveChatfriendNum = receiveChatfriendNum;
        this.sendchatGroupNum = sendchatGroupNum;
        this.receivechatGroupNum = receivechatGroupNum;
        this.status = status;
    }

    public AdminStatisticsRespBody(String userId, Long friendNum, Long groupNum) {
        this();
        this.userId = userId;
        this.friendNum = friendNum;
        this.groupNum = groupNum;
    }

    public AdminStatisticsRespBody(String userId, String status) {
        this();
        this.userId = userId;
        this.status = status;

    }

    @Override
    public AdminStatisticsRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public AdminStatisticsRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public AdminStatisticsRespBody() {
        this.setCommand(Command.ADMIN_MSG_REQ);
    }

    public AdminStatisticsRespBody(Command command, Status status) {
        super(command, status);
    }
}
