package com.daren.chen.im.server.springboot.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.daren.chen.im.server.springboot.common.GatewayCodeSerialize;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by JiKun.Li on 2017-10-20. Email: tracenet@126.com
 */
@JSONType(deserializer = GatewayCodeSerialize.class)
public enum GatewayCode {

    /**
     * 请求成功
     */
    SUCCESS(200, "请求成功"),
    /**
     * 请求失败
     */
    ERROR(500, "请求失败");

    private String message;

    private Integer code;

    GatewayCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    private void setCode(Integer code) {
        this.code = code;
    }

    @JsonCreator
    public static GatewayCode convert(int code) {
        GatewayCode[] enums = GatewayCode.values();
        for (GatewayCode e : enums) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
