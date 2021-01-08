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
public class HelloClientStarter {

    public static ImClientChannelContext imClientChannelContext = null;

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        init();
        // 连上后，发条消息玩玩
        send();
    }

    private static void init() {
        // new Thread(() -> {
        // 服务器节点
        Node serverNode = new Node("192.168.1.22", 18887);
        // 构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
            // 客户端业务回调器,不可以为NULL
            .clientHandler(new HelloImClientHandler())
            // 客户端事件监听器，可以为null，但建议自己实现该接口
            .clientListener(new HelloImClientListener())
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
        // }, "IM启动线程").start();

    }

    private static void send() throws Exception {
        byte[] loginBody =
            new LoginReqBody("13888800001", "13888800001", "222222", "e10adc3949ba59abbe56e057f20f883e").toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        // 先登录;
        boolean b = JimClientAPI.bSend(imClientChannelContext, loginPacket);

        System.out.println(b);

        for (int i = 0; i < 1000; i++) {
            ChatBody chatBody = ChatBody.newBuilder().from("13888800001").to("13888800002").msgType(0)
                .chatType(ChatType.CHAT_TYPE_PRIVATE.getNumber()).content("Socket111111普通客户端消息测试!").build();

            TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
            JimClientAPI.send(imClientChannelContext, chatPacket);
            //
            Thread.sleep(1000);
        }
    }
}
