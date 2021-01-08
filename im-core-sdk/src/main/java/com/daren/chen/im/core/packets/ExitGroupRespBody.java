/**
 *
 */
package com.daren.chen.im.core.packets;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 退出群组响应消息体 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class ExitGroupRespBody extends RespBody {

    private static final long serialVersionUID = -758677839878412908L;
    public ExitGroupResult result;
    public String group;
    /**
     *
     */
    private String suerId;
    /**
     * 是否是解散
     */
    private boolean disband;

    public ExitGroupRespBody() {
        this(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, null);
    }

    public ExitGroupRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.COMMAND_EXIT_GROUP_NOTIFY_RESP;
    }

    public ExitGroupRespBody(Status status) {
        this(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, status);
    }

    public ExitGroupRespBody(Command command, Status status) {
        super(command, status);
    }

    public ExitGroupResult getResult() {
        return result;
    }

    public ExitGroupRespBody setResult(ExitGroupResult result) {
        this.result = result;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ExitGroupRespBody setGroup(String group) {
        this.group = group;
        return this;
    }

    public boolean isDisband() {
        return disband;
    }

    public void setDisband(boolean disband) {
        this.disband = disband;
    }

    public String getSuerId() {
        return suerId;
    }

    public void setSuerId(String suerId) {
        this.suerId = suerId;
    }

    @Override
    public ExitGroupRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public ExitGroupRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static ExitGroupRespBody success() {
        ExitGroupRespBody joinGroupRespBody = new ExitGroupRespBody(ImStatus.C10024);
        joinGroupRespBody.setResult(ExitGroupResult.EXIT_GROUP_RESULT_OK);
        return joinGroupRespBody;
    }

    public static ExitGroupRespBody success(String group) {
        ExitGroupRespBody joinGroupRespBody = new ExitGroupRespBody(ImStatus.C10024);
        joinGroupRespBody.setResult(ExitGroupResult.EXIT_GROUP_RESULT_OK);
        joinGroupRespBody.setGroup(group);
        return joinGroupRespBody;
    }

    public static ExitGroupRespBody success(String userId, String group, boolean isDisband) {
        ExitGroupRespBody joinGroupRespBody = new ExitGroupRespBody(ImStatus.C10024);
        joinGroupRespBody.setResult(ExitGroupResult.EXIT_GROUP_RESULT_OK);
        joinGroupRespBody.setSuerId(userId);
        joinGroupRespBody.setGroup(group);
        joinGroupRespBody.setDisband(isDisband);
        return joinGroupRespBody;
    }

    public static ExitGroupRespBody failed() {
        ExitGroupRespBody joinGroupRespBody = new ExitGroupRespBody(ImStatus.C10029);
        joinGroupRespBody.setResult(ExitGroupResult.EXIT_GROUP_RESULT_UNKNOWN);
        return joinGroupRespBody;
    }

}
