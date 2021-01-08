/**
 *
 */
package com.daren.chen.im.server.protocol.http.api;

import org.tio.core.ChannelContext;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.http.HttpConfig;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.http.HttpResponse;
import com.daren.chen.im.core.packets.CloseReqBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.command.CommandManager;
import com.daren.chen.im.server.command.handler.ChatReqHandler;
import com.daren.chen.im.server.command.handler.CloseReqHandler;
import com.daren.chen.im.server.command.handler.MessageReqHandler;
import com.daren.chen.im.server.protocol.http.annotation.RequestPath;
import com.daren.chen.im.server.util.HttpResps;

/**
 * 版本: [1.0] 功能说明: Http协议消息发送控制器类
 *
 * @author : WChao 创建时间: 2017年8月8日 上午9:08:48
 */
@RequestPath(value = "/api")
public class HttpApiController {

    @RequestPath(value = "/message/send")
    public HttpResponse chat(HttpRequest request, HttpConfig httpConfig, ImChannelContext channelContext)
        throws Exception {
        HttpResponse response = new HttpResponse(request, httpConfig);
        ChatReqHandler chatReqHandler = CommandManager.getCommand(Command.COMMAND_CHAT_REQ, ChatReqHandler.class);
        ImPacket chatPacket = chatReqHandler.handler(request, channelContext);
        if (chatPacket != null) {
            response = (HttpResponse)chatPacket;
        }
        return response;
    }

    @RequestPath(value = "/message/online")
    public HttpResponse onlineMessage(HttpRequest request, HttpConfig httpConfig, ImChannelContext channelContext)
        throws Exception {
        HttpResponse response = new HttpResponse(request, httpConfig);
        MessageReqHandler messageReqHandler =
            CommandManager.getCommand(Command.COMMAND_GET_MESSAGE_REQ, MessageReqHandler.class);
        ImPacket chatPacket = messageReqHandler.handler(request, channelContext);
        if (chatPacket != null) {
            response = (HttpResponse)chatPacket;
        }
        return response;
    }

    /**
     * 判断用户是否在线接口;
     *
     * @param request
     * @param httpConfig
     * @param channelContext
     * @return
     * @throws Exception
     */
    @RequestPath(value = "/user/online")
    public HttpResponse online(HttpRequest request, HttpConfig httpConfig, ChannelContext channelContext)
        throws Exception {
        Object[] params = request.getParams().get("userid");
        if (params == null || params.length == 0) {
            return HttpResps.json(request, new RespBody(ImStatus.C10020));
        }
        String userId = params[0].toString();
        User user = null/*Jim.getUser(userId)*/;
        if (user != null) {
            return HttpResps.json(request, new RespBody(ImStatus.C10019));
        } else {
            return HttpResps.json(request, new RespBody(ImStatus.C10001));
        }
    }

    /**
     * 关闭指定用户;
     *
     * @param request
     * @param httpConfig
     * @param channelContext
     * @return
     * @throws Exception
     */
    @RequestPath(value = "user/close")
    public HttpResponse close(HttpRequest request, HttpConfig httpConfig, ImChannelContext channelContext)
        throws Exception {
        Object[] params = request.getParams().get("userid");
        if (params == null || params.length == 0) {
            return HttpResps.json(request, new RespBody(ImStatus.C10020));
        }
        String userId = params[0].toString();
        ImPacket closePacket = new ImPacket(Command.COMMAND_CLOSE_REQ, new CloseReqBody(userId).toByte());
        CloseReqHandler closeReqHandler = CommandManager.getCommand(Command.COMMAND_CLOSE_REQ, CloseReqHandler.class);
        closeReqHandler.handler(closePacket, channelContext);
        return HttpResps.json(request, new RespBody(ImStatus.C10021));
    }

}
