/**
 *
 */
package com.daren.chen.im.server.processor.auth;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.packets.AuthReqBody;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.server.processor.SingleProtocolCmdProcessor;

/**
 *
 * @author WChao
 */
public interface AuthCmdProcessor extends SingleProtocolCmdProcessor {
    /**
     * 执行检查token操作接口方法
     *
     * @param authReqBody
     * @param imChannelContext
     * @return
     */
    AuthRespBody checkToken(AuthReqBody authReqBody, ImChannelContext imChannelContext);

}
