package com.daren.chen.im.core.packets;

/**
 * <pre>
 **
 * 加入群组申请的结果
 * </pre>
 *
 * enum {@code JoinGroupResult}
 *
 * @author chendaren
 */
public enum ExitGroupResult {
    /**
     * <pre>
     *不允许离开，原因为其它
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_UNKNOWN = 0;</code>
     */
    EXIT_GROUP_RESULT_UNKNOWN(0),
    /**
     * <pre>
     * 允许离开
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_OK = 1;</code>
     */
    EXIT_GROUP_RESULT_OK(1),
    /**
     * <pre>
     * 组不存在
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_NOT_EXIST = 2;</code>
     */
    EXIT_GROUP_RESULT_NOT_EXIST(2),
    /**
     * <pre>
     * 组满
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_GROUP_FULL = 3;</code>
     */
    EXIT_GROUP_RESULT_GROUP_FULL(3),
    /**
     * <pre>
     * 在黑名单中
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_IN_BACKLIST = 4;</code>
     */
    EXIT_GROUP_RESULT_IN_BACKLIST(4),
    /**
     * <pre>
     * 被踢
     * </pre>
     *
     * <code>EXIT_GROUP_RESULT_KICKED = 5;</code>
     */
    EXIT_GROUP_RESULT_KICKED(5),;

    public final int getNumber() {
        return value;
    }

    public static ExitGroupResult valueOf(int value) {
        return forNumber(value);
    }

    public static ExitGroupResult forNumber(int value) {
        switch (value) {
            case 0:
                return EXIT_GROUP_RESULT_UNKNOWN;
            case 1:
                return EXIT_GROUP_RESULT_OK;
            case 2:
                return EXIT_GROUP_RESULT_NOT_EXIST;
            case 3:
                return EXIT_GROUP_RESULT_GROUP_FULL;
            case 4:
                return EXIT_GROUP_RESULT_IN_BACKLIST;
            case 5:
                return EXIT_GROUP_RESULT_KICKED;
            default:
                return null;
        }
    }

    private final int value;

    private ExitGroupResult(int value) {
        this.value = value;
    }
}
