package com.daren.chen.im.server.springboot.helper.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daren.chen.im.core.packets.ChatBody;
import com.daren.chen.im.core.packets.ChatType;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.core.packets.UserMessageData;
import com.daren.chen.im.server.service.AuthCacheService;
import com.daren.chen.im.server.springboot.common.ApiMethodConstants;
import com.daren.chen.im.server.springboot.entity.GatewayCode;
import com.daren.chen.im.server.springboot.entity.Result;
import com.daren.chen.im.server.springboot.queue.MQMsgProducerService;
import com.daren.chen.im.server.springboot.utils.HttpApiUtils;
import com.daren.chen.im.server.util.Environment;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @author chendaren
 * @version V1.0
 * @ClassName CupChatCommonMethodUtils
 * @Description
 * @date 2020/10/20 10:02
 **/
@Service
public class ChatCommonMethodUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChatCommonMethodUtils.class);

    @Autowired
    private MQMsgProducerService mqMsgProducerService;

    /**
     * 获取群组的用户
     *
     * @param groupId
     * @return
     */
    public List<String> getGroupUsers(String operateUserId, String groupId) {
        List<String> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", groupId);
            Result<JSONObject> result =
                HttpApiUtils.post(ApiMethodConstants.GET_ALL_GROUP_USERS_BY_GROUP_ID, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONArray userList = resultData.getJSONArray("group_user_ids");
                    if (userList != null && userList.size() > 0) {
                        for (int i = 0; i < userList.size(); i++) {
                            String id = userList.getString(i);
                            list.add(id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return list;
    }

    /**
     * 用户添加至群组
     *
     * @param groupId
     * @param userId
     */
    public Boolean addUserToGroup(String operateUserId, String groupId, String userId) {
        boolean success = false;
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", groupId);
            jsonObject.put("user_id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.ADD_USER_TO_GROUP, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    success = resultData.getBoolean("success");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return success;
    }

    /**
     * 用户移除群组
     *
     * @param groupId
     * @param userId
     */
    public Boolean removeGroupUser(String operateUserId, String groupId, String userId) {
        boolean success = false;
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", groupId);
            jsonObject.put("user_id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.REMOVE_GROUP_USER, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    success = resultData.getBoolean("success");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return success;
    }

    /**
     * 获取用户的所有群组
     *
     * @param userId
     * @return
     */
    public List<String> getAllGroupUsers(String operateUserId, String userId) {
        List<String> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_ids", Collections.singletonList(userId));
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_USER_GROUP, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONObject userMap = resultData.getJSONObject("map");
                    if (userMap != null) {
                        Map<String, Object> map = JSON.parseObject(userMap.toJSONString(), Map.class);
                        JSONArray o = (JSONArray)map.get(userId);
                        if (o != null) {
                            for (Object o1 : o) {
                                JSONObject json = (JSONObject)o1;
                                String groupId = json.getString("group_id");
                                if (StringUtils.isNotBlank(groupId)) {
                                    list.add(groupId);
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return list;
    }

    /**
     * @param token
     * @return
     */
    public static User getUserBaseInfoByToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                throw new RuntimeException("token  为空！");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.AUTH_101801, jsonObject);
            if (GatewayCode.SUCCESS.getCode() != result.getCode()
                || !GatewayCode.SUCCESS.getCode().toString().equals(result.getBizCode())) {
                throw new RuntimeException(result.getBizMsg());
            }
            JSONObject data = result.getData();
            String userId = data.getString("user_id");
            String username = data.getString("username");
            String realname = data.getString("realname");
            String avatar = data.getString("avatar");
            String newToken = data.getString("token");
            // 根据userId 获取好友和群组

            return User.newBuilder().id(userId).userId(userId).nick(realname).avatar(avatar).addExtra("token", newToken)
                .addExtra("username", username).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    public User getUserById(String operateUserId, String userId) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            List<String> userIds = new ArrayList<>(2);
            userIds.add(userId);
            List<User> list = batchGetUserListByIds(operateUserId, userIds);
            if (CollectionUtil.isEmpty(list)) {
                return User.newBuilder().id(userId).userId(userId).build();
            }
            return list.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return null;
    }

    /**
     * 批量获取用户信息
     *
     * @param ids
     * @return
     */
    public List<User> batchGetUserByIds(String operateUserId, List<String> ids) {
        List<User> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            list = batchGetUserListByIds(operateUserId, ids);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return list;
    }

    /**
     * 批量获取用户信息
     *
     * @param ids
     * @return
     */
    private List<User> batchGetUserListByIds(String operateUserId, List<String> ids) {
        List<User> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_ids", ids);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_USERS_BY_IDS, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONObject userMap = resultData.getJSONObject("obj");
                    if (userMap != null) {
                        Map<String, Object> map = JSON.parseObject(userMap.toJSONString(), Map.class);
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            String userId = entry.getKey();
                            Object userInfo = entry.getValue();
                            JSONObject json = JSON.parseObject(userInfo.toString());
                            String nick = json.getString("realname");
                            String avatar = json.getString("avatar");
                            User user = User.newBuilder().id(userId).nick(nick).userId(userId).avatar(avatar).build();
                            list.add(user);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return list;
    }

    /**
     * 初始化用户好友
     *
     * @param userId
     */
    public List<User> initUserFrineds(String operateUserId, String userId) {
        if (StringUtils.isBlank(userId)) {
            return new ArrayList<>();
        }
        List<User> userList = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_USER_FRIENDS_BY_ID, jsonObject);
            if (result == null) {
                return userList;
            }
            JSONObject resultData = result.getData();
            if (resultData == null) {
                return userList;
            }
            JSONArray jsonArray = resultData.getJSONArray("obj");
            if (jsonArray == null) {
                return userList;
            }
            for (Object o : jsonArray) {
                JSONObject o1 = (JSONObject)o;
                String userId2 = o1.getString("id");
                String nick = o1.getString("realname");
                String avatar = o1.getString("avatar");
                User user = User.newBuilder().nick(nick).id(userId2).userId(userId2).avatar(avatar).build();
                userList.add(user);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }

        return userList;
    }

    /**
     * 初始化用户好友
     *
     * @param userList
     */
    private void initUserFrineds(String operateUserId, List<User> userList) {
        if (userList == null || userList.size() == 0) {
            return;
        }
        for (User user : userList) {
            List<String> friendIds = getUserFriendsIdsByUserId(operateUserId, user.getUserId());
            if (friendIds != null && friendIds.size() > 0) {
                List<User> friendList = batchGetUserListByIds(operateUserId, friendIds);
                if (friendList.size() > 0) {
                    user.setFriends(friendList);
                }
            }
        }

    }

    /**
     * 初始化用户群组
     *
     * @param userId
     */
    public List<Group> initUserGroups(String operateUserId, String userId) {
        if (StringUtils.isBlank(userId)) {
            return new ArrayList<>();
        }
        // List<String> groupIds = getAllGroupUsers(userId);
        // if (groupIds != null && groupIds.size() > 0) {
        // return batchGetGroupsByIds(groupIds);
        // }
        List<Group> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_GROUP_INFO_BY_USER_ID, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONArray groupList = resultData.getJSONArray("obj");
                    for (Object o : groupList) {
                        JSONObject o1 = (JSONObject)o;
                        JSONObject groupInfo = o1.getJSONObject("group_info");
                        if (groupInfo == null) {
                            continue;
                        }
                        String groupId = groupInfo.getString("id");
                        String groupName = groupInfo.getString("group_name");
                        Long groupNo = groupInfo.getLong("group_no");
                        JSONArray members = o1.getJSONArray("members");
                        List<User> userList = new ArrayList<>();
                        if (members != null) {
                            for (Object member : members) {
                                JSONObject member1 = (JSONObject)member;
                                String userId1 = member1.getString("id");
                                String username = member1.getString("nick_name");
                                String realname = member1.getString("nick_name");
                                String avatar = member1.getString("avatar");
                                // 根据userId 获取好友和群组
                                User build = User.newBuilder().id(userId1).userId(userId1).nick(realname).avatar(avatar)
                                    .addExtra("username", username).build();
                                userList.add(build);
                            }
                        }
                        Group group = Group.newBuilder().groupId(groupId).groupNo(groupNo).name(groupName)
                            .users(userList).build();
                        list.add(group);
                    }
                    for (int i = 0; i < groupList.size(); i++) {
                        JSONObject json = groupList.getJSONObject(i);
                        String groupId = json.getString("id");
                        String groupName = json.getString("group_name");
                        Long groupNo = json.getLong("group_no");
                        Group group = Group.newBuilder().groupId(groupId).groupNo(groupNo).name(groupName).build();
                        list.add(group);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return list;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 初始化用户群组
     *
     * @param userList
     */
    private void initUserGroups(String operateUserId, List<User> userList) {
        if (userList == null || userList.size() == 0) {
            return;
        }
        for (User user : userList) {
            List<String> groupIds = getAllGroupUsers(operateUserId, user.getUserId());
            if (groupIds != null && groupIds.size() > 0) {
                List<Group> groupList = batchGetGroupsByIds(operateUserId, groupIds);
                if (groupList != null && groupList.size() > 0) {
                    user.setGroups(groupList);
                }
            }
        }
    }

    /**
     * 批量获取组信息
     *
     * @param ids
     * @return
     */
    public List<Group> batchGetGroupsByIds(String operateUserId, List<String> ids) {
        List<Group> list = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ids", ids);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_GROUPS_BY_IDS, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONArray groupList = resultData.getJSONArray("list");
                    for (int i = 0; i < groupList.size(); i++) {
                        JSONObject json = groupList.getJSONObject(i);
                        String groupId = json.getString("id");
                        String groupName = json.getString("group_name");
                        Long groupNo = json.getLong("group_no");
                        Group group = Group.newBuilder().groupId(groupId).groupNo(groupNo).name(groupName).build();
                        list.add(group);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return list;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 获取单个组信息
     *
     * @param groupId
     * @return
     */
    public Group getGroupById(String operateUserId, String groupId) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", groupId);
            LoginUser loginUser = Environment.getCurrentUser();
            String userId = "";
            if (loginUser != null) {
                userId = loginUser.getUserId();
            }
            jsonObject.put("user_id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_GROUP_INFO_BY_ID, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONObject groupInfoDto = resultData.getJSONObject("group_info_dto");
                    if (groupInfoDto == null) {
                        return null;
                    }
                    String groupName = groupInfoDto.getString("group_name");
                    Long groupNo = groupInfoDto.getLong("group_no");
                    JSONArray members = groupInfoDto.getJSONArray("members");
                    List<User> userList = new ArrayList<>();
                    if (members != null) {
                        for (Object member : members) {
                            JSONObject member1 = (JSONObject)member;
                            String userId1 = member1.getString("id");
                            String username = member1.getString("nick_name");
                            String realname = member1.getString("nick_name");
                            String avatar = member1.getString("avatar");
                            // 根据userId 获取好友和群组
                            User build = User.newBuilder().id(userId1).userId(userId1).nick(realname).avatar(avatar)
                                .addExtra("username", username).build();
                            userList.add(build);
                        }
                    }
                    return Group.newBuilder().groupId(groupId).groupNo(groupNo).name(groupName).users(userList).build();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 批量获取用户好友信息
     *
     * @param userId
     * @return
     */
    public List<String> getUserFriendsIdsByUserId(String operateUserId, String userId) {
        List<String> friendIds = new ArrayList<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_ids", Collections.singletonList(userId));
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.GET_ALL_USERS_FRIENDS_BY_IDS, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONObject userMap = resultData.getJSONObject("obj");
                    if (userMap != null) {
                        Map<String, Object> map = JSON.parseObject(userMap.toJSONString(), Map.class);
                        if (map != null) {
                            Object obj = map.get(userId);
                            if (obj != null) {
                                JSONArray friendsList = JSON.parseArray(obj.toString());
                                for (int i = 0; i < friendsList.size(); i++) {
                                    JSONObject json = friendsList.getJSONObject(i);
                                    String friendId = json.getString("friend_id");
                                    if (StringUtils.isNotBlank(friendId)) {
                                        friendIds.add(friendId);
                                    }
                                }
                            }
                        }

                    }
                }
            }
            return friendIds;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return friendIds;
        } finally {
            Environment.remove();
        }

    }

    /**
     * 添加好友
     *
     * @param curUserId
     * @param friendUserId
     * @return
     */
    public boolean addFriend(String operateUserId, String curUserId, String friendUserId) {

        boolean success = false;
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", curUserId);
            jsonObject.put("friend_id", friendUserId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.ADD_FRIEND, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    success = resultData.getBoolean("success");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return success;
    }

    /**
     * 删除好友
     *
     * @param curUserId
     * @param friendUserId
     * @return
     */
    public boolean deleteFriend(String operateUserId, String curUserId, String friendUserId) {
        boolean success = false;
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", curUserId);
            jsonObject.put("friend_id", friendUserId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.DELETE_FRIEND, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    success = resultData.getBoolean("success");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return success;
    }

    /**
     * 新增群组
     *
     * @param userId
     * @param groupName
     * @return
     */
    public String addGroupInfo(String operateUserId, String userId, String groupName) {
        String groupId = null;
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            jsonObject.put("group_name", groupName);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.ADD_GROUP, jsonObject);
            if (result != null) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    groupId = resultData.getString("group_id");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        return groupId;
    }

    /**
     *
     * @param chatBody
     */
    public void writeMessage(String operateUserId, ChatBody chatBody) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", chatBody.getFrom());
            jsonObject.put("group_id", chatBody.getGroupId());
            jsonObject.put("friend_id", chatBody.getTo());
            jsonObject.put("type", chatBody.getMsgType());
            jsonObject.put("last_msg_id", chatBody.getId());
            jsonObject.put("content", chatBody.getContent());
            jsonObject.put("chat_type", chatBody.getChatType());
            jsonObject.put("time_comparer", chatBody.getCreateTime());
            // Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.CHAT_128200, jsonObject);
            // if (result.isSuccess()) {
            // }
            boolean b = mqMsgProducerService.addChatMsg(jsonObject);
            if (!b) {
                logger.error("添加失败!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
    }

    /**
     * 获取离线消息记录
     *
     * @param userId
     */
    public UserMessageData getOfflineMessage(String operateUserId, String userId) {
        UserMessageData userMessageData = new UserMessageData();
        userMessageData.setUserId(userId);
        Map<String, List<ChatBody>> friends = new HashMap<>();
        Map<String, List<ChatBody>> groups = new HashMap<>();
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.CHAT_128201, jsonObject);
            if (result.isSuccess()) {
                JSONObject resultData = result.getData();
                if (resultData != null) {
                    JSONArray object = resultData.getJSONArray("object");
                    if (object != null) {
                        for (Object o : object) {
                            JSONObject o1 = (JSONObject)o;
                            String fromUserId = o1.getString("from_user_id");
                            String fromGroupId = o1.getString("from_group_id");
                            JSONArray offLineMsgList = o1.getJSONArray("off_line_msg");
                            List<ChatBody> chatBodies = assJsonArrayToChatBody(operateUserId, offLineMsgList);
                            // 好友
                            if (StringUtils.isBlank(fromGroupId)) {
                                List<ChatBody> chatBodies1 =
                                    friends.computeIfAbsent(fromUserId, k -> new ArrayList<>());
                                chatBodies1.addAll(chatBodies);
                            }
                            // 群消息
                            else {
                                List<ChatBody> chatBodies1 = groups.computeIfAbsent(fromUserId, k -> new ArrayList<>());
                                chatBodies1.addAll(chatBodies);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            Environment.remove();
        }
        userMessageData.setFriends(friends);
        userMessageData.setGroups(groups);
        return userMessageData;
    }

    private List<ChatBody> assJsonArrayToChatBody(String operateUserId, JSONArray offLineMsgList) {
        if (offLineMsgList == null) {
            return new ArrayList<>();
        }
        List<ChatBody> chatBodyList = new ArrayList<>(offLineMsgList.size());
        try {
            AuthCacheService.setEnvironment(operateUserId);

            for (Object o : offLineMsgList) {
                JSONObject o1 = (JSONObject)o;
                String fromId = o1.getString("user_id");
                String groupId = o1.getString("group_id");
                Integer msgType = o1.getInteger("type");
                String content = o1.getString("content");
                String toId = o1.getString("friend_id");
                String id = o1.getString("last_msg_id");
                Date createTime = o1.getDate("create_time");
                Integer chatType = o1.getInteger("chat_type");
                if (chatType == null) {
                    if (StringUtils.isNotBlank(groupId)) {
                        chatType = ChatType.CHAT_TYPE_PUBLIC.getNumber();
                    } else {
                        chatType = ChatType.CHAT_TYPE_PRIVATE.getNumber();
                    }
                }
                ChatBody build = ChatBody.newBuilder().groupId(groupId).from(fromId).to(toId)
                    .msgType(msgType == null ? 0 : msgType).content(content).setId(id).chatType(chatType)
                    .setCreateTime(createTime == null ? System.currentTimeMillis() : createTime.getTime()).build();
                chatBodyList.add(build);
            }
            return chatBodyList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Environment.remove();
        }
        return chatBodyList;
    }

    /**
     * 修改消息LastMsgId
     *
     * @param fromId
     * @param friendId
     * @param groupId
     * @param lastMsgId
     * @return
     */
    public boolean updateLastMsgId(String operateUserId, String fromId, String friendId, String groupId,
        String groupMemberId, String lastMsgId) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", fromId);
            jsonObject.put("group_id", groupId);
            jsonObject.put("friend_id", friendId);
            jsonObject.put("group_member_id", groupMemberId);
            jsonObject.put("last_msg_id", lastMsgId);
            // Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.CHAT_128202, jsonObject);
            // if (result.isSuccess()) {
            // JSONObject data = result.getData();
            // Boolean success = data.getBoolean("success");
            // return success == null ? false : success;
            // }
            mqMsgProducerService.updateLastMsgId(jsonObject);
            return true;
        } catch (Exception e) {
            logger.error("修改最后的消息失败", e);
            return false;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 解散群组
     *
     * @param userId
     * @param groupId
     * @return
     */
    public boolean disbandGroup(String operateUserId, String userId, String groupId) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", groupId);
            Result<JSONObject> result = HttpApiUtils.post(ApiMethodConstants.DISBAND_GROUP_128201, jsonObject);
            if (result.isSuccess()) {
                JSONObject data = result.getData();
                Boolean success = data.getBoolean("success");
                return success == null ? false : success;
            }
            logger.error(result.getBizMsg());
            return false;
        } catch (Exception e) {
            logger.error("解算群组失败", e);
            return false;
        } finally {
            Environment.remove();
        }
    }

    /**
     * 新增用户在线状态记录
     *
     * @param paramMap
     * @return
     */
    public boolean addUserOnlineStatusRecord(String operateUserId, Map<String, Object> paramMap) {
        try {
            AuthCacheService.setEnvironment(operateUserId);
            JSONObject jsonObject = new JSONObject();
            // map 转jsonObject
            toJsonObj(paramMap, jsonObject);
            // Result<JSONObject> result =
            // HttpApiUtils.post(ApiMethodConstants.SAVE_USER_ONLINE_STATUS_128300, jsonObject);
            // if (result.isSuccess()) {
            // return true;
            // }
            mqMsgProducerService.updateUserOnlineStatus(jsonObject);
            return true;
        } catch (Exception e) {
            logger.error("新增用户状态记录错误", e);
            return false;
        } finally {
            Environment.remove();
        }
    }

    /**
     * map 转json
     *
     * @param map
     * @param resultJson
     * @return
     */
    private JSONObject toJsonObj(Map<String, Object> map, JSONObject resultJson) {
        for (String key : map.keySet()) {
            resultJson.put(key, map.get(key));
        }
        return resultJson;
    }

}
