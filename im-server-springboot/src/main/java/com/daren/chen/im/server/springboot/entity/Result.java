package com.daren.chen.im.server.springboot.entity;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/10/19 16:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Result<T> implements Serializable {

    @ApiModelProperty("网关响应唯一编号")
    private String resultId;

    @ApiModelProperty(value = "网关返回码", required = true, example = "200", notes = "网关是否处理成功的标识200为成功")
    // @JSONField(serializeUsing = GatewayCodeSerialize.class, deserializeUsing = GatewayCodeSerialize.class)
    private int code;

    @ApiModelProperty(value = "网关返回码描述", required = true, example = "请求成功", notes = "网关返回码描述")
    private String msg;

    @ApiModelProperty(value = "业务返回码，参见具体的API接口文档", required = true, example = "SUCCESS")
    private String bizCode;

    @ApiModelProperty(value = "业务返回码描述，参见具体的API接口文档", required = true, example = "业务处理成功")
    private String bizMsg;

    @ApiModelProperty("业务返回数据")
    private T data;

    private Result() {}

    public static <T> Result<T> success(GatewayRequest request, T data, Long dataTotal) {
        return build(request, data, GatewayCode.SUCCESS, BizCode.SUCCESS.getCode(), BizCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success(GatewayRequest request) {
        return build(request, null, GatewayCode.SUCCESS, BizCode.SUCCESS.getCode(), BizCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success(GatewayRequest request, T data) {
        return build(request, data, GatewayCode.SUCCESS, BizCode.SUCCESS.getCode(), BizCode.SUCCESS.getMessage());
    }

    /**
     * 2019年7月12日14:21:36 添加 专门返回 try catch 的错误
     *
     * @param request
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> Result<T> error(GatewayRequest request, String msg) {
        String message =
            !StringUtils.isBlank(msg) ? BizCode.ERROR.getMessage() + "," + msg : BizCode.ERROR.getMessage();
        return build(request, null, GatewayCode.SUCCESS, BizCode.ERROR.getCode(), message);
    }

    public static <T> Result<T> error(GatewayRequest request, BizCodeEnum bizCodeEnum, String msg) {
        String message = !StringUtils.isBlank(msg) ? bizCodeEnum.getMessage() + "," + msg : bizCodeEnum.getMessage();
        return build(request, null, GatewayCode.SUCCESS, bizCodeEnum.getCode(), message);
    }

    public static <T> Result<T> error(GatewayRequest request, BizCodeEnum bizCodeEnum) {
        return build(request, null, GatewayCode.SUCCESS, bizCodeEnum.getCode(), bizCodeEnum.getMessage());
    }

    public static <T> Result<T> error(GatewayRequest request, T data, BizCodeEnum bizCodeEnum) {
        return build(request, data, GatewayCode.SUCCESS, bizCodeEnum.getCode(), bizCodeEnum.getMessage());
    }

    public static <T> Result<T> error(GatewayRequest request, T data, String bizCode, String bizMsg) {
        return build(request, data, GatewayCode.SUCCESS, bizCode, bizMsg);
    }

    public static <T> Result<T> error(GatewayRequest request, String bizCode, String bizMsg) {
        return build(request, null, GatewayCode.SUCCESS, bizCode, bizMsg);
    }

    public static <T> Result<T> getewayError(GatewayRequest request, BizCodeEnum bizCodeEnum, String msg) {
        String message = !StringUtils.isBlank(msg) ? bizCodeEnum.getMessage() + "," + msg : bizCodeEnum.getMessage();
        return build(request, null, GatewayCode.ERROR, bizCodeEnum.getCode(), message);
    }

    /**
     * 统一汇总返回
     *
     * @param request
     * @param data
     * @param code
     * @param bizCode
     * @param bizMsg
     * @param <T>
     * @return
     */
    private static <T> Result<T> build(GatewayRequest request, T data, GatewayCode code, String bizCode,
        String bizMsg) {
        Result<T> resultInfo = new Result<T>();
        resultInfo.data = data;
        resultInfo.code = code.getCode();
        resultInfo.msg = code.getMessage();
        resultInfo.bizMsg = bizMsg;
        resultInfo.bizCode = bizCode;
        return resultInfo;
    }

    @JSONField(serialize = false)
    @JsonIgnore
    public boolean isSuccess() {
        return this.code == Integer.parseInt(BizCode.SUCCESS.getCode())
            && BizCode.SUCCESS.getCode().equals(this.bizCode);
    }
}
