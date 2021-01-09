package com.daren.chen.im.client.test3;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ReconnConf;
import org.tio.core.Node;

import com.daren.chen.im.client.ImClientChannelContext;
import com.daren.chen.im.client.JimClient;
import com.daren.chen.im.client.JimClientAPI;
import com.daren.chen.im.client.config.ImClientConfig;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.packets.MessageReqBody;
import com.daren.chen.im.core.tcp.TcpPacket;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年8月30日 下午1:05:17
 */
public class Test3ClientStarter2 {
    private static final Logger logger = LoggerFactory.getLogger(Test3ClientStarter2.class);
    public static ImClientChannelContext imClientChannelContext = null;

    /**
     *
     */
    private final Map<String, ChatAckBody> chatBodyCache = new HashMap<>();

    public Map<String, ChatAckBody> getChatBodyCache() {
        return chatBodyCache;
    }

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        init();
        // 连上后，发条消息玩玩
        send();
    }

    private static void init() {
        new Thread(() -> {
            // 服务器节点
            Node serverNode = new Node("127.0.0.1", 18885);
            // 构建客户端配置信息
            ImClientConfig imClientConfig = ImClientConfig.newBuilder()
                // 客户端业务回调器,不可以为NULL
                .clientHandler(new Test3HelloImClientHandler2())
                // 客户端事件监听器，可以为null，但建议自己实现该接口
                .clientListener(null)
                // 心跳时长不设置，就不发送心跳包
                .heartbeatTimeout(60000)
                // 断链后自动连接的，不想自动连接请设为null
                .reConnConf(new ReconnConf(5000L)).name("测试").build();
            // 生成客户端对象;
            JimClient jimClient = new JimClient(imClientConfig);
            // 连接服务端
            try {
                imClientChannelContext = jimClient.connect(serverNode, 30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "IM启动线程").start();

    }

    private static void send() throws Exception {
        String userId = "13888800006";
        String token = "13888800006";
        byte[] loginBody = new LoginReqBody(userId, token, "222222", "e10adc3949ba59abbe56e057f20f883e").toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        // 先登录;
        int n = 20;
        while (n > 0 && imClientChannelContext == null) {
            n--;
            Thread.sleep(1000);
        }
        boolean b = JimClientAPI.bSend(imClientChannelContext, loginPacket);

        System.out.println(b);

        getOfflineMessage(userId);

        Thread.sleep(6000);
        //
        sendMessage(userId, "13888800001", "1");

        //

    }

    private static void sendMessage(String userId, String toUserId, String groupId) throws Exception {
        for (int i = 1001; i <= 2000; i++) {
            ChatBody chatBody = ChatBody.newBuilder().from(userId).to(toUserId).msgType(0)
                .chatType(ChatType.CHAT_TYPE_PRIVATE.getNumber()).content("2-私聊消息测试!-" + i).build();

            TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
            JimClientAPI.send(imClientChannelContext, chatPacket);
            //
            Thread.sleep(50);
            // 群消息
            ChatBody chatBody2 = ChatBody.newBuilder().from(userId).msgType(0).groupId(groupId)
                .chatType(ChatType.CHAT_TYPE_PUBLIC.getNumber()).content("2-群消息测试!-" + i).build();

            TcpPacket chatPacket2 = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody2.toByte());
            JimClientAPI.send(imClientChannelContext, chatPacket2);
            //
            Thread.sleep(50);
        }
    }

    private static void getOfflineMessage(String userId) {
        MessageReqBody messageReqBody = new MessageReqBody();
        messageReqBody.setUserId(userId);
        messageReqBody.setEndTime((double) System.currentTimeMillis());
        messageReqBody.setType(0);
        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_GET_MESSAGE_REQ, messageReqBody.toByte());
        //
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }

    // 收到消息 反馈
    public void chatAck(ChatBody chatBody) {
        if (chatBody == null) {
            return;
        }
        //
        String msgId = chatBody.getId();
        ChatAckBody chatAckBody = chatBodyCache.get(msgId);
        if (chatAckBody == null) {
            return;
        }
        Long createTime = chatBody.getCreateTime();
        // msgId 不等于 并且 创建时间小于 缓存中的 就 不处理 直接返回
        if (!msgId.equals(chatAckBody.getId()) && (createTime == null || createTime < chatAckBody.getCreateTime())) {
            return;
        }
        //
        TcpPacket tcpPacket2 = new TcpPacket(Command.CHAT_ACK_REQ, chatAckBody.toByte());
        JimClientAPI.send(imClientChannelContext, tcpPacket2);
        //
        //
        logger.warn("msgId [{}]", msgId);
        if (msgId.equals(chatAckBody.getId())) {
            chatBodyCache.remove(msgId);
        }
    }
}
