/**
 *
 */
package com.daren.chen.im.core.packets;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 加入好友响应消息体 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class AddFriendRespBody extends RespBody {

    private static final long serialVersionUID = 5867884248517872477L;

    public AddFriendResult result;
    /**
     *
     */
    public String userId;

    /**
     * 添加的id
     */
    public String addUserId;

    public AddFriendRespBody() {
        this(Command.COMMAND_ADD_FRIEND_REQ_RESP, null);
    }

    public AddFriendRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.COMMAND_ADD_FRIEND_REQ_RESP;
    }

    public AddFriendRespBody(Status status) {
        this(Command.COMMAND_ADD_FRIEND_REQ_RESP, status);
    }

    public AddFriendRespBody(Command command, Status status) {
        super(command, status);
    }

    public AddFriendResult getResult() {
        return result;
    }

    public AddFriendRespBody setResult(AddFriendResult result) {
        this.result = result;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public String getAddUserId() {
        return addUserId;
    }

    public void setAddUserId(String addUserId) {
        this.addUserId = addUserId;
    }

    public AddFriendRespBody setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public AddFriendRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public AddFriendRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static AddFriendRespBody success() {
        AddFriendRespBody addFriendRespBody = new AddFriendRespBody(ImStatus.C10025);
        addFriendRespBody.setResult(AddFriendResult.ADD_FRIEND_RESULT_OK);
        return addFriendRespBody;
    }

    public static AddFriendRespBody success(String userId, String addUserId) {
        AddFriendRespBody addFriendRespBody = new AddFriendRespBody(ImStatus.C10025);
        addFriendRespBody.setResult(AddFriendResult.ADD_FRIEND_RESULT_OK);
        addFriendRespBody.setUserId(userId);
        addFriendRespBody.setAddUserId(addUserId);
        return addFriendRespBody;
    }

    public static AddFriendRespBody failed() {
        AddFriendRespBody addFriendRespBody = new AddFriendRespBody(ImStatus.C10026);
        addFriendRespBody.setResult(AddFriendResult.ADD_FRIEND_RESULT_UNKNOWN);
        return addFriendRespBody;
    }

    public static AddFriendRespBody failed(String userId, String addUserId) {
        AddFriendRespBody addFriendRespBody = new AddFriendRespBody(ImStatus.C10026);
        addFriendRespBody.setResult(AddFriendResult.ADD_FRIEND_RESULT_UNKNOWN);
        addFriendRespBody.setUserId(userId);
        addFriendRespBody.setAddUserId(addUserId);
        return addFriendRespBody;
    }
}
