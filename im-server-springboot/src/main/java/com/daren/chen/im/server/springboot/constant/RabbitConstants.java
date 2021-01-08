package com.daren.chen.im.server.springboot.constant;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/11/30 10:30
 */
public class RabbitConstants {

    /**
     * 添加IM 添加消息
     */
    public static final String EXCHANGE_IM_CHAT = "mq_exchange_add_im_chat_msg";
    /**
     * 添加IM 添加消息
     */
    public static final String TOPIC_EXCHANGE_IM_CHAT = "topic_mq_exchange_add_im_chat_msg";
    /**
     *
     */
    public static final String ROUTINGKEY_IM_CHAT = "routingKey_im_chat";

    /**
     *
     */
    public static final String QUEUE_IM_CHAT = "queue_im_chat";

    /**
     *
     */
    public static final String QUEUE_IM_CHAT_UPDATE_LAST_ID = "queue_im_chat_update_last_id";

    /**
     *
     */
    public static final String QUEUE_IM_FUNCTION = "queue_im_function";

    /**
     *
     */
    public static final String EXCHANGE_IM_CHAT_UPDATE_LAST_ID = "mq_exchange_update_last_id";
    /**
     *
     */
    public static final String QUEUE_IM_FUNCTION_UPDATE_LAST_ID = "queue_im_function_update_last_id";
    /**
     *
     */
    public static final String ROUTINGKEY_IM_CHAT_UPDATE_LAST_ID = "routingkey_im_chat_update_last_id";

    /**
     *
     */
    public static final String ROUTINGKEY_IM_FUNCTION_UPDATE_LAST_ID = "routingkey_im_function_update_last_id";

    /**
     *
     */
    public static final String QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS = "queue_im_chat_update_user_online_status";

    /**
     *
     */
    public static final String QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS_1 = "queue_im_chat_update_user_online_status_1";

    /**
     *
     */
    public static final String QUEUE_IM_CHAT_UPDATE_USER_ONLINE_STATUS_2 = "queue_im_chat_update_user_online_status_2";
    /**
     *
     */
    public static final String ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS_1 =
        "routingkey_im_chat_update_user_online_status_1";
    /**
     *
     */
    public static final String ROUTINGKEY_IM_CHAT_UPDATE_USER_ONLINE_STATUS =
        "routingkey_im_chat_update_user_online_status";
}
