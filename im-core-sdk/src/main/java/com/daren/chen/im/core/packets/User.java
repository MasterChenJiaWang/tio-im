/**
 *
 */
package com.daren.chen.im.core.packets;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import cn.hutool.core.bean.BeanUtil;

/**
 * 版本: [1.0] 功能说明:
 *
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户id;
     */
    private String userId;
    /**
     * user nick
     */
    private String nick;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 在线状态(online、offline)
     */
    private String status = UserStatusType.OFFLINE.getStatus();
    /**
     * 个性签名;
     */
    private String sign;
    /**
     * 用户所属终端;(ws、tcp、http、android、ios等)
     */
    private String terminal;

    /**
     * 识别号
     */
    private String identificationNumber;
    /**
     * 好友列表;
     */
    private List<User> friends;
    /**
     * 群组列表;
     */
    private List<Group> groups;

    /**
     * 好友ID列表;
     */
    private List<String> friendIds;
    /**
     * 群组ID列表;
     */
    private List<String> groupIds;

    /**
     * 扩展参数字段
     */
    protected JSONObject extras;

    private User() {}

    private User(String userId, String nick, String avatar, String status, String sign, String terminal,
                 List<User> friends, List<Group> groups, JSONObject extras) {
        this.userId = userId;
        this.nick = nick;
        this.avatar = avatar;
        this.status = status;
        this.sign = sign;
        this.terminal = terminal;
        this.friends = friends;
        this.groups = groups;
        this.extras = extras;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public static class Builder {
        /**
         * 用户id;
         */
        private String userId;
        /**
         * user nick
         */
        private String nick;
        /**
         * 用户头像
         */
        private String avatar;
        /**
         * 在线状态(online、offline)
         */
        private String status = UserStatusType.OFFLINE.getStatus();
        /**
         * 个性签名;
         */
        private String sign;
        /**
         * 用户所属终端;(ws、tcp、http、android、ios等)
         */
        private String terminal;
        /**
         * 好友列表;
         */
        private List<User> friends;
        /**
         * 群组列表;
         */
        private List<Group> groups;

        /**
         * 好友ID列表;
         */
        private List<String> friendIds;
        /**
         * 群组ID列表;
         */
        private List<String> groupIds;

        /**
         * 扩展参数字段
         */
        protected JSONObject extras;

        public Builder() {};

        public Builder id(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder nick(String nick) {
            this.nick = nick;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder sign(String sign) {
            this.sign = sign;
            return this;
        }

        public Builder terminal(String terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder addFriend(User friend) {
            if (CollectionUtils.isEmpty(friends)) {
                friends = Lists.newArrayList();
            }
            friends.add(friend);
            return this;
        }

        public Builder addGroup(Group group) {
            if (CollectionUtils.isEmpty(groups)) {
                groups = Lists.newArrayList();
            }
            groups.add(group);
            return this;
        }

        public Builder groups(List<Group> groups) {
            if (groups != null && groups.size() > 0) {
                groups.addAll(groups);
            }

            return this;
        }

        public Builder friends(List<User> users) {
            if (users != null && users.size() > 0) {
                friends.addAll(users);
            }
            return this;
        }

        public Builder groupIds(List<String> groupIds) {
            if (groupIds != null && groupIds.size() > 0) {
                groupIds.addAll(groupIds);
            }

            return this;
        }

        public Builder friendIds(List<String> userIds) {
            if (userIds != null && userIds.size() > 0) {
                friendIds.addAll(userIds);
            }
            return this;
        }

        public Builder addExtra(String key, Object value) {
            if (null == value) {
                return this;
            } else {
                if (null == extras) {
                    this.extras = new JSONObject();
                }
                this.extras.put(key, value);
                return this;
            }
        }

        // @Override
        protected Builder getThis() {
            return this;
        }

        // @Override
        public User build() {
            return new User(userId, nick, avatar, status, sign, terminal, friends, groups, extras);
        }

    }

    public JSONObject getExtras() {
        return extras;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    @Override
    public User clone() {
        User cloneUser = new User();
        BeanUtil.copyProperties(this, cloneUser, "friends", "groups");
        return cloneUser;
    }

}
