/**
 *
 */
package com.daren.chen.im.core.packets;

import com.alibaba.fastjson.JSONObject;

/**
 * 消息ack 通知
 */
public class NoticeAckBody extends Message {

    private static final long serialVersionUID = 447383339887767164L;
    /**
     * 发送用户id;
     */
    private String from;
    /**
     * 目标用户id;
     */
    private String to;

    private NoticeAckBody() {}

    private NoticeAckBody(String id, String from, String to, Integer cmd, Long createTime, JSONObject extras) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.cmd = cmd;
        this.createTime = createTime;
        this.extras = extras;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getFrom() {
        return from;
    }

    public NoticeAckBody setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public NoticeAckBody setTo(String to) {
        this.to = to;
        return this;
    }

    public static class Builder extends Message.Builder<NoticeAckBody, Builder> {
        /**
         * 来自user_id;
         */
        private String from;
        /**
         * 目标user_id;
         */
        private String to;

        public Builder() {};

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public NoticeAckBody build() {
            return new NoticeAckBody(this.id, this.from, this.to, this.cmd, this.createTime, this.extras);
        }
    }
}
