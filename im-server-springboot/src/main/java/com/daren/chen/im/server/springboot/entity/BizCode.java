package com.daren.chen.im.server.springboot.entity;

/**
 * @program: micro-merger-parent
 * @description:
 * @author: djj
 * @create: 2019-06-28 11:53
 **/

import lombok.Getter;

/**
 * 日志级别定义
 *
 * @author chendaren
 */
@Getter
public enum BizCode implements BizCodeEnum {

    /**
     *
     */
    SUCCESS("200", "业务处理成功"),
    /**
     *
     */
    SUCCESS_NONE("200", "查询无结果"),
    /**
     *
     */
    SERVICE_TIMEOUT("504", "处理超时"),
    /**
     *
     */
    PARAM_ERROR("201000", "参数错误"),
    /**
     *
     */
    SAVE_ERROR("201001", "保存失败"),
    /**
     *
     */
    UPDATE_ERROR("201002", "修改失败"),
    /**
     *
     */
    DEL_ERROR("201003", "删除失败"),
    /**
     *
     */
    LIST_ERROR("201004", "列表查询失败"),
    /**
     *
     */
    QUERY_ERROR("201005", "查询失败"),

    /*1011*/
    NOT_USER("1011001", "用户不存在"), USER_FREEZE("1011002", "用户被冻结"), USER_DELETE("1011003", "用户被删除"),
    USER_PASSWORD_ERROR("1011004", "用户密码错误"), USER_PHONE_CODE_ERROR("1011005", "手机验证码错误"),
    USER_PHONE_CODE_EXPIRE("1011006", "手机验证码过期"), NOT_PHONE("1011007", "手机号码不存在"),
    OLD_PASSWORD_ERROR("1011008", "旧密码错误"),

    /*1012*/
    NOT_ROLE("1012001", "角色不存在"),

    /*1013*/
    NOT_PERMISSION("1013001", "菜单不存在"), NOT_PERMISSION_DATA_RULE("1013002", "菜单数据权限不存在"),
    NOt_METHOD_AUTHORITY("1013003", "没有权限,请联系管理员授权"),

    /*1014*/
    NOT_DEPART("1014001", "部门不存在"),

    /*1017*/
    VERIFICATION_CODE_SEND_ERROR("1017001", "验证码发送失败"), VERIFICATION_CODE_REPEAT_SEND("1017002", "验证码重复发送"),
    NO_PERMISSION_TO_LOG_IN_TO_THE_PLATFORM("1017003", "您没有权限登录该平台"),

    /*1018*/
    TOKEN_ERROR("1018001", "token认证失败"), TOKEN_EXPIRE("1018002", "token过期"),
    TOKEN_GENERATION_FAILURE("1018003", "token生成失败"),

    /*2016*/
    VEHICLE_NOT_ENTERING("2016001", "车辆未入场,无法出场"), NOT_PLANGUARDVISITING("2016002", "门岗来访登记记录不存在"),
    NOT_PLANORDERGOODS("2016003", "计划订单货物不存在"), NOT_PLANGUARDRECORD("2016004", "门岗计划记录不存在"),

    /*201204*/
    NON_PENDING_APPROVALS("2012041", "存在非待审核状态计划,无法进行审核"), UNIQUE_CATEGORY_NAME("205906", "类别已存在"),

    ACCOUNT_MENOY_LACK("501", "账户余额不足"), ATYPISM("502", "不一致"), ERROR("503", "业务处理失败"), IP_REFUSE("505", "ip被拒绝"),
    INIT("506", "初始化"), LACK_CONFIG("507", "缺少配置信息,调用者传唯一码,开放平台未找到对应信息"), NOT_APPKEY("508", "appkey不存在"),
    NOT_DATA("509", "没有更新数据"), NOT_LIST("510", "没有数据"), SIGN_ERROR("512", "验签异常"), TIMEOUT("513", "业务处理超时"),
    NOT_SAVE("515", "处理失败,请重新发送"), NOT_PARAMETER("520", "缺少参数信息"), EXCEED_QUOTA("521", "调用服务超出配额"),
    NOT_RESULT("522", "接口供应商无返回结果"), WEB_STOP("523", "接口供应商网站错误"), ENCRYPT_ABNORMAL("524", "json加密异常"),
    SERVICE_EXCEPTION("530", "处理异常"),

    SERVICE_NULL("524", "服务不存在"), UN_SETTING("525", "配置不可用"), UNOPENED_SERVICES("530", "未开通此服务"),
    KEY_VALIDATION_ERROR("540", "密钥验证失败"), IP_AUTHENTICATION_ERROR("545", "IP验证失败"), UNDER_REVIEW("550", "正在审核中"),
    MULTIPLE_SETTING("560", "存在多条配置,请传入配置"), SETTING_ERROR_KEY("565", "配置参数错误"), SERVICE_NOT_USE("566", "服务不可使用"),
    SERVICE_NOT_PUBLIC_SETTING("568", "服务没有绑定公用配置"), SERVICE_OWN_SETTING("569", "服务必须使用自身配置"),
    SERVICE_PUBLIC_SETTING("570", "服务必须使用公用配置"), NULL_FREE("571", "该服务未指定价格"),

    // 结束

    // 交科信用
    VERIFY_ERROR("302", "data=3的错误,具体查看返回的msg"), VERIFY_NOTDATA("303", "查询成功,查无结果"),
    VERIFY_ON_FILE("306", "车辆已存在备案记录,无需再次备案"),
    // 身份证认证
    VERIFY_NOTCARD("304", "无此身份证号码"), VERIFY_MAINTAIN("305", "身份证中心维护中"),
    // 核验、认证功能 结束
    ;

    private final String message;
    private final String code;

    BizCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
