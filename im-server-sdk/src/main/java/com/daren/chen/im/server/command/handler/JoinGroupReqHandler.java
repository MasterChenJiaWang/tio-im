package com.daren.chen.im.server.command.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.JoinGroupReqBody;
import com.daren.chen.im.core.packets.JoinGroupRespBody;
import com.daren.chen.im.core.packets.JoinGroupResult;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.processor.group.GroupCmdProcessor;

import cn.hutool.core.bean.BeanUtil;

/**
 *
 * 版本: [1.0] 功能说明: 加入群组消息cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
public class JoinGroupReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(JoinGroupReqHandler.class);

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        JoinGroupRespBody joinGroupRespBody = null;
        try {
            // 绑定群组;
            JoinGroupReqBody joinGroupReqBody = JsonKit.toBean(packet.getBody(), JoinGroupReqBody.class);
            if (joinGroupReqBody == null) {
                log.error("上下文ID [{}] 用户ID [{}]   group is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            String groupId = joinGroupReqBody.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                log.error("上下文ID [{}] 用户ID [{}]   group is null", imChannelContext.getId(),
                    imChannelContext.getUserId());
                return null;
            }
            // 实际绑定之前执行处理器动作
            GroupCmdProcessor groupProcessor = this.getSingleProcessor(GroupCmdProcessor.class);
            // 当有群组处理器时候才会去处理
            //
            Group group = Group.newBuilder().build();
            BeanUtil.copyProperties(joinGroupReqBody, group);
            //
            if (Objects.nonNull(groupProcessor)) {
                joinGroupRespBody = groupProcessor.join(joinGroupReqBody, imChannelContext);
                boolean joinGroupIsTrue = Objects.isNull(joinGroupRespBody)
                    || JoinGroupResult.JOIN_GROUP_RESULT_OK.getNumber() != joinGroupRespBody.getResult().getNumber();
                if (joinGroupIsTrue) {
                    return null;
                }
            }
            // // 是否是登录 登录会执行此方法 但是不需要通知群友
            // boolean loginAdd = joinGroupReqBody.getLoginAdd();
            // if(!loginAdd){
            // //集群通知
            // ImConfig imConfig = ImConfig.Global.get();
            // ImCluster cluster = imConfig.getCluster();
            // if (cluster != null && !packet.isFromCluster()) {
            // cluster.clusterToGroup(groupId, packet);
            // }
            // }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Command command() {
        return Command.COMMAND_JOIN_GROUP_REQ;
    }
}
