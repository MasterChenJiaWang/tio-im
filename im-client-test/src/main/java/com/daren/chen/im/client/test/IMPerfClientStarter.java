package com.daren.chen.im.client.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ReconnConf;
import org.tio.core.Node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.client.ImClientChannelContext;
import com.daren.chen.im.client.JimClient;
import com.daren.chen.im.client.JimClientAPI;
import com.daren.chen.im.client.config.ImClientConfig;
import com.daren.chen.im.client.test.pressuretest.TestImClientListener;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Command;
import com.daren.chen.im.core.packets.LoginReqBody;
import com.daren.chen.im.core.tcp.TcpPacket;

import cn.hutool.http.HttpRequest;

/**
 * IM的性能和稳定性测试脚本 用5台测试机器一起跑，每台机器编号为1，2，3，4，5，表示5个测试床（跑测试时配置测试床编号，程序自动会计算需要跑哪些用户和群）。
 * 每个测试床上跑2000个用户和40个群组，根据用户和群组的编号模5取余数决定落在哪个测试床上。
 * <p>
 * 每个测试床，支持两种测试方案： 第一种: 好友互聊, 一个群组支持聊一次就有C50/2=1225条消息。 第二种: 群聊，一个群组支持每个人发一次消息群聊就有2500条消息。
 * <p>
 * 用户表数据： 用户名：【测试13888800001】~【测试13888810000】（后面5位从1累加到10000） 手机号：【13888800001】~【13888810000】（后面5位从1累加到10000）
 * <p>
 * 群组数据： 10000个用户分配到50个群组中 群组：群名为 【测试群组1】~【测试群组200】群主为每一批用户的第一个成员
 * 群用户：例如【测试群组1】里面有用户【测试13888800001】~【测试13888800050】，群主就为用户【测试13888800001】； 【测试群组2】里面有用户【测试13888800051】~【测试13888800051】
 * 群主就为用户【测试13888800051】； 依次类推
 * <p>
 * 好友数据： 一个群组里面的所有成员互为好友 例如群组【测试群组1】里面有50个成员【测试13888800001】~【测试13888800050】，
 * 则成员【测试13888800001】与成员【测试13888800002】~【测试13888800050】 都互为好友
 */
public class IMPerfClientStarter {

    private static final Logger logger = LoggerFactory.getLogger(IMPerfClientStarter.class);

    public static class TestParams {
        // 指定的测试床编号
        public int tb_sn;
        // 指定测试的次数
        public long test_times;
        // 指定测试的方案
        public int test_type;
        // 与IM通讯的链路
        public ImClientChannelContext[] imContexts = new ImClientChannelContext[MAX_USER_NUM];
        // 本测试床跑的测试用户
        public long[] phones = new long[MAX_USER_NUM];

        public Map<String, ImClientChannelContext> contextMap = new ConcurrentHashMap<>(MAX_USER_NUM * 3 / 2);

    }

    public static final TestParams testParams = new TestParams();
    /**
     *
     */
    public static final Map<Integer, Node> NODE_MAP = new HashMap<>();
    // 测试方案的取值
    private static final int T_GROUP = 1;
    private static final int T_FRIEND = 2;

    // 测试床的最大编号
    private static final int MAX_TB_SN = 5;
    // 一个测试床支持的最大用户数
    private static final int MAX_USER_NUM = 2000;

    // 网关服务器地址
    // private static final String GATE_WAY_URL = "http://127.0.0.1:15010/gateway";
    private static final String GATE_WAY_URL = "http://192.168.1.250:15010/gateway";

    // 聊天的测试内容
    private static final String CHAT_CONTENT_FRIEND = "测试好友互聊: ";
    private static final String CHAT_CONTENT_GROUP = "测试群组群发互聊: ";

    private static final String APP_ID = "222222";
    private static final String APP_KEY = "e10adc3949ba59abbe56e057f20f883e";

    private static final String IM_SERVER_IP = "192.168.1.22";
    // private static final String IM_SERVER_IP = "127.0.0.1";
    private static final int IM_SERVER_PORT = 18887;
    // 预置测试数据的编号基线值
    private static final long PHONE_BASE = 13888800001L;

