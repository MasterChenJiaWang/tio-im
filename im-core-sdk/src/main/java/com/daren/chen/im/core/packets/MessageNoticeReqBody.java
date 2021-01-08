package com.daren.chen.im.core.packets;

/**
 * @author WChao
 * @date 2018年4月10日 下午3:18:06
 */
public class MessageNoticeReqBody extends Message {

    private static final long serialVersionUID = 6486393588840919832L;
    /**
     * 是否接收成功;
     */
    private boolean receive;

    public boolean isReceive() {
        return receive;
    }

    public void setReceive(boolean receive) {
        this.receive = receive;
    }
}
