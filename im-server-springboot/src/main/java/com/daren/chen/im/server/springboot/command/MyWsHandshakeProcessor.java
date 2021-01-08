/**
 *
 */
package com.daren.chen.im.server.springboot.command;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.http.HttpRequest;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.JimServerAPI;
import com.daren.chen.im.server.command.CommandManager;
import com.daren.chen.im.server.command.handler.LoginReqHandler;
import com.daren.chen.im.server.processor.handshake.WsHandshakeProcessor;

/**
 * @author WChao
 *
 */
public class MyWsHandshakeProcessor extends WsHandshakeProcessor {

    @Override
    public void onAfterHandshake(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        LoginReqHandler loginHandler = (LoginReqHandler)CommandManager.getCommand(Command.COMMAND_LOGIN_REQ);
        HttpRequest request = (HttpRequest)packet;
        String username =
            request.getParams().get("username") == null ? null : (String)request.getParams().get("username")[0];
        String phone = request.getParams().get("phone") == null ? null : (String)request.getParams().get("phone")[0];
        String appId = request.getParams().get("appId") == null ? null : (String)request.getParams().get("appId")[0];
        String appKey = request.getParams().get("appKey") == null ? null : (String)request.getParams().get("appKey")[0];
        String token = request.getParams().get("token") == null ? null : (String)request.getParams().get("token")[0];
        LoginReqBody loginBody = new LoginReqBody(phone, token, appId, appKey);
        byte[] loginBytes = JsonKit.toJsonBytes(loginBody);
        request.setBody(loginBytes);
        try {
            request.setBodyString(new String(loginBytes, ImConst.CHARSET));
        } catch (Exception e) {
            throw new ImException(e);
        }
        ImPacket loginRespPacket = loginHandler.handler(request, imChannelContext);
        if (loginRespPacket != null) {
            JimServerAPI.send(imChannelContext, loginRespPacket);
        }
    }
}
