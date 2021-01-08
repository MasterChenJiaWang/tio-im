package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 */
public enum MsgNoticeResult {
    /**
     * <pre>
     *不允许添加，原因为其它
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_UNKNOWN = 0;</code>
     */
    MSG_NOTICE_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 允许添加
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_OK = 1;</code>
     */
    MSG_NOTICE_RESULT_OK(1),;

    public final int getNumber() {
        return value;
    }

    public static MsgNoticeResult valueOf(int value) {
        return forNumber(value);
    }

    public static MsgNoticeResult forNumber(int value) {
        switch (value) {
            case 0:
                return MSG_NOTICE_RESULT_UNKNOWN;
            case 1:
                return MSG_NOTICE_RESULT_OK;
            default:
                return null;
        }
    }

    private final int value;

    private MsgNoticeResult(int value) {
        this.value = value;
    }
}
