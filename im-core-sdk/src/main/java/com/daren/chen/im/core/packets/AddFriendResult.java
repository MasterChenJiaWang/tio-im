package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 */
public enum AddFriendResult {
    /**
     * <pre>
     *不允许添加，原因为其它
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_UNKNOWN = 0;</code>
     */
    ADD_FRIEND_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 允许添加
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_OK = 1;</code>
     */
    ADD_FRIEND_RESULT_OK(1),
    /**
     * <pre>
     * 好友不存在
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_NOT_EXIST = 2;</code>
     */
    ADD_FRIEND_RESULT_NOT_EXIST(2),
    /**
     * <pre>
     * 好友满了
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_GROUP_FULL = 3;</code>
     */
    ADD_FRIEND_RESULT_GROUP_FULL(3),
    /**
     * <pre>
     * 在黑名单中
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_IN_BACKLIST = 4;</code>
     */
    ADD_FRIEND_RESULT_IN_BACKLIST(4),
    /**
     * <pre>
     * 被踢
     * </pre>
     *
     * <code>ADD_FRIEND_RESULT_KICKED = 5;</code>
     */
    ADD_FRIEND_RESULT_KICKED(5),;

    public final int getNumber() {
        return value;
    }

    public static AddFriendResult valueOf(int value) {
        return forNumber(value);
    }

    public static AddFriendResult forNumber(int value) {
        switch (value) {
            case 0:
                return ADD_FRIEND_RESULT_UNKNOWN;
            case 1:
                return ADD_FRIEND_RESULT_OK;
            case 2:
                return ADD_FRIEND_RESULT_NOT_EXIST;
            case 3:
                return ADD_FRIEND_RESULT_GROUP_FULL;
            case 4:
                return ADD_FRIEND_RESULT_IN_BACKLIST;
            case 5:
                return ADD_FRIEND_RESULT_KICKED;
            default:
                return null;
        }
    }

    private final int value;

    private AddFriendResult(int value) {
        this.value = value;
    }
}
