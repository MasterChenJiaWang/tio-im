package com.daren.chen.im.core.session.id;

import com.daren.chen.im.core.http.HttpConfig;

/**
 * @author wchao 2017年8月15日 上午10:49:58
 */
public interface ISessionIdGenerator {

    /**
     *
     * @return
     * @author wchao
     */
    String sessionId(HttpConfig httpConfig);

}
