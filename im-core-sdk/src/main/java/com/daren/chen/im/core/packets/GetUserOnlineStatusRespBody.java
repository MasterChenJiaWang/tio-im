/**
 *
 */
package com.daren.chen.im.core.packets;

import org.apache.commons.lang3.StringUtils;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 获取用户状态响应消息体 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class GetUserOnlineStatusRespBody extends RespBody {

    private static final long serialVersionUID = 4795404819919830281L;
    public GetUserOnlineStatusResult result;
    public String userId;

    /**
     * 在线状态(online、offline)
     */
    private String status = UserStatusType.OFFLINE.getStatus();

    public GetUserOnlineStatusRespBody() {
        this(Command.GET_USER_ONLINE_STATUS_REQ_RESP, null);
    }

    public GetUserOnlineStatusRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.GET_USER_ONLINE_STATUS_REQ_RESP;
    }

    public GetUserOnlineStatusRespBody(Status status) {
        this(Command.GET_USER_ONLINE_STATUS_REQ_RESP, status);
    }

    public GetUserOnlineStatusRespBody(Command command, Status status) {
        super(command, status);
    }

    public GetUserOnlineStatusResult getResult() {
        return result;
    }

    public GetUserOnlineStatusRespBody setResult(GetUserOnlineStatusResult result) {
        this.result = result;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public GetUserOnlineStatusRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public GetUserOnlineStatusRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static GetUserOnlineStatusRespBody success() {
        GetUserOnlineStatusRespBody joinGroupRespBody = new GetUserOnlineStatusRespBody(ImStatus.C10032);
        joinGroupRespBody.setResult(GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_OK);
        return joinGroupRespBody;
    }

    public static GetUserOnlineStatusRespBody success(String userId) {
        GetUserOnlineStatusRespBody joinGroupRespBody = new GetUserOnlineStatusRespBody(ImStatus.C10032);
        joinGroupRespBody.setResult(GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_OK);
        joinGroupRespBody.setUserId(userId);
        return joinGroupRespBody;
    }

    public static GetUserOnlineStatusRespBody success(String userId, String status) {
        GetUserOnlineStatusRespBody joinGroupRespBody = new GetUserOnlineStatusRespBody(ImStatus.C10032);
        joinGroupRespBody.setResult(GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_OK);
        joinGroupRespBody.setUserId(userId);
        joinGroupRespBody.setStatus(StringUtils.isBlank(status) ? UserStatusType.OFFLINE.getStatus() : status);
        return joinGroupRespBody;
    }

    public static GetUserOnlineStatusRespBody failed() {
        GetUserOnlineStatusRespBody joinGroupRespBody = new GetUserOnlineStatusRespBody(ImStatus.C10033);
        joinGroupRespBody.setResult(GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_UNKNOWN);
        return joinGroupRespBody;
    }

    public static GetUserOnlineStatusRespBody failed(String userId) {
        GetUserOnlineStatusRespBody joinGroupRespBody = new GetUserOnlineStatusRespBody(ImStatus.C10033);
        joinGroupRespBody.setResult(GetUserOnlineStatusResult.GET_USER_ONLINE_STATUS_RESULT_UNKNOWN);
        joinGroupRespBody.setUserId(userId);
        return joinGroupRespBody;
    }
}
