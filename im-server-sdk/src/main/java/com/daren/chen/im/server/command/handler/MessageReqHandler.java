package com.daren.chen.im.server.command.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.ImStatus;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImException;
import com.daren.chen.im.core.message.MessageHelper;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.MessageReqBody;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.core.utils.JsonKit;
import com.daren.chen.im.server.command.AbstractCmdHandler;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.protocol.ProtocolManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取聊天消息命令处理器
 *
 * @author WChao
 * @date 2018年4月10日 下午2:40:07
 */
public class MessageReqHandler extends AbstractCmdHandler {

    private static final Logger log = LoggerFactory.getLogger(MessageReqHandler.class);

    @Override
    public Command command() {
        return Command.COMMAND_GET_MESSAGE_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        try {
            RespBody resPacket;
            MessageReqBody messageReqBody;
            try {
                messageReqBody = JsonKit.toBean(packet.getBody(), MessageReqBody.class);
            } catch (Exception e) {
                // 用户消息格式不正确
                return getMessageFailedPacket(imChannelContext);
            }
            UserMessageData messageData = null;
            if (messageReqBody == null) {
                resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10015.getText());
                resPacket.setMsg("获取用户消息失败!");
                return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
            }
            ImServerConfig imServerConfig = ImConfig.Global.get();
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            // 群组ID;
            String groupId = messageReqBody.getGroupId();
            // 当前用户ID;
            String userId = messageReqBody.getUserId();
            // 消息来源用户ID;
            String fromUserId = messageReqBody.getFromUserId();
            // 消息区间开始时间;
            Double beginTime = messageReqBody.getBeginTime();
            // 消息区间结束时间;
            Double endTime = messageReqBody.getEndTime();
            // 分页偏移量;
            Integer offset = messageReqBody.getOffset();
            // 分页数量;
            Integer count = messageReqBody.getCount();
            // 消息类型;
            int type = messageReqBody.getType();
            // 如果用户ID为空或者type格式不正确，获取消息失败;
            if (StringUtils.isEmpty(userId) || (0 != type && 1 != type)
                    || !ImServerConfig.ON.equals(imServerConfig.getIsStore())) {
                return getMessageFailedPacket(imChannelContext);
            }
            if (type == 0) {
                resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10016.getText());
            } else {
                resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10018.getText());
            }
            // 群组ID不为空获取用户该群组消息;
            if (!StringUtils.isEmpty(groupId)) {
                // 离线消息;
                if (0 == type) {
                    messageData = messageHelper.getGroupOfflineMessage(imChannelContext.getUserId(), userId, groupId);
                    // 历史消息;
                } else if (1 == type) {
                    messageData = messageHelper.getGroupHistoryMessage(imChannelContext.getUserId(), userId, groupId,
                            beginTime, endTime, offset, count);
                }
            } else if (StringUtils.isEmpty(fromUserId)) {
                // 获取用户所有离线消息(好友+群组);
                if (0 == type) {
                    messageData = messageHelper.getFriendsOfflineMessage(imChannelContext.getUserId(), userId);
                } else {
                    return getMessageFailedPacket(imChannelContext);
                }
            } else {
                // 获取与指定用户离线消息;
                if (0 == type) {
                    messageData = messageHelper.getFriendsOfflineMessageOfLastsgId(userId, fromUserId, endTime);
                    // 获取与指定用户历史消息;
                } else if (1 == type) {
                    messageData = messageHelper.getFriendHistoryMessage(imChannelContext.getUserId(), userId,
                            fromUserId, beginTime, endTime, offset, count);
                }
            }
            // //
            // if (beginTime != null && endTime != null) {
            // resPacket.setData(messageData);
            // return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
            // } else {
            // // 拆分
            // List<UserMessageData> list = splitList(messageData);
            // if (list.size() > 0) {
            // for (UserMessageData userMessageData : list) {
            // resPacket.setData(userMessageData);
            // ImPacket imPacket = ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
            // JimServerAPI.send(imChannelContext, imPacket);
            // }
            // return null;
            // }
            // }
            resPacket.setData(messageData);
            // resPacket.setData(new UserMessageData(userId));
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        } catch (ImException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
            return getMessageFailedPacket(imChannelContext);
        }
    }

    /**
     * 获取用户消息失败响应包;
     *
     * @param imChannelContext
     * @return
     */
    public ImPacket getMessageFailedPacket(ImChannelContext imChannelContext) throws ImException {
        RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, ImStatus.C10015.getText());
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }

    /**
     * @param messageData
     * @return
     */
    private List<UserMessageData> splitList(UserMessageData messageData) {
        List<UserMessageData> list = new ArrayList<>();
        if (messageData == null) {
            return list;
        }
        Map<String, List<ChatBody>> friends = messageData.getFriends();
        if (friends != null && friends.size() > 0) {
            friends.forEach((k, v) -> {
                if (CollectionUtil.isNotEmpty(v) && v.size() > 5) {
                    int totalcount = v.size();
                    int pagesize = 5;
                    int pagecount = 0;
                    int m = totalcount % pagesize;
                    if (m > 0) {
                        pagecount = totalcount / pagesize + 1;
                    } else {
                        pagecount = totalcount / pagesize;
                    }
                    for (int i = 1; i <= pagecount; i++) {
                        List<ChatBody> chatBodies = pageBySubList(v, 5, i);
                        addFriendList(k, chatBodies, messageData.getUserId(), list);
                    }
                } else {
                    addFriendList(k, v, messageData.getUserId(), list);
                }

            });
        }
        Map<String, List<ChatBody>> groups = messageData.getGroups();
        if (groups != null && groups.size() > 0) {
            groups.forEach((k, v) -> {
                if (CollectionUtil.isNotEmpty(v) && v.size() > 5) {
                    int totalcount = v.size();
                    int pagesize = 5;
                    int pagecount = 0;
                    int m = totalcount % pagesize;
                    if (m > 0) {
                        pagecount = totalcount / pagesize + 1;
                    } else {
                        pagecount = totalcount / pagesize;
                    }
                    for (int i = 1; i <= pagecount; i++) {
                        List<ChatBody> chatBodies = pageBySubList(v, pagesize, i);
                        addGroupList(k, chatBodies, messageData.getUserId(), list);
                    }
                } else {
                    addGroupList(k, v, messageData.getUserId(), list);
                }

            });
        }
        return list;
    }

    private void addFriendList(String k, List<ChatBody> v, String userId, List<UserMessageData> list) {
        UserMessageData userMessageData = new UserMessageData();
        userMessageData.setUserId(userId);
        Map<String, List<ChatBody>> map = new HashMap<>(4);
        map.put(k, v);
        userMessageData.setFriends(map);
        list.add(userMessageData);
    }

    private void addGroupList(String k, List<ChatBody> v, String userId, List<UserMessageData> list) {
        UserMessageData userMessageData = new UserMessageData();
        userMessageData.setUserId(userId);
        Map<String, List<ChatBody>> map = new HashMap<>(4);
        map.put(k, v);
        userMessageData.setGroups(map);
        list.add(userMessageData);
    }

    public static List<ChatBody> pageBySubList(List<ChatBody> list, int pagesize, int currentPage) {
        int totalcount = list.size();
        int pagecount = 0;
        List<ChatBody> subList;
        int m = totalcount % pagesize;
        if (m > 0) {
            pagecount = totalcount / pagesize + 1;
        } else {
            pagecount = totalcount / pagesize;
        }
        if (m == 0) {
            subList = list.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
        } else {
            if (currentPage == pagecount) {
                subList = list.subList((currentPage - 1) * pagesize, totalcount);
            } else {
                subList = list.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
            }
        }
        return subList;
    }
}
