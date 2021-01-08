package com.daren.chen.im.client.test;

import org.tio.client.ReconnConf;
import org.tio.core.Node;

import com.daren.chen.im.client.ImClientChannelContext;
import com.daren.chen.im.client.JimClient;
import com.daren.chen.im.client.JimClientAPI;
import com.daren.chen.im.client.config.ImClientConfig;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.tcp.TcpPacket;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年8月30日 下午1:05:17
 */
public class HelloClientStarter2 {

    public static ImClientChannelContext imClientChannelContext = null;

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        // 服务器节点
        Node serverNode = new Node("192.168.1.22", 8887);
        // Node serverNode = new Node("127.0.0.1", 18887);
        // 构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
            // 客户端业务回调器,不可以为NULL
            .clientHandler(new HelloImClientHandler())
            // 客户端事件监听器，可以为null，但建议自己实现该接口
            .clientListener(new HelloImClientListener())
            // 心跳时长不设置，就不发送心跳包
            .heartbeatTimeout(5000)
            // 断链后自动连接的，不想自动连接请设为null
            .reConnConf(new ReconnConf(5000L)).build();
        // 生成客户端对象;
        JimClient jimClient = new JimClient(imClientConfig);
        // 连接服务端
        imClientChannelContext = jimClient.connect(serverNode);
        // 连上后，发条消息玩玩
        send();
    }

    private static void send() throws Exception {
        byte[] loginBody =
            new LoginReqBody("13888800002", "13888800002", "222222", "e10adc3949ba59abbe56e057f20f883e").toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        // 先登录;
        JimClientAPI.send(imClientChannelContext, loginPacket);

        //
        // MessageReqBody messageReqBody = new MessageReqBody();
        // messageReqBody.setUserId("3f6290d632b14c10bfdbe56ab564d4c3");
        // messageReqBody.setType(0);
        // TcpPacket chatPacket = new TcpPacket(Command.COMMAND_GET_MESSAGE_REQ, messageReqBody.toByte());
        // //
        // JimClientAPI.send(imClientChannelContext, chatPacket);

        // MessageReqBody messageReqBody = new MessageReqBody();
        // messageReqBody.setUserId("3f6290d632b14c10bfdbe56ab564d4c3");
        // messageReqBody.setType(0);
        // TcpPacket chatPacket = new TcpPacket(Command.COMMAND_GET_MESSAGE_REQ, messageReqBody.toByte());
        // //
        // JimClientAPI.send(imClientChannelContext, chatPacket);

        //
        for (int i = 0; i < 1000; i++) {
            ChatBody chatBody = ChatBody.newBuilder().from("f7efa824eb804ad8b10e0b750d0f3f46")
                .groupId("7e505afb10f434b04c5b90d2066cca0d").msgType(0).chatType(ChatType.CHAT_TYPE_PUBLIC.getNumber())
                .content("Socket2222222222222普通客户端消息测试!").build();

            TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
            JimClientAPI.send(imClientChannelContext, chatPacket);

            Thread.sleep(1000);
        }
        // AddGroupReqBody addGroupReqBody = new AddGroupReqBody();
        // addGroupReqBody.setUserId("328d32baa9314f3ca733f74280b07fb9");
        // addGroupReqBody.setGroupName("陈大人测试加入群组2!");
        // TcpPacket chatPacket = new TcpPacket(Command.COMMAND_ADD_GROUP_REQ, addGroupReqBody.toByte());
        // //
        // JimClientAPI.send(imClientChannelContext, chatPacket);

        // GetUserOnlineStatusReq getUserOnlineStatusReq = new GetUserOnlineStatusReq();
        //
        // getUserOnlineStatusReq.setUserId("f7efa824eb804ad8b10e0b750d0f3f46");
        // TcpPacket loginPacket2 = new TcpPacket(Command.GET_USER_ONLINE_STATUS_REQ, getUserOnlineStatusReq.toByte());
        // // 先登录;
        // JimClientAPI.send(imClientChannelContext, loginPacket2);

    }
}
