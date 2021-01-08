package com.daren.chen.im.client.test;

import org.tio.client.ReconnConf;
import org.tio.core.Node;

import com.daren.chen.im.client.ImClientChannelContext;
import com.daren.chen.im.client.JimClient;
import com.daren.chen.im.client.JimClientAPI;
import com.daren.chen.im.client.config.ImClientConfig;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.packets.UserReqBody;
import com.daren.chen.im.core.tcp.TcpPacket;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年8月30日 下午1:05:17
 */
public class HelloClientStarter3 {

    public static ImClientChannelContext imClientChannelContext = null;

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        // 服务器节点
        // Node serverNode = new Node("192.168.1.22", 8887);
        Node serverNode = new Node("127.0.0.1", 18887);
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
            new LoginReqBody("13888800001", "13888800001", "111111", "e10adc3949ba59abbe56e057f20f883e").toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        // 先登录;
        JimClientAPI.send(imClientChannelContext, loginPacket);

        // for (int i = 0; i < 1000; i++) {
        // ChatBody chatBody = ChatBody.newBuilder().from("3f6290d632b14c10bfdbe56ab564d4c3")
        // .to("41957c3ba8f542a396ae5457d10bd3ab").groupId("f6d9d4eef9f4fd5f0680af6da1d25467").msgType(0)
        // .chatType(ChatType.CHAT_TYPE_PUBLIC.getNumber()).content("Socket2222222222222普通客户端消息测试!").build();
        //
        // TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
        // JimClientAPI.send(imClientChannelContext, chatPacket);
        //
        // Thread.sleep(100);
        // }
        // AddUser addUser = new AddUser();
        // addUser.setCurUserId("3f6290d632b14c10bfdbe56ab564d4c3");
        // addUser.setFriendUserId("e2240386a7ab44c287acb85251fd7e45");
        // TcpPacket tcpPacket1 = new TcpPacket(Command.COMMAND_ADD_FRIEND_REQ, addUser.toByte());
        // JimClientAPI.send(imClientChannelContext, tcpPacket1);

        // DeleteUser deleteUser = new DeleteUser();
        // deleteUser.setCurUserId("3f6290d632b14c10bfdbe56ab564d4c3");
        // deleteUser.setFriendUserId("e2240386a7ab44c287acb85251fd7e45");
        // TcpPacket tcpPacket2 = new TcpPacket(Command.COMMAND_DELETE_FRIEND_REQ, deleteUser.toByte());
        // JimClientAPI.send(imClientChannelContext, tcpPacket2);

        // 加入群组
        // User user = User.newBuilder().userId("3f6290d632b14c10bfdbe56ab564d4c3").build();
        // Group group = Group.newBuilder().groupId("f7f5fb7a0997c0029821e8e10eb2b0f5").addUser(user).build();
        //
        // TcpPacket tcpPacket2 = new TcpPacket(Command.COMMAND_JOIN_GROUP_REQ, group.toByte());
        // JimClientAPI.send(imClientChannelContext, tcpPacket2);

        //
        UserReqBody userReqBody = new UserReqBody();
        userReqBody.setUserId("3f6290d632b14c10bfdbe56ab564d4c3");
        // (0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线]);
        userReqBody.setType(2);
        TcpPacket tcpPacket2 = new TcpPacket(Command.COMMAND_GET_USER_REQ, userReqBody.toByte());
        JimClientAPI.send(imClientChannelContext, tcpPacket2);
    }
}
