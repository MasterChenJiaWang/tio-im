/**
 *
 */
package com.daren.chen.im.core.packets;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * 消息ack 通知
 */
public class ChatAckBody extends BaseChatBody {

    private static final long serialVersionUID = 8964074915235387802L;

    /**
     *
     */
    private List<String> msgIds;

    public List<String> getMsgIds() {
        return msgIds;
    }

    public void setMsgIds(List<String> msgIds) {
        this.msgIds = msgIds;
    }

    private ChatAckBody() {
        this.dataType = 2;
    }

    private ChatAckBody(String id, String from, String to, String groupId, Integer cmd, Long createTime,
        List<String> msgIds, JSONObject extras) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.groupId = groupId;
        this.cmd = cmd;
        this.createTime = createTime;
        this.extras = extras;
        this.dataType = 2;
        this.msgIds = msgIds;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends Message.Builder<ChatAckBody, Builder> {
        /**
         * 来自user_id;
         */
        private String from;
        /**
         * 目标user_id;
         */
        private String to;
        /**
         * 消息发到哪个群组;
         */
        private String groupId;

        /**
         *
         */
        private List<String> msgIds = new ArrayList<>();

        public Builder() {};

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder msgIds(List<String> allMsgIds) {
            this.msgIds.addAll(allMsgIds);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public ChatAckBody build() {
            return new ChatAckBody(this.id, this.from, this.to, this.groupId, this.cmd, this.createTime, this.msgIds,
                this.extras);
        }
    }
}
