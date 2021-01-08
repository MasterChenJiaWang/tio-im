package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.NoticeOfflineReq;
import com.daren.chen.im.core.packets.NoticeOfflineRespBody;
import com.daren.chen.im.core.packets.NoticeOfflineResult;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.notice.NoticeOfflineCmdProcessor;
import com.daren.chen.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0] 功能说明: 消息通知cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class NoticeOfflineReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(NoticeOfflineReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        // 绑定群组;
        try {
            NoticeOfflineRespBody noticeOfflineRespBody = null;
            NoticeOfflineReq noticeOfflineReq = JsonKit.toBean(packet.getBody(), NoticeOfflineReq.class);
            if (noticeOfflineReq == null) {
                log.error("上下文ID [{}] 用户ID [{}]   msgNoticeReq is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                noticeOfflineRespBody = NoticeOfflineRespBody.failed().setMsg("msgNoticeReq is null");
                return ProtocolManager.Converter.respPacket(noticeOfflineRespBody, imChannelContext);
            }
            // 实际绑定之前执行处理器动作
            NoticeOfflineCmdProcessor noticeOfflineCmdProcessor =
                this.getSingleProcessor(NoticeOfflineCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(noticeOfflineCmdProcessor)) {
                noticeOfflineRespBody = noticeOfflineCmdProcessor.offlineNotice(noticeOfflineReq, imChannelContext);
                boolean noticeOfflineIsTrue =
                    Objects.isNull(noticeOfflineRespBody) || NoticeOfflineResult.NOTICE_OFFLINE_RESULT_OK
                        .getNumber() != noticeOfflineRespBody.getResult().getNumber();
                if (noticeOfflineIsTrue) {
                    // 通知失败!
                    noticeOfflineRespBody = NoticeOfflineRespBody.failed().setData(noticeOfflineRespBody);
                    return ProtocolManager.Converter.respPacket(noticeOfflineRespBody, imChannelContext);
                } else {
                    // 通知成功成功!
                    noticeOfflineRespBody = noticeOfflineRespBody.setData(noticeOfflineRespBody);
                    // 发送通知
                    return ProtocolManager.Converter.respPacket(noticeOfflineRespBody, imChannelContext);
                }
            }
            // 没有通知成功
            noticeOfflineRespBody = NoticeOfflineRespBody.failed().setData(noticeOfflineRespBody);
            return ProtocolManager.Converter.respPacket(noticeOfflineRespBody, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ProtocolManager.Converter.respPacket(NoticeOfflineRespBody.failed().setMsg(e.getMessage()),
                imChannelContext);
        }
    }

    @Override
    public Command command() {
        return Command.NOTICE_OFFLINE_REQ;
    }
}
