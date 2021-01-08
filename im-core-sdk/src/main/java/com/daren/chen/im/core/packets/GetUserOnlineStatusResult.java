package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 */
public enum GetUserOnlineStatusResult {
    /**
     * <pre>
     * 获取状态是不，原因为其它
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_UNKNOWN = 0;</code>
     */
    GET_USER_ONLINE_STATUS_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 获取状态成功
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_OK = 1;</code>
     */
    GET_USER_ONLINE_STATUS_RESULT_OK(1),;

    public final int getNumber() {
        return value;
    }

    public static GetUserOnlineStatusResult valueOf(int value) {
        return forNumber(value);
    }

    public static GetUserOnlineStatusResult forNumber(int value) {
        switch (value) {
            case 0:
                return GET_USER_ONLINE_STATUS_RESULT_UNKNOWN;
            case 1:
                return GET_USER_ONLINE_STATUS_RESULT_OK;
            default:
                return null;
        }
    }

    private final int value;

    private GetUserOnlineStatusResult(int value) {
        this.value = value;
    }
}
