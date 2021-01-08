package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.ExitGroupReqBody;
import com.daren.chen.im.core.packets.ExitGroupRespBody;
import com.daren.chen.im.core.packets.ExitGroupResult;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.group.GroupCmdProcessor;

/**
 * 版本: [1.0] 功能说明: 离开群组消息cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class ExitGroupReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(ExitGroupReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            // 绑定群组;
            ExitGroupRespBody exitGroupRespBody = null;
            ExitGroupReqBody exitGroup = JsonKit.toBean(packet.getBody(), ExitGroupReqBody.class);
            if (exitGroup == null) {
                log.error("上下文ID [{}] 用户ID [{}]   group info null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            //
            String groupId = exitGroup.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                log.error("上下文ID [{}] 用户ID [{}]   group id null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            // 实际绑定之前执行处理器动作
            GroupCmdProcessor groupProcessor = this.getSingleProcessor(GroupCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            if (Objects.nonNull(groupProcessor)) {
                exitGroupRespBody = groupProcessor.exit(exitGroup, imChannelContext);
                boolean joinGroupIsTrue = Objects.isNull(exitGroupRespBody)
                    || ExitGroupResult.EXIT_GROUP_RESULT_OK.getNumber() != exitGroupRespBody.getResult().getNumber();
                if (joinGroupIsTrue) {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Command command() {

        return Command.COMMAND_EXIT_GROUP_REQ;
    }
}
