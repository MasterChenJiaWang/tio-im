/**
 *
 */
package com.daren.chen.im.core.packets;

import com.alibaba.fastjson.JSONObject;

/**
 * 版本: [1.0] 功能说明: 作者: WChao 创建时间: 2017年7月26日 上午11:34:44
 */
public class ChatBody extends BaseChatBody {

    private static final long serialVersionUID = 5731474214655476286L;
    /**
     * 消息类型;(如：0:text、1:image、2:voice、3:vedio、4:music、5:news)
     */
    private Integer msgType;
    /**
     * 聊天类型;(如公聊、私聊)
     */
    private Integer chatType;
    /**
     * 消息内容;
     */
    private String content;

    /**
     * 自增ID
     */
    private Long uid;

    private ChatBody() {
        this.dataType = 1;
    }

    private ChatBody(String id, String from, String to, Integer msgType, Integer chatType, String content,
        String groupId, Integer cmd, Long createTime, JSONObject extras, Long uid) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.msgType = msgType;
        this.chatType = chatType;
        this.content = content;
        this.groupId = groupId;
        this.cmd = cmd;
        this.createTime = createTime;
        this.extras = extras;
        this.dataType = 1;
        this.uid = uid;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Integer getMsgType() {
        return msgType;
    }

    public ChatBody setMsgType(Integer msgType) {
        this.msgType = msgType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ChatBody setContent(String content) {
        this.content = content;
        return this;
    }

    public Integer getChatType() {
        return chatType;
    }

    public ChatBody setChatType(Integer chatType) {
        this.chatType = chatType;
        return this;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public static class Builder extends Message.Builder<ChatBody, Builder> {
        /**
         * 来自user_id;
         */
        private String from;
        /**
         * 目标user_id;
         */
        private String to;
        /**
         * 消息类型;(如：0:text、1:image、2:voice、3:vedio、4:music、5:news)
         */
        private Integer msgType;
        /**
         * 聊天类型;(如公聊、私聊)
         */
        private Integer chatType;
        /**
         * 消息内容;
         */
        private String content;
        /**
         * 消息发到哪个群组;
         */
        private String groupId;

        /**
         * 自增ID
         */
        private Long uid;

        public Builder() {};

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder msgType(Integer msgType) {
            this.msgType = msgType;
            return this;
        }

        public Builder chatType(Integer chatType) {
            this.chatType = chatType;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder uid(Long uid) {
            this.uid = uid;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public ChatBody build() {
            return new ChatBody(this.id, this.from, this.to, this.msgType, this.chatType, this.content, this.groupId,
                this.cmd, this.createTime, this.extras, this.uid);
        }
    }
}
