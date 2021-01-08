/**
 *
 */
package com.daren.chen.im.core.packets;

import java.io.Serializable;

import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.Status;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
public class NoticeOfflineRespBody extends RespBody implements Serializable {

    private static final long serialVersionUID = 2078700961646519251L;

    public NoticeOfflineResult result;

    public NoticeOfflineRespBody() {
        this(Command.NOTICE_OFFLINE_REQ_RESP, null);
    }

    public NoticeOfflineRespBody(Integer code, String msg) {
        super(code, msg);
        this.command = Command.NOTICE_OFFLINE_REQ_RESP;
    }

    public NoticeOfflineRespBody(Status status) {
        this(Command.NOTICE_OFFLINE_REQ_RESP, status);
    }

    public NoticeOfflineRespBody(Command command, Status status) {
        super(command, status);
    }

    public NoticeOfflineResult getResult() {
        return result;
    }

    public NoticeOfflineRespBody setResult(NoticeOfflineResult result) {
        this.result = result;
        return this;
    }

    @Override
    public NoticeOfflineRespBody setData(Object data) {
        super.setData(data);
        return this;
    }

    @Override
    public NoticeOfflineRespBody setMsg(String msg) {
        super.setMsg(msg);
        return this;
    }

    public static NoticeOfflineRespBody success() {
        NoticeOfflineRespBody msgNoticeRespBody = new NoticeOfflineRespBody(ImStatus.C10036);
        msgNoticeRespBody.setResult(NoticeOfflineResult.NOTICE_OFFLINE_RESULT_OK);
        return msgNoticeRespBody;
    }

    public static NoticeOfflineRespBody success(NoticeOfflineRespBody data) {
        NoticeOfflineRespBody msgNoticeRespBody = new NoticeOfflineRespBody(ImStatus.C10036);
        msgNoticeRespBody.setResult(NoticeOfflineResult.NOTICE_OFFLINE_RESULT_OK);
        msgNoticeRespBody.setData(data);
        return msgNoticeRespBody;
    }

    public static NoticeOfflineRespBody failed() {
        NoticeOfflineRespBody msgNoticeRespBody = new NoticeOfflineRespBody(ImStatus.C10037);
        msgNoticeRespBody.setResult(NoticeOfflineResult.NOTICE_OFFLINE_RESULT_UNKNOWN);
        return msgNoticeRespBody;
    }

}
