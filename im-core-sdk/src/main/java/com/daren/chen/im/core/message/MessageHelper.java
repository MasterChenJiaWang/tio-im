package com.daren.chen.im.core.message;

import java.util.List;
import java.util.Map;

import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.packets.ChatAckBody;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.MsgNoticeReq;
import com.daren.chen.im.core.packets.MsgNoticeRespBody;
import com.daren.chen.im.core.packets.NoticeOfflineReq;
import com.daren.chen.im.core.packets.NoticeOfflineRespBody;
import com.daren.chen.im.core.packets.ReceiveMsgNoticeRespBody;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;

/**
 * @author WChao
 * @date 2018年4月9日 下午4:31:51
 */
public interface MessageHelper {
    /**
     * 获取IM开启持久化时绑定/解绑群组、用户监听器;
     *
     * @return
     */
    ImStoreBindListener getBindListener();

    /**
     * 判断用户是否在线
     *
     * @param userId
     *            用户ID
     * @return
     */
    boolean isOnline(String userId);

    /**
     * 获取指定群组所有成员信息
     *
     * @param groupId
     *            群组ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    Group getGroupUsers(String operateUserId, String groupId, Integer type);

    /**
     * 获取用户所有群组成员信息
     *
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    List<Group> getAllGroupUsers(String operateUserId, String userId, Integer type);

    // /**
    // * 获取好友分组所有成员信息
    // *
    // * @param userId
    // * 用户ID
    // * @param friendGroupId
    // * 好友分组ID
    // * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
    // * @return
    // */
    // Group getFriendUsers(String userId, String friendGroupId, Integer type);

    /**
     * 获取好友分组所有成员信息
     *
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    List<User> getAllFriendUsers(String operateUserId, String userId, Integer type);

    /**
     *
     * @param userId
     * @return
     */
    List<String> getAllFriendUserIds(String operateUserId, String userId);

    /**
     * 根据在线类型获取用户信息;
     *
     * @param userId
     *            用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    User getUserByType(String operateUserId, String userId, Integer type);

    /**
     * 添加群组成员
     *
     * @param userId
     *            用户ID
     * @param groupId
     *            群组ID
     */
    void addGroupUser(String operateUserId, String userId, String groupId);

    /**
     * 获取群组所有成员;
     *
     * @param groupId
     *            群组ID
     * @return
     */
    List<String> getGroupUsers(String operateUserId, String groupId);

    /**
     * 获取用户拥有的群组ID;
     *
     * @param userId
     *            用户ID
     * @return
     */
    List<String> getGroups(String operateUserId, String userId);

    /**
     * 消息持久化写入
     *
     * @param timelineTable
     *            持久化表
     * @param timelineId
     *            持久化ID
     * @param chatBody
     *            消息记录
     */
    void writeMessage(String timelineTable, String timelineId, ChatBody chatBody);

    /**
     * 保存未读消息
     *
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    void saveNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody);

    /**
     * 移出未读消息
     *
     * @param timelineTable
     * @param timelineId
     */
    void removeNoReadMessage(String timelineTable, String timelineId);

    /**
     * 往数据库写消息
     *
     * @param chatBody
     */
    void writeMessageOfSql(String operateUserId, ChatBody chatBody);

    /**
     * 移除群组用户
     *
     * @param userId
     *            用户ID
     * @param groupId
     *            群组ID
     */
    void removeGroupUser(String operateUserId, String userId, String groupId);

    /**
     * 获取与指定用户离线消息;
     *
     * @param userId
     *            用户ID
     * @param fromUserId
     *            目标用户ID
     * @return
     */
    UserMessageData getFriendsOfflineMessage(String operateUserId, String userId, String fromUserId);

    /**
     * 获取与所有用户离线消息;
     *
     * @param userId
     *            用户ID
     * @return
     */
    UserMessageData getFriendsOfflineMessage(String operateUserId, String userId);

    /**
     * 获取用户指定群组离线消息;
     *
     * @param userId
     *            用户ID
     * @param groupId
     *            群组ID
     * @return
     */
    UserMessageData getGroupOfflineMessage(String operateUserId, String userId, String groupId);

    /**
     * 获取与指定用户历史消息;
     *
     * @param userId
     *            用户ID
     * @param fromUserId
     *            目标用户ID
     * @param beginTime
     *            消息区间开始时间
     * @param endTime
     *            消息区间结束时间
     * @param offset
     *            分页偏移量
     * @param count
     *            数量
     * @return
     */
    UserMessageData getFriendHistoryMessage(String operateUserId, String userId, String fromUserId, Double beginTime,
        Double endTime, Integer offset, Integer count);

    /**
     * 获取与指定群组历史消息;
     *
     * @param userId
     *            用户ID
     * @param groupId
     *            群组ID
     * @param beginTime
     *            消息区间开始时间
     * @param endTime
     *            消息区间结束时间
     * @param offset
     *            分页偏移量
     * @param count
     *            数量
     * @return
     */
    UserMessageData getGroupHistoryMessage(String operateUserId, String userId, String groupId, Double beginTime,
        Double endTime, Integer offset, Integer count);

    /**
     *
     * @return
     */
    boolean updateLastMessageId(String operateUserId, String userId, ChatAckBody chatAckBody);

    /**
     * 更新用户终端协议类型及在线状态;(在线:online:离线:offline)
     *
     * @param user
     *            用户信息
     */
    boolean updateUserTerminal(String operateUserId, User user);

    /**
     * 添加好友申请
     *
     * @param curUserId
     * @param friendUserId
     * @param content
     * @return
     */
    boolean addFriendApply(String operateUserId, String curUserId, String friendUserId, String content);

    /**
     * 获取用户的状态
     *
     * @param userId
     * @return
     */
    String getUserOnlineStatus(String operateUserId, String userId);

    /**
     * 离线消息通知
     *
     * @param msgNoticeReq
     * @return
     */
    MsgNoticeRespBody msgNoticeOffline(String operateUserId, MsgNoticeReq msgNoticeReq);

    /**
     * 用户下线消息通知
     *
     * @param noticeOfflineReq
     * @return
     */
    NoticeOfflineRespBody noticeOffLine(String operateUserId, NoticeOfflineReq noticeOfflineReq);

    /**
     * 接收通知
     *
     * @param userId
     * @return
     */
    ReceiveMsgNoticeRespBody receiveMsgNotice(String operateUserId, String userId);

    /**
     * 解散群组
     *
     * @param msgNoticeReq
     * @return
     */
    Map<String, Object> dissolveGroup(String operateUserId, MsgNoticeReq msgNoticeReq);

    /**
     *
     * @param noticeMsgId
     * @param noticeUserId
     * @return
     */
    boolean deleteMsgNotice(String operateUserId, String noticeMsgId, String noticeUserId);

    /**
     * 新增用户
     *
     * @param paramMap
     * @return
     */
    boolean addUserOnlineStatusRecord(String operateUserId, Map<String, Object> paramMap);

    /**
     *
     * @param topicName
     */
    boolean addTopicName(String topicName);

    /**
     *
     * @return
     */
    List<String> findAllTopicName();

    void addUserLoginInfo(String timelineTable, String userId, LoginUser loginUser);

    LoginUser getUserLoginInfo(String timelineTable, String userId);

    /**
     *
     * @param token
     * @return
     */
    User getUserBaseInfoByToken(String token);
}
