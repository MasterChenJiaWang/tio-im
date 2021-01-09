package com.daren.chen.im.server.helper.mongo;

import java.util.List;
import java.util.Map;

import com.daren.chen.im.core.listener.ImStoreBindListener;
import com.daren.chen.im.core.message.MessageHelper;
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
 * @Desc Mongo获取持久化+同步消息助手;
 * @date 2020-05-03 21:04
 */
public class MongoMessageHelper implements MessageHelper {
    @Override
    public ImStoreBindListener getBindListener() {
        return null;
    }

    @Override
    public boolean isOnline(String userId) {
        return false;
    }

    @Override
    public Group getGroupUsers(String operateUserId, String group_id, Integer type) {
        return null;
    }

    @Override
    public List<Group> getAllGroupUsers(String operateUserId, String user_id, Integer type) {
        return null;
    }

    // @Override
    // public Group getFriendUsers(String user_id, String friend_group_id, Integer type) {
    // return null;
    // }

    @Override
    public List<User> getAllFriendUsers(String operateUserId, String user_id, Integer type) {
        return null;
    }

    @Override
    public List<String> getAllFriendUserIds(String operateUserId, String userId) {
        return null;
    }

    @Override
    public User getUserByType(String operateUserId, String userid, Integer type) {
        return null;
    }

    @Override
    public void addGroupUser(String operateUserId, String userid, String group_id) {

    }

    @Override
    public List<String> getGroupUsers(String operateUserId, String group_id) {
        return null;
    }

    @Override
    public List<String> getGroups(String operateUserId, String user_id) {
        return null;
    }

    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {

    }

    @Override
    public void saveNoReadMessage(String timelineTable, String timelineId, ChatBody chatBody) {

    }

    @Override
    public void removeNoReadMessage(String timelineTable, String timelineId) {}

    @Override
    public void writeMessageOfSql(String operateUserId, ChatBody chatBody) {

    }

    @Override
    public void removeGroupUser(String operateUserId, String userId, String group_id) {

    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId, String fromUserId) {
        return null;
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String operateUserId, String userId) {
        return null;
    }

    @Override
    public UserMessageData getFriendsOfflineMessageOfLastsgId(String operateUserId, String userId, Double endTime) {
        return null;
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String operateUserId, String userId, String groupId) {
        return null;
    }

    @Override
    public UserMessageData getFriendHistoryMessage(String operateUserId, String userId, String fromUserId,
        Double beginTime, Double endTime, Integer offset, Integer count) {
        return null;
    }

    @Override
    public UserMessageData getGroupHistoryMessage(String operateUserId, String userId, String groupId, Double beginTime,
        Double endTime, Integer offset, Integer count) {
        return null;
    }

    @Override
    public boolean updateLastMessageId(String operateUserId, String userId, ChatAckBody chatBody) {
        return false;
    }

    @Override
    public boolean updateUserTerminal(String operateUserId, User user) {
        return false;
    }

    @Override
    public boolean addFriendApply(String operateUserId, String curUserId, String friendUserId, String content) {
        return false;
    }

    @Override
    public String getUserOnlineStatus(String operateUserId, String userId) {
        return null;
    }

    // @Override
    // public MsgNoticeRespBody msgNotice(MsgNoticeReq msgNoticeReq) {
    // return null;
    // }

    @Override
    public MsgNoticeRespBody msgNoticeOffline(String operateUserId, MsgNoticeReq msgNoticeReq) {
        return null;
    }

    @Override
    public NoticeOfflineRespBody noticeOffLine(String operateUserId, NoticeOfflineReq noticeOfflineReq) {
        return null;
    }

    @Override
    public ReceiveMsgNoticeRespBody receiveMsgNotice(String operateUserId, String userId) {
        return null;
    }

    @Override
    public Map<String, Object> dissolveGroup(String operateUserId, MsgNoticeReq msgNoticeReq) {
        return null;
    }

    @Override
    public boolean deleteMsgNotice(String operateUserId, String noticeMsgId, String noticeUserId) {
        return false;
    }

    @Override
    public boolean addUserOnlineStatusRecord(String operateUserId, Map<String, Object> paramMap) {
        return false;
    }

    @Override
    public boolean addTopicName(String topicName) {
        return true;
    }

    @Override
    public List<String> findAllTopicName() {
        return null;
    }

    @Override
    public void addUserLoginInfo(String timelineTable, String userId, LoginUser loginUser) {

    }

    @Override
    public LoginUser getUserLoginInfo(String timelineTable, String userId) {
        return null;
    }

    @Override
    public User getUserBaseInfoByToken(String token) {
        return null;
    }

}
