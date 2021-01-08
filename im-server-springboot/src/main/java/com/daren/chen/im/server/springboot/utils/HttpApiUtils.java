package com.daren.chen.im.server.springboot.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.daren.chen.im.core.config.ApiServerConfig;
import com.daren.chen.im.core.config.ImConfig;
import com.daren.chen.im.server.config.ImServerConfig;
import com.daren.chen.im.server.springboot.common.FieldConstants;
import com.daren.chen.im.server.springboot.common.Method;
import com.daren.chen.im.server.springboot.entity.GatewayRequest;
import com.daren.chen.im.server.springboot.entity.RequestDto;
import com.daren.chen.im.server.springboot.entity.ResponseDto;
import com.daren.chen.im.server.springboot.entity.Result;
import com.daren.chen.im.server.util.Environment;

import cn.hutool.core.util.StrUtil;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/19 16:54
 */
public class HttpApiUtils {

    protected static ImServerConfig imServerConfig = ImConfig.Global.get();

    /**
     *
     * @param o
     * @return
     */
    public static Result<JSONObject> post(String method, JSONObject o) {

        if (imServerConfig == null) {
            imServerConfig = ImConfig.Global.get();
        }
        GatewayRequest<Object> request = new GatewayRequest<>();
        try {
            ApiServerConfig apiServerConfig = imServerConfig.getApiServerConfig();

            request.setMethod(method);
            request.setAppId(Environment.getAppId());
            request.setAppKey(Environment.getAppKey());
            request.setContent(o);

            RequestDto requestDto = new RequestDto();
            requestDto.setMethod(Method.POST_STRING);
            requestDto.setHost(apiServerConfig.getUrl());
            HashMap<String, String> headers = new HashMap<>(4);
            requestDto.setHeaders(initialBasicHeader(headers, Environment.getToken()));
            requestDto.setStringBody(JSON.toJSONString(request));
            ResponseDto responseDto = HttpUtil.httpPost(requestDto);
            String body = responseDto.getBody();
            if (StrUtil.isBlank(body)) {
                return Result.error(request, "数据为空!");
            }
            return JSON.parseObject(body, new TypeReference<Result<JSONObject>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(request, e.getMessage());
        }
    }

    protected static Map<String, String> initialBasicHeader(Map<String, String> headers, String token) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Content-Type", "application/json;charset=UTF-8");
        if (StringUtils.hasText(token) && token.contains(FieldConstants.REQ_BEARER)) {
            token = token.substring(7);
        }
        headers.put(FieldConstants.REQ_AUTHORIZATION, FieldConstants.REQ_BEARER + token);
        return headers;
    }
}
