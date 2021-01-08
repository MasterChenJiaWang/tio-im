package com.daren.chen.im.client.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.json.JSONUtil;

/**
 * @Description:
 * @author: chenjiawang
 * @CreateDate: 2020/11/19 19:49
 */
public class TokenToFileProcessor {

    /**
     *
     */
    private static FileAppender APPENDER = null;

    private static final String FILE_PATH =
        TokenToFileProcessor.class.getClassLoader().getResource("user_token.txt").getFile();
    private static File file = null;

    /**
     * 添加到文件
     *
     * @param userToken
     */
    public void process(UserToken userToken) {
        if (null == userToken) {
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
            APPENDER.append(JSONUtil.toJsonStr(userToken));
            APPENDER.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void read(Map<String, UserToken> map) {
        try {
            if (map == null) {
                return;
            }
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
                    UserToken userToken = JSONUtil.toBean(string, UserToken.class);
                    if (null == userToken) {
                        continue;
                    }
                    map.put(userToken.getUserId(), userToken);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
