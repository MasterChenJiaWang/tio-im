package com.daren.chen.im.server.springboot.common;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.util.TypeUtils;
import com.daren.chen.im.server.springboot.entity.GatewayCode;

/**
 * @program: micro-merger-parent
 * @description:
 * @author: djj
 * @create: 2019-07-15 17:56
 **/

public class GatewayCodeSerialize implements ObjectSerializer, ObjectDeserializer {

    public GatewayCodeSerialize() {}

    @Override
    public GatewayCode deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object value = parser.parse();
        return value == null ? null : GatewayCode.convert(TypeUtils.castToInt(value));
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
        throws IOException {
        serializer.write(((GatewayCode)object).getCode());
    }
}
