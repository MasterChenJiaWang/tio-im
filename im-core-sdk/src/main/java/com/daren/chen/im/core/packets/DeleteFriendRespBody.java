/**
 *
 */
package com.daren.chen.im.core.packets;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 删除好友响应消息体 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class DeleteFriendRespBody extends RespBody {

    private static final long serialVersionUID = 6635620192752369689L;
    public DeleteFriendResult result;
    /**
     *
     */
    public String operatorId;

    /**
     * 被删除者
     */
    public String deletedUserId;

    public DeleteFriendRespBody() {
        this(Command.COMMAND_DELETE_FRIEND_REQ_RESP, null);
    }

    public DeleteFriendRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.COMMAND_DELETE_FRIEND_REQ_RESP;
    }

    public DeleteFriendRespBody(Status status) {
        this(Command.COMMAND_DELETE_FRIEND_REQ_RESP, status);
    }

    public DeleteFriendRespBody(Command command, Status status) {
        super(command, status);
    }

    public DeleteFriendResult getResult() {
        return result;
    }

    public DeleteFriendRespBody setResult(DeleteFriendResult result) {
        this.result = result;
        return this;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDeletedUserId() {
        return deletedUserId;
    }

    public void setDeletedUserId(String deletedUserId) {
        this.deletedUserId = deletedUserId;
    }

    @Override
    public DeleteFriendRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public DeleteFriendRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static DeleteFriendRespBody success() {
        DeleteFriendRespBody joinGroupRespBody = new DeleteFriendRespBody(ImStatus.C10027);
        joinGroupRespBody.setResult(DeleteFriendResult.DELETE_FRIEND_RESULT_OK);
        return joinGroupRespBody;
    }

    public static DeleteFriendRespBody success(String operatorId, String deletedUserId) {
        DeleteFriendRespBody joinGroupRespBody = new DeleteFriendRespBody(ImStatus.C10027);
        joinGroupRespBody.setResult(DeleteFriendResult.DELETE_FRIEND_RESULT_OK);
        joinGroupRespBody.setOperatorId(operatorId);
        joinGroupRespBody.setDeletedUserId(deletedUserId);
        return joinGroupRespBody;
    }

    public static DeleteFriendRespBody failed() {
        DeleteFriendRespBody joinGroupRespBody = new DeleteFriendRespBody(ImStatus.C10028);
        joinGroupRespBody.setResult(DeleteFriendResult.DELETE_FRIEND_RESULT_UNKNOWN);
        return joinGroupRespBody;
    }

    public static DeleteFriendRespBody failed(String operatorId, String deletedUserId) {
        DeleteFriendRespBody joinGroupRespBody = new DeleteFriendRespBody(ImStatus.C10028);
        joinGroupRespBody.setResult(DeleteFriendResult.DELETE_FRIEND_RESULT_UNKNOWN);
        joinGroupRespBody.setOperatorId(operatorId);
        joinGroupRespBody.setDeletedUserId(deletedUserId);
        return joinGroupRespBody;
    }
}
