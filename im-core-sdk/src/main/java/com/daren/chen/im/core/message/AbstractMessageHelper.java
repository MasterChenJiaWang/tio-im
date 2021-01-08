/**
 *
 */
package com.daren.chen.im.core.message;

import com.daren.chen.im.core.ImConst;
import com.daren.chen.im.core.config.ImConfig;

/**
 * @author HP
 *
 */
public abstract class AbstractMessageHelper implements MessageHelper, ImConst {

    protected ImConfig imConfig;

    public ImConfig getImConfig() {
        return imConfig;
    }

    public void setImConfig(ImConfig imConfig) {
        this.imConfig = imConfig;
    }
}