    private static final ExecutorService exe = Executors.newFixedThreadPool(8);
    /**
     *
     */
    private static final ExecutorService WRITE_FILE_EXECUTOR = Executors.newFixedThreadPool(8);

    /**
     * 缓存token
     */
    public static final Map<String, UserToken> TOKEN_MAP = new ConcurrentHashMap<>(4000);

    /**
     * 写token队列
     */
    private static TokenQueueRunnable tokenQueueRunnable = null;
    /**
     *
     */
    private static final TokenToFileProcessor tokenToFileProcessor = new TokenToFileProcessor();

    private static CountDownLatch countDownLatch = null;

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 3) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请指定测试的方案（1 or  2） :");
            int testType = sc.nextInt();
            System.out.println("输入的测试方案是" + testType);
            System.out.println("------------------------------");
            System.out.println("请指定的测试床编号( 1-5)");
            int tbSn = sc.nextInt();
            System.out.println("输入的测试床编号是" + tbSn);
            System.out.println("------------------------------");
            System.out.println("请指定测试的次数");
            int count = sc.nextInt();
            System.out.println("输入的测测试的次数是" + count);
            args = new String[] {testType + "", tbSn + "", count + ""};
        }
        if (null == args || args.length == 0 || args.length < 3) {
            System.out.println("TestType(1,2) TB_SN(1-5) TestTimes(>0)");
            long phone = PHONE_BASE + (5 - 1);
            for (int i = 0; i < 10; i++) {
                System.out.println(phone + i * MAX_TB_SN);
            }
            String json = "{\n" + " \"result_id\": null,\n" + " \"code\": 200,\n" + " \"msg\": \"请求成功\",\n"
                + " \"biz_code\": \"200\",\n" + " \"biz_msg\": \"业务处理成功\",\n" + " \"data\": {\n"
                + "  \"is_success\": true,\n" + "  \"code\": \"146177\",\n" + "  \"user_id\": \"13888800001\"\n"
                + " },\n" + " \"data_total\": 0,\n" + " \"z1\": null,\n" + " \"z2\": null\n" + "}";
            JSONObject res = JSON.parseObject(json);
            res = (JSONObject)res.get("data");
            System.out.println(res.get("code") + "|" + res.get("user_id"));
            return;
        } else {
            testParams.test_type = Integer.parseInt(args[0]);
            testParams.tb_sn = Integer.parseInt(args[1]);
            testParams.test_times = Long.parseLong(args[2]);
            if (testParams.test_type > T_FRIEND || testParams.test_type < T_GROUP) {
                System.out.println("TestType must be 1-2");
                return;
            }
            if (testParams.tb_sn > MAX_TB_SN || testParams.tb_sn < 1) {
                System.out.println("TB_SN must be 1-5");
                return;
            }
            if (testParams.test_times < 1) {
                System.out.println("TestTimes must be greater 0");
                return;
            }
        }
        //
        initTokenCache();
        //
        initTokenQueue();
        //
        initConnect();

        countDownLatch = new CountDownLatch(MAX_USER_NUM);
        //
        initLogin();
        // 等待所有登录成功 最多等1分钟
        boolean await = countDownLatch.await(5, TimeUnit.MINUTES);
        if (await) {
            logger.info("全部登录成功!");
        } else {
            logger.error("部分登录成功!.....");
        }
        // // 所有登录成功后再 发消息
        testPert();
    }

    // 用户获取短信并登录IM的任务
    static class LoginTask implements Runnable {
        private final long phone;

        private final int i;

        public LoginTask(long phone, int i) {
            this.phone = phone;
            this.i = i;
        }

        @Override
        public void run() {
            // logger.info(" {} 手机号登录", this.phone);
            UserToken userToken1 = TOKEN_MAP.get(this.phone + "");
            if (userToken1 != null) {
                //
                loginIm(i, userToken1, testParams.imContexts[i]);
                return;
            }
            JSONObject param = new JSONObject();
            JSONObject content = new JSONObject();
            content.put("phone", this.phone + "");

            param.put("app_id", APP_ID);
            param.put("method", "101702");
            param.put("app_key", APP_KEY);
            param.put("debug", "false");
            param.put("content", content);

            // 获取短信
            String resBody = HttpRequest.post(GATE_WAY_URL).header("Content-Type", "application/json;charset=UTF-8")
                .body(param.toString()).execute().body();
            JSONObject res = JSON.parseObject(resBody);
            if (null == res) {
                logger.error(this.phone + " get code error!");
                return;
            }

            res = (JSONObject)res.get("data");
            if (null == res) {
                logger.error(this.phone + " get code error!");
                return;
            }
            String code = res.getString("code");
            logger.info("{}  获取验证码为:  {}", this.phone, code);
            content.clear();
            param.clear();
            content.put("phone", this.phone + "");
            content.put("code", code);
            content.put("login_type", "1");

            param.put("app_id", APP_ID);
            param.put("method", "101703");
            param.put("app_key", APP_KEY);
            param.put("debug", "false");
            param.put("content", content);
            // 登录
            resBody = HttpRequest.post(GATE_WAY_URL).header("Content-Type", "application/json;charset=UTF-8")
                .body(param.toString()).execute().body();
            res = JSON.parseObject(resBody);
            if (null == res) {
                logger.error(this.phone + " login error!");
                return;
            }
            res = (JSONObject)res.get("data");
            if (null == res) {
                logger.error(this.phone + " login error!");
                return;
            }
            String token = res.getString("token");
            if (StringUtils.isBlank(token)) {
                logger.error(this.phone + " login error!");
                return;
            }
            //
            UserToken userToken = new UserToken(this.phone + "", token);
            tokenQueueRunnable.addMsg(userToken);
            tokenQueueRunnable.executor.execute(tokenQueueRunnable);
            TOKEN_MAP.put(this.phone + "", userToken);
            //
            loginIm(i, userToken, testParams.imContexts[i]);
        }
    }

    public static void loginIm(int i, UserToken userToken, ImClientChannelContext imContext) {
        if (imContext == null) {
            return;
        }
        byte[] loginBody = new LoginReqBody(userToken.getUserId(), userToken.getToken(), APP_ID, APP_KEY).toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        // 先登录;
        boolean b = JimClientAPI.bSend(imContext, loginPacket);
        if (!b) {
            logger.error("{}   {}   登录失败", i, userToken.getUserId());
        } else {
            imContext.setUserId(userToken.getUserId());
            testParams.contextMap.put(userToken.getUserId(), imContext);
        }
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    // 聊天的任务
    static class ChatTask implements Runnable {
        private final int index;
        private final long userid;
        private final long friendid;
        protected long groupno;

        public ChatTask(int index, long userid, long friendid, long groupno) {
            this.index = index;
            this.userid = userid;
            this.friendid = friendid;
            this.groupno = groupno;
        }

        @Override
        public void run() {
            ChatBody chatBody = null;
            if (0 == this.friendid) // 群聊
            {
                String message = CHAT_CONTENT_GROUP
                    + String.format(" %s 往 群 %s 发送消息,序号: %d ", this.userid, this.groupno, this.index);
                chatBody = ChatBody.newBuilder().from("" + this.userid).groupId("" + this.groupno).msgType(0)
                    .chatType(ChatType.CHAT_TYPE_PUBLIC.getNumber()).content(message).build();
            } else {
                String message = CHAT_CONTENT_FRIEND
                    + String.format(" %s 给好友 %s 发送消息,序号: %d ", this.userid, this.friendid, this.index);
                chatBody = ChatBody.newBuilder().from("" + this.userid).to("" + this.friendid).msgType(0)
                    .chatType(ChatType.CHAT_TYPE_PRIVATE.getNumber()).content(message).build();
            }

            TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
            JimClientAPI.send(testParams.imContexts[this.index], chatPacket);
            // if (!b) {
            // logger.error("发送消息失败 {}", JsonKit.toJSONString(chatBody));
            // }
        }
    }

    // 模拟2000个用户登录IM服务器
    private static void initLogin() {
        long phone = PHONE_BASE + (testParams.tb_sn - 1);
        for (int i = 0; i < MAX_USER_NUM; i++) {
            long l = phone + i * MAX_TB_SN;
            exe.execute(new LoginTask(l, i));
            testParams.phones[i] = l;
            // try {
            // countDownLatch.await(5, TimeUnit.MILLISECONDS);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
        }
        logger.info("initLogin ok!");
    }

    // 模拟2000个用户链接IM服务器
    private static void initConnect() {

        // 服务器节点
        Node serverNode = new Node(IM_SERVER_IP, IM_SERVER_PORT);

        // 构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
            // 客户端业务回调器,不可以为NULL
            .clientHandler(new HelloImClientHandler())
            // 客户端事件监听器，可以为null，但建议自己实现该接口
            .clientListener(new TestImClientListener())
            // 心跳时长不设置，就不发送心跳包
            .heartbeatTimeout(60000)
            // 断链后自动连接的，不想自动连接请设为null
            .reConnConf(new ReconnConf(5000L)).name("Test").build();

        for (int i = 1; i <= MAX_USER_NUM; i++) {
            // 生成客户端对象;
            imClientConfig.setName("Test" + i);
            JimClient jimClient1 = new JimClient(imClientConfig);
            // 连接服务端
            try {
                ImClientChannelContext connect = jimClient1.connect(serverNode, 30);
                if (connect != null) {
                    testParams.imContexts[i - 1] = connect;

                    Thread.sleep(20);
                } else {
                    logger.info(" {} 连接失败", i);
                }
            } catch (Exception e) {
                logger.error("initConnect 报错: " + e.toString(), e);
            }
            if (testParams.imContexts[i - 1] == null) {
                NODE_MAP.put(i, serverNode);
            }
        }
        if (!NODE_MAP.isEmpty()) {
            ImClientConfig finalImClientConfig = imClientConfig;
            NODE_MAP.forEach((k, v) -> {
                // 生成客户端对象;
                finalImClientConfig.setName("Test" + k);
                JimClient jimClient1 = new JimClient(finalImClientConfig);
                // 连接服务端
                try {
                    logger.info(" {} 正在重新连接", k);
                    ImClientChannelContext connect = jimClient1.connect(v, 30);
                    if (connect != null) {
                        testParams.imContexts[k - 1] = connect;
                        Thread.sleep(20);
                    } else {
                        logger.info(" {} 连接失败", k);
                    }
                } catch (Exception e) {
                    logger.error("initConnect 报错: " + e.toString(), e);
                }
            });
        }

        int kkk = 0;
        for (ImClientChannelContext imContext : testParams.imContexts) {
            if (imContext != null) {
                kkk++;
            }
        }
        logger.info("initConnect ok!  {} imContexts 总长度", kkk);
    }

    // 群聊
    private static void _doGroupChatTask(long times) {
        for (int i = 0; i < testParams.test_times; i++) {
            for (int j = 0; j < MAX_USER_NUM; j++) {
                exe.execute(new ChatTask(j, testParams.phones[j], 0, (testParams.phones[j] - PHONE_BASE) / 50 + 1));
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                logger.error(e.toString(), e);
            }
        }
    }

    // 根据预置的测试数据返回用户的好友列表,预置的数据是一个群组50个人
    private static long[] buildFriends(long userid) {
        long[] phones = new long[49];
        long groupno = (userid - PHONE_BASE) / 50;
        long phone;
        for (int i = 0; i < 50; i++) {
            phone = PHONE_BASE + 50 * groupno + i;
            if (phone != userid) {
                phones[i] = phone;
            }
        }
        return phones;
    }

    // 好友聊天
    private static void _doFriendChatTask(long times) {
        for (int i = 0; i < testParams.test_times; i++) {
            for (int j = 0; j < MAX_USER_NUM; j++) {
                long[] friends = buildFriends(testParams.phones[j]);
                for (long friend : friends) {
                    exe.execute(new ChatTask(j, testParams.phones[j], friend, 0));
                }
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                logger.error(e.toString(), e);
            }
        }
    }

    private static void testPert() throws Exception {
        if (testParams.test_type == T_GROUP) {
            _doGroupChatTask(testParams.test_times);
        } else {
            _doFriendChatTask(testParams.test_times);
        }
    }

    private static void initTokenQueue() {
        tokenQueueRunnable = new TokenQueueRunnable(WRITE_FILE_EXECUTOR, tokenToFileProcessor);
    }

    /**
     * 读
     */
    private static void initTokenCache() {
        tokenToFileProcessor.read(TOKEN_MAP);
    }
}
