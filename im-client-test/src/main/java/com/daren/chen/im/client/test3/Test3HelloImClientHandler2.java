package com.daren.chen.im.client.test3;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.daren.chen.im.client.handler.ImClientHandler;
import com.daren.chen.im.core.ImChannelContext;
import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.ImPacket;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.core.exception.ImDecodeException;
import com.daren.chen.im.core.packets.AuthRespBody;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.RespBody;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.core.tcp.TcpPacket;
import com.daren.chen.im.core.tcp.TcpServerDecoder;
import com.daren.chen.im.core.tcp.TcpServerEncoder;

import cn.hutool.core.collection.CollectionUtil;

/**
 *
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年8月30日 下午1:10:28
 */
public class Test3HelloImClientHandler2 implements ImClientHandler, ImConst {
    private static Logger logger = LoggerFactory.getLogger(Test3HelloImClientHandler2.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 处理消息
     */
    @Override
    public void handler(ImPacket imPacket, ImChannelContext channelContext) {
        TcpPacket helloPacket = (TcpPacket)imPacket;
        byte[] body = helloPacket.getBody();
        if (body != null) {
            try {
                String str = new String(body, ImConst.CHARSET);
                decodeCommand(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return;
    }

    private void decodeCommand(String str) {
        RespBody r = JSON.parseObject(str, RespBody.class);
        Command command = r.getCommand();
        if (command != Command.COMMAND_HEARTBEAT_REQ) {
            // logger.info("客户端收到消息:[{}], {}", command.getNumber(), str);
        }
        switch (command) {
            // 握手响应，含http的websocket握手响应
            case COMMAND_HANDSHAKE_RESP:
                r = JSON.parseObject(str, RespBody.class);
                break;
            // 鉴权响应
            case COMMAND_AUTH_RESP:
                r = JSON.parseObject(str, AuthRespBody.class);
                break;
            // 登录响应
            case COMMAND_LOGIN_RESP:
                break;
            // 申请进入群组响应
            case COMMAND_JOIN_GROUP_RESP:
                break;
            // 进入群组通知
            case COMMAND_JOIN_GROUP_NOTIFY_RESP:
                break;
            // 退出群组通知
            case COMMAND_EXIT_GROUP_NOTIFY_RESP:
                break;
            // 聊天响应
            case COMMAND_CHAT_RESP:

                // logger.info("收到 消息: {}", JSON.toJSONString(r.getData()));
                ChatBody chatBody = JSON.parseObject(JSON.toJSONString(r.getData()), ChatBody.class);
                if (chatBody != null) {
                    int i = atomicInteger.incrementAndGet();
                    logger.warn("2-即时消息: {}  id[{}]", i, chatBody.getId());
                    // 收到消息反馈
                    Test3ClientStarter2 instance = new Test3ClientStarter2();
                    Map<String, ChatAckBody> chatBodyCache = instance.getChatBodyCache();
                    List<String> msgIds = new ArrayList<>(1);
                    msgIds.add(chatBody.getId());
                    ChatAckBody chatAckBody = ChatAckBody.newBuilder().to(chatBody.getTo()).from(chatBody.getFrom())
                        .groupId(chatBody.getGroupId()).msgIds(msgIds).setId(chatBody.getId()).build();
                    chatAckBody.setCreateTime(chatBody.getCreateTime());
                    chatBodyCache.put(chatAckBody.getId(), chatAckBody);
                    instance.chatAck(chatBody);
                }
                break;
            // 收到撤消消息指令
            case COMMAND_CANCEL_MSG_RESP:
                break;
            // 获取用户信息响应
            case COMMAND_GET_USER_RESP:
                break;
            // 获取聊天消息响应
            case COMMAND_GET_MESSAGE_RESP:
                Test3ClientStarter2 instance = new Test3ClientStarter2();
                Map<String, ChatAckBody> chatBodyCache = instance.getChatBodyCache();
                UserMessageData userMessageData =
                    JSON.parseObject(JSON.toJSONString(r.getData()), UserMessageData.class);
                if (userMessageData != null) {
                    Map<String, List<ChatBody>> friends = userMessageData.getFriends();
                    Map<String, List<ChatBody>> groups = userMessageData.getGroups();
                    for (String key : friends.keySet()) {
                        List<ChatBody> chatBodyList = friends.get(key);
                        if (chatBodyList != null) {
                            Map<String, List<ChatBody>> stringListMap = chatBodyListToFirendIdMap(chatBodyList);
                            if (stringListMap.size() > 0) {
                                for (Map.Entry<String, List<ChatBody>> map : stringListMap.entrySet()) {
                                    List<ChatBody> value = map.getValue();
                                    if (value == null || value.size() == 0) {
                                        continue;
                                    }
                                    //
                                    setChatBodyCache(chatBodyCache, value);
                                    for (ChatBody body : value) {
                                        int i2 = atomicInteger.incrementAndGet();
                                        logger.warn("2-离线消息 : {}  id[{}]", i2, body.getId());
                                        //
                                        instance.chatAck(body);
                                    }

                                }
                            }
                        }
                    }

                    for (String key : groups.keySet()) {
                        List<ChatBody> chatBodyList = groups.get(key);
                        if (chatBodyList != null) {
                            Map<String, List<ChatBody>> stringListMap = chatBodyListToGroupIdMap(chatBodyList);
                            if (stringListMap.size() > 0) {
                                for (Map.Entry<String, List<ChatBody>> map : stringListMap.entrySet()) {
                                    List<ChatBody> value = map.getValue();
                                    if (value == null || value.size() == 0) {
                                        continue;
                                    }
                                    //
                                    setChatBodyCache(chatBodyCache, value);
                                    for (ChatBody body : value) {
                                        int i2 = atomicInteger.incrementAndGet();
                                        logger.warn("2-离线消息 : {}  id[{}]", i2, body.getId());
                                        //
                                        instance.chatAck(body);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case COMMAND_LOGIN_REQ:
                break;
            default:
                break;
        }
    }

    /**
     * 分页设置
     *
     * @param chatBodyList
     *            分页数据
     */

    public static void setChatBodyCache(Map<String, ChatAckBody> chatBodyCache, List<ChatBody> chatBodyList) {
        if (CollectionUtil.isEmpty(chatBodyList)) {
            return;
        }

        int totalcount = chatBodyList.size();
        int pagecount;
        int pagesize = 10;
        List<ChatBody> subList;
        int m = totalcount % pagesize;
        if (m > 0) {
            pagecount = totalcount / pagesize + 1;
        } else {
            pagecount = totalcount / pagesize;
        }
        //
        for (int currentPage = 1; currentPage <= pagecount; currentPage++) {
            if (m == 0) {
                subList = chatBodyList.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
            } else {
                if (currentPage == pagecount) {
                    subList = chatBodyList.subList((currentPage - 1) * pagesize, totalcount);
                } else {
                    subList = chatBodyList.subList((currentPage - 1) * pagesize, pagesize * (currentPage));
                }
            }
            //
            if (subList.size() > 0) {
                ChatBody lastChatBody = subList.get(subList.size() - 1);
                List<String> msgIds = subList.stream().map(ChatBody::getId).collect(Collectors.toList());
                ChatAckBody chatAckBody = ChatAckBody.newBuilder().to(lastChatBody.getTo()).from(lastChatBody.getFrom())
                    .groupId(lastChatBody.getGroupId()).msgIds(msgIds).setId(lastChatBody.getId()).build();
                chatAckBody.setCreateTime(lastChatBody.getCreateTime());
                chatBodyCache.put(chatAckBody.getId(), chatAckBody);
            }

        }

    }

    /**
     * @param chatBodyList
     * @return
     */
    private Map<String, List<ChatBody>> chatBodyListToGroupIdMap(List<ChatBody> chatBodyList) {
        Map<String, List<ChatBody>> chatBodyMap = new HashMap<>();
        if (chatBodyList == null) {
            return chatBodyMap;
        }
        for (ChatBody chatBody : chatBodyList) {
            String groupId = chatBody.getGroupId();
            if (StringUtils.isBlank(groupId)) {
                continue;
            }
            List<ChatBody> chatBodies = chatBodyMap.get(groupId);
            if (chatBodies == null) {
                chatBodies = new ArrayList<>();
            }
            chatBodies.add(chatBody);
            chatBodyMap.put(groupId, chatBodies);
        }
        return chatBodyMap;
    }

    /**
     * @param chatBodyList
     * @return
     */
    private Map<String, List<ChatBody>> chatBodyListToFirendIdMap(List<ChatBody> chatBodyList) {
        Map<String, List<ChatBody>> chatBodyMap = new HashMap<>();
        if (chatBodyList == null) {
            return chatBodyMap;
        }
        for (ChatBody chatBody : chatBodyList) {
            String from = chatBody.getFrom();
            if (StringUtils.isBlank(from)) {
                continue;
            }
            List<ChatBody> chatBodies = chatBodyMap.get(from);
            if (chatBodies == null) {
                chatBodies = new ArrayList<>();
            }
            chatBodies.add(chatBody);
            chatBodyMap.put(from, chatBodies);
        }
        return chatBodyMap;
    }

    /**
     * 编码：把业务消息包编码为可以发送的ByteBuffer 总的消息结构：消息头 + 消息体 消息头结构： 4个字节，存储消息体的长度 消息体结构： 对象的json串的byte[]
     */
    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        TcpPacket tcpPacket = (TcpPacket)imPacket;
        return TcpServerEncoder.encode(tcpPacket, imConfig, imChannelContext);
    }

    @Override
    public TcpPacket decode(ByteBuffer buffer, int limit, int position, int readableLength,
        ImChannelContext imChannelContext) throws ImDecodeException, ImDecodeException {
        TcpPacket tcpPacket = TcpServerDecoder.decode(buffer, imChannelContext);
        return tcpPacket;
    }

    private static TcpPacket heartbeatPacket =
        new TcpPacket(Command.COMMAND_HEARTBEAT_REQ, new byte[] {Protocol.HEARTBEAT_BYTE});

    /**
     * 此方法如果返回null，框架层面则不会发心跳；如果返回非null，框架层面会定时发本方法返回的消息包
     */
    @Override
    public TcpPacket heartbeatPacket(ImChannelContext imChannelContext) {
        return heartbeatPacket;
    }

}
