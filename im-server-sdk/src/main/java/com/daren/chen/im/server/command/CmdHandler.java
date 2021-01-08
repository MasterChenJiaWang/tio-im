package com.daren.chen.im.server.command;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;

/**
 *
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年9月8日 下午4:29:38
 */
public interface CmdHandler {
    /**
     * 功能描述：[命令主键]
     *
     * @author：WChao 创建时间: 2017年7月17日 下午2:31:51
     * @return
     */
    Command command();

    /**
     * 处理Cmd命令
     *
     * @param imPacket
     * @param imChannelContext
     * @return
     * @throws ImException
     * @date 2016年11月18日 下午1:08:45
     */
    ImPacket handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException;

}
