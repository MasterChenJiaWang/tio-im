package com.daren.chen.im.server.springboot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.cache.redis.RedisCacheManager;
import com.daren.chen.im.core.packets.Group;
import com.daren.chen.im.core.packets.LoginUser;
import com.daren.chen.im.core.packets.User;
import com.daren.chen.im.server.springboot.helper.redis.ChatCommonMethodUtils;
import com.daren.chen.im.server.springboot.helper.redis.LocalCacheUtils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.json.JSONUtil;

/**
 * 数据初始化
 *
 * @Description:
 * @author: chendaren
 * @CreateDate: 2021/1/5 18:02
 */
@Component
public class UserDataInit implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserDataInit.class);
    /**
     *
     */
    private static FileAppender APPENDER = null;

    private static final String FILE_PATH = UserDataInit.class.getClassLoader().getResource("user_info.txt").getFile();
    private static File file = null;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 等待初始化完成
        Thread.sleep(3000);
        //
        // List<User> users = read();
        // //
        // save(users);
    }

    private void save(List<User> users) {
        if (CollectionUtil.isNotEmpty(users)) {
            Map<String, List<String>> groupUserIdList = new HashMap<>();
            for (User user : users) {
                List<String> groupIds = user.getGroupIds();
                if (CollectionUtil.isNotEmpty(groupIds)) {
                    LocalCacheUtils.me().saveUserGroupIdsByCache(user.getUserId(), groupIds);
                    //
                    for (String groupId : groupIds) {
                        List<String> orDefault = groupUserIdList.getOrDefault(groupId, new ArrayList<>());
                        orDefault.add(user.getUserId());
                        groupUserIdList.put(groupId, orDefault);
                    }
                }
                List<String> friendIds = user.getFriendIds();
                if (CollectionUtil.isNotEmpty(friendIds)) {
                    LocalCacheUtils.me().saveUserFriendIdsByCache(user.getUserId(), friendIds);
                }
                User build = User.newBuilder().userId(user.getUserId()).id(user.getUserId()).nick(user.getNick())
                    .avatar(user.getAvatar()).build();
                LocalCacheUtils.me().saveUserInfoByCache(user.getUserId(), build);
                logger.info(" {} 初始化完成!", user.getUserId());
            }

            if (!groupUserIdList.isEmpty()) {
                groupUserIdList.forEach((groupId, list) -> {
                    Group group = Group.newBuilder().groupId(groupId).name("群名称-" + groupId).build();
                    LocalCacheUtils.me().saveGroupInfoByCache(groupId, group);
                    //
                    if (CollectionUtil.isNotEmpty(list)) {
                        LocalCacheUtils.me().saveGroupUserIdsByCache(groupId, list);
                    }
                });
            }

            logger.info(" 全部 初始化完成!");
        }
    }

    private void initData() {
        List<User> users = initUserId();
        for (User user : users) {
            process(user);
        }
    }

    /**
     * 添加到文件
     *
     * @param user
     */
    private void process(User user) {
        if (null == user) {
            return;
        }
        try {
            if (APPENDER == null) {
                file = new File(FILE_PATH);
                if (!file.exists()) {
                    FileUtil.touch(file);
                }
                APPENDER = new FileAppender(file, 16, true);
            }
            APPENDER.append(JSONUtil.toJsonStr(user));
            APPENDER.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<User> read() {
        List<User> userList = new ArrayList<>();
        try {
            if (file == null) {
                file = new File(FILE_PATH);
            }
            if (!file.exists()) {
                FileUtil.touch(file);
            }
            List<String> strings = FileUtil.readLines(file, StandardCharsets.UTF_8);
            if (CollectionUtil.isNotEmpty(strings)) {
                for (String string : strings) {
                    if (StringUtils.isBlank(string)) {
                        continue;
                    }
                    User user = JSONUtil.toBean(string, User.class);
                    if (null == user) {
                        continue;
                    }
                    userList.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    private List<User> initUserId() {
        List<String> userIds = new ArrayList<>();
        long phone = 13888800001L;
        for (int i = 0; i < 10000; i++) {
            userIds.add(phone + "");
            phone++;
        }
        // List<String> groupIds = new ArrayList<>();
        // int groupId = 1;
        // for (int i = 0; i < 10000; i++) {
        // groupIds.add(groupId + "");
        // groupId++;
        // }
        List<User> userList = new ArrayList<>();
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId("b9343ad1b51646d4b6ba8b33283012cb");
        loginUser.setToken(
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdWJqZWN0IiwibG9naW5Vc2VyIjoiYzllMGUwYjBiNDQ1Yjc3NDYzN2MyMDVhY2VhNWEyNWMiLCJsb2dpblVzZXJOYW1lIjoiYWRtaW4iLCJhcHBJZCI6IjExMTExMSIsImFwcENvZGUiOiJlMTBhZGMzOTQ5YmE1OWFiYmU1NmUwNTdmMjBmODgzZSIsImV4cCI6MTYxMDUwMjMwMSwiaWF0IjoxNjA5ODk3NTAxfQ.MSgsiSUxpUEWi3NJygGuSc5yofqVzn0TI2EaYb4tMVHDhl6Fez7fGx751_fmx0gDgn60JSVcIwK8fkzTlCTlHA6eVxVzJK7-as3dm7grXL6qh2BjvhdNSbdLeH9-qFBJJCWloG4j_I-FHhjWCzczPO_Onsffhn1T9O-F7lruSv0");
        RedisCacheManager.getCache(ImConst.USER_TOKEN).put("b9343ad1b51646d4b6ba8b33283012cb", loginUser);
        ChatCommonMethodUtils chatCommonMethodUtils = new ChatCommonMethodUtils();

        for (String userId : userIds) {
            User newUser = chatCommonMethodUtils.getUserById(loginUser.getUserId(), userId);
            //
            List<User> friends = chatCommonMethodUtils.initUserFrineds(loginUser.getUserId(), userId);
            List<String> groupIdsList = chatCommonMethodUtils.getAllGroupUsers(loginUser.getUserId(), userId);
            if (newUser == null) {
                continue;
            }
            newUser.setGroupIds(groupIdsList);
            newUser.setFriends(friends);
            // LocalCacheUtils.me().saveUserInfoByCache(userId, user);
            //
            if (CollectionUtils.isEmpty(friends)) {
                userList.add(newUser);
                continue;
            }
            List<String> friendIds = friends.stream().filter(user1 -> StringUtils.isNotBlank(user1.getUserId()))
                .map(User::getUserId).collect(Collectors.toList());
            newUser.setFriendIds(friendIds);
            userList.add(newUser);
            // LocalCacheUtils.me().saveUserFriendIdsByCache(userId, friendIds);
        }

        return userList;
    }

}
