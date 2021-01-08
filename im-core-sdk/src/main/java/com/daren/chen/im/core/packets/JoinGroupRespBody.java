/**
 *
 */
package com.daren.chen.im.core.packets;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明: 加入群组响应消息体 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class JoinGroupRespBody extends RespBody {

    private static final long serialVersionUID = 3940185593784125976L;
    public JoinGroupResult result;
    public String group;

    public JoinGroupRespBody() {
        this(Command.COMMAND_JOIN_GROUP_RESP, null);
    }

    public JoinGroupRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.COMMAND_JOIN_GROUP_RESP;
    }

    public JoinGroupRespBody(Status status) {
        this(Command.COMMAND_JOIN_GROUP_RESP, status);
    }

    public JoinGroupRespBody(Command command, Status status) {
        super(command, status);
    }

    public JoinGroupResult getResult() {
        return result;
    }

    public JoinGroupRespBody setResult(JoinGroupResult result) {
        this.result = result;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public JoinGroupRespBody setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public JoinGroupRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    public static JoinGroupRespBody success() {
        JoinGroupRespBody joinGroupRespBody = new JoinGroupRespBody(ImStatus.C10011);
        joinGroupRespBody.setResult(JoinGroupResult.JOIN_GROUP_RESULT_OK);
        return joinGroupRespBody;
    }

    public static JoinGroupRespBody failed() {
        JoinGroupRespBody joinGroupRespBody = new JoinGroupRespBody(ImStatus.C10012);
        joinGroupRespBody.setResult(JoinGroupResult.JOIN_GROUP_RESULT_UNKNOWN);
        return joinGroupRespBody;
    }

    public static JoinGroupRespBody failed(String msg) {
        JoinGroupRespBody joinGroupRespBody = new JoinGroupRespBody(ImStatus.C10012);
        joinGroupRespBody.setResult(JoinGroupResult.JOIN_GROUP_RESULT_UNKNOWN);
        joinGroupRespBody.setMsg(msg);
        return joinGroupRespBody;
    }
}
