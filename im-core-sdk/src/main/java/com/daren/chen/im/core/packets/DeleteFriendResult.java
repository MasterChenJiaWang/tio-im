package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 */
public enum DeleteFriendResult {
    /**
     * <pre>
     *不允许删除，原因为其它
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_UNKNOWN = 0;</code>
     */
    DELETE_FRIEND_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 允许删除
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_OK = 1;</code>
     */
    DELETE_FRIEND_RESULT_OK(1),
    /**
     * <pre>
     * 好友不存在
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_NOT_EXIST = 2;</code>
     */
    DELETE_FRIEND_RESULT_NOT_EXIST(2),
    /**
     * <pre>
     * 好友满了
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_GROUP_FULL = 3;</code>
     */
    DELETE_FRIEND_RESULT_GROUP_FULL(3),
    /**
     * <pre>
     * 在黑名单中
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_IN_BACKLIST = 4;</code>
     */
    DELETE_FRIEND_RESULT_IN_BACKLIST(4),
    /**
     * <pre>
     * 被踢
     * </pre>
     *
     * <code>DELETE_FRIEND_RESULT_KICKED = 5;</code>
     */
    DELETE_FRIEND_RESULT_KICKED(5),;

    public final int getNumber() {
        return value;
    }

    public static DeleteFriendResult valueOf(int value) {
        return forNumber(value);
    }

    public static DeleteFriendResult forNumber(int value) {
        switch (value) {
            case 0:
                return DELETE_FRIEND_RESULT_UNKNOWN;
            case 1:
                return DELETE_FRIEND_RESULT_OK;
            case 2:
                return DELETE_FRIEND_RESULT_NOT_EXIST;
            case 3:
                return DELETE_FRIEND_RESULT_GROUP_FULL;
            case 4:
                return DELETE_FRIEND_RESULT_IN_BACKLIST;
            case 5:
                return DELETE_FRIEND_RESULT_KICKED;
            default:
                return null;
        }
    }

    private final int value;

    private DeleteFriendResult(int value) {
        this.value = value;
    }
}
