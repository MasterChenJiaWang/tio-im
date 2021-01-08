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
public class ReceiveMsgNoticeRespBody extends RespBody implements Serializable {

    private static final long serialVersionUID = 944258233571236176L;
    public ReceiveMsgNoticeResult result;

    private List<MsgNoticeRespBody> list;

    public List<MsgNoticeRespBody> getList() {
        return list;
    }

    public void setList(List<MsgNoticeRespBody> list) {
        this.list = list;
    }

    public ReceiveMsgNoticeRespBody() {
        this(Command.RECEIVE_MSG_NOTICE_REQ_RESP, null);
    }

    public ReceiveMsgNoticeRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.RECEIVE_MSG_NOTICE_REQ_RESP;
    }

    public ReceiveMsgNoticeRespBody(Status status) {
        this(Command.RECEIVE_MSG_NOTICE_REQ_RESP, status);
    }

    public ReceiveMsgNoticeRespBody(Command command, Status status) {
        super(command, status);
    }

    public ReceiveMsgNoticeResult getResult() {
        return result;
    }

    public ReceiveMsgNoticeRespBody setResult(ReceiveMsgNoticeResult result) {
        this.result = result;
        return this;
    }

    @Override
    public ReceiveMsgNoticeRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public ReceiveMsgNoticeRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static ReceiveMsgNoticeRespBody success() {
        ReceiveMsgNoticeRespBody msgNoticeRespBody = new ReceiveMsgNoticeRespBody(ImStatus.C10038);
        msgNoticeRespBody.setResult(ReceiveMsgNoticeResult.RECEIVE_MSG_NOTICE_RESULT_OK);
        return msgNoticeRespBody;
    }

    public static ReceiveMsgNoticeRespBody success(ReceiveMsgNoticeRespBody data) {
        ReceiveMsgNoticeRespBody msgNoticeRespBody = new ReceiveMsgNoticeRespBody(ImStatus.C10038);
        msgNoticeRespBody.setResult(ReceiveMsgNoticeResult.RECEIVE_MSG_NOTICE_RESULT_OK);
        msgNoticeRespBody.setData(data);
        return msgNoticeRespBody;
    }

    public static ReceiveMsgNoticeRespBody failed() {
        ReceiveMsgNoticeRespBody msgNoticeRespBody = new ReceiveMsgNoticeRespBody(ImStatus.C10039);
        msgNoticeRespBody.setResult(ReceiveMsgNoticeResult.RECEIVE_MSG_NOTICE_RESULT_UNKNOWN);
        return msgNoticeRespBody;
    }

}
