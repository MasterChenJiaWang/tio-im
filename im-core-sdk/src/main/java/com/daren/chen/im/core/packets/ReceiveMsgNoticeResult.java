package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 接收通知的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 *
 * @author chendaren
 */
public enum ReceiveMsgNoticeResult {
    /**
     * <pre>
     *接收通知，原因为其它
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_UNKNOWN = 0;</code>
     */
    RECEIVE_MSG_NOTICE_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 接收通知
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_OK = 1;</code>
     */
    RECEIVE_MSG_NOTICE_RESULT_OK(1),;

    public final int getNumber() {
        return value;
    }

    public static ReceiveMsgNoticeResult valueOf(int value) {
        return forNumber(value);
    }

    public static ReceiveMsgNoticeResult forNumber(int value) {
        switch (value) {
            case 0:
                return RECEIVE_MSG_NOTICE_RESULT_UNKNOWN;
            case 1:
                return RECEIVE_MSG_NOTICE_RESULT_OK;
            default:
                return null;
        }
    }

    private final int value;

    private ReceiveMsgNoticeResult(int value) {
        this.value = value;
    }
}
