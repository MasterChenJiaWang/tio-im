package com.daren.chen.im.server.springboot.http;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.http.HttpResponse;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.server.command.CommandManager;
import com.daren.chen.im.server.command.handler.MsgNoticeReqHandler;
import com.daren.chen.im.server.protocol.http.annotation.RequestPath;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/5 16:11
 */
@RequestPath(value = "/cup")
public class MyHttpApiController {

    /**
     *
     * @param request
     * @param httpConfig
     * @param channelContext
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/notice/send")
    public HttpResponse cupNoticeMessage(HttpRequest request, HttpConfig httpConfig, ImChannelContext channelContext)
        throws Exception {
        HttpResponse response = new HttpResponse(request, httpConfig);
        MsgNoticeReqHandler msgNoticeReqHandler =
            CommandManager.getCommand(Command.MSG_NOTICE_REQ, MsgNoticeReqHandler.class);
        ImPacket chatPacket = msgNoticeReqHandler.handler(request, channelContext);
        if (chatPacket != null) {
            response = (HttpResponse)chatPacket;
        }
        return response;
    }

}
