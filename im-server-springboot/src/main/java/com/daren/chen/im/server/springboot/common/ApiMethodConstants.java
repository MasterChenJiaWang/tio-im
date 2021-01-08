package com.daren.chen.im.server.springboot.common;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName ApiMethodConstants
 * @Description
 * @date 2020/10/20 10:19
 **/
public class ApiMethodConstants {

    /**
     * 新增群组
     */
    public static final String ADD_GROUP = "128100";

    /**
     * 通过群组ID查询所有的用户
     */
    public static final String GET_ALL_GROUP_USERS_BY_GROUP_ID = "128112";

    /**
     * 用户添加到群组
     */
    public static final String ADD_USER_TO_GROUP = "128102";

    /**
     * 往群组里面删除用户
     */
    public static final String REMOVE_GROUP_USER = "128106";

    /**
     * 查询用户的所有群组
     */
    public static final String GET_ALL_USER_GROUP = "128105";

    /**
     * 批量获取群组信息
     */
    public static final String GET_ALL_GROUPS_BY_IDS = "128107";
    /**
     * 查询群组信息
     */
    public static final String GET_GROUP_INFO_BY_ID = "128108";
    /**
     * 根据用户id查询群组以及群组成员
     */
    public static final String GET_ALL_GROUP_INFO_BY_USER_ID = "128111";

    /**
     * 添加好友
     */
    public static final String ADD_FRIEND = "128000";

    /**
     * 删除好友
     */
    public static final String DELETE_FRIEND = "128001";

    /**
     * 批量获取用户好友信息
     */
    public static final String GET_ALL_USERS_FRIENDS_BY_IDS = "128004";

    /**
     * 批量获取用户信息
     */
    public static final String GET_ALL_USERS_BY_IDS = "128005";
    /**
     * 查询用户好友列表
     */
    public static final String GET_ALL_USER_FRIENDS_BY_ID = "128006";

    /**
     * 校验token
     */
    public static final String AUTH_101801 = "101801";
    /**
     *
     */
    public static final String CHAT_128200 = "128200";
    /**
     * 获取离线消息记录
     */
    public static final String CHAT_128201 = "128201";
    /**
     * 修改消息LastMsgId
     */
    public static final String CHAT_128202 = "128202";

    /**
     * 解散群组
     */
    public static final String DISBAND_GROUP_128201 = "128101";

    /**
     * 解散群组
     */
    public static final String SAVE_USER_ONLINE_STATUS_128300 = "128300";
}
