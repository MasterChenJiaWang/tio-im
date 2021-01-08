package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 */
public enum NoticeOfflineResult {
    /**
     * <pre>
     *下线失败，原因为其它
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_UNKNOWN = 0;</code>
     */
    NOTICE_OFFLINE_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 下线成功
     * </pre>
     *
     * <code>MSG_NOTICE__RESULT_OK = 1;</code>
     */
    NOTICE_OFFLINE_RESULT_OK(1),;

    public final int getNumber() {
        return value;
    }

    public static NoticeOfflineResult valueOf(int value) {
        return forNumber(value);
    }

    public static NoticeOfflineResult forNumber(int value) {
        switch (value) {
            case 0:
                return NOTICE_OFFLINE_RESULT_UNKNOWN;
            case 1:
                return NOTICE_OFFLINE_RESULT_OK;
            default:
                return null;
        }
    }

    private final int value;

    private NoticeOfflineResult(int value) {
        this.value = value;
    }
}
