package com.daren.chen.im.server.springboot.entity;

import java.io.Serializable;

import com.daren.chen.im.core.utils.Md5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用请求网关的公共请求参数 参考支付宝的开放API设计，具体的业务接口的请求参数都要继承这个类，加上业务自身的请求参数即可
 *
 * @author chendaren
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayRequest<T> implements Serializable {

    private String requestId;

    @ApiModelProperty(value = "应用编号", required = true)
    private String appId;

    @ApiModelProperty(value = "应用密钥", required = true)
    private String appKey;

    @ApiModelProperty(value = "数据签名", required = true)
    private String sign;

    @ApiModelProperty(value = "用户令牌", required = false)
    private String token;

    @ApiModelProperty(value = "服务编号", required = true)
    private String method;

    @ApiModelProperty(value = "请求时间", required = true)
    private Long timestamp;

    @ApiModelProperty("配置编号")
    private String appSettingId;

    @ApiModelProperty("是否调试模式")
    private Boolean debug;

    @ApiModelProperty("业务数据")
    private T content;

    public String createSign() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getAppId()).append(this.getAppKey()).append(this.getMethod()).append(this.getToken())
            .append(this.getTimestamp().toString());
        return Md5.getMD5(sb.toString());
    }

    public boolean validateSign(String sign) {
        String nsign = this.createSign();
        return sign.equals(nsign);
    }

    public GatewayRequest() {

    }

    public GatewayRequest(GatewayRequest<?> req) {
        this.requestId = req.getRequestId();
        this.appId = req.getAppId();
        this.appKey = req.getAppKey();
        this.sign = req.getSign();
        this.token = req.getToken();
        this.timestamp = req.getTimestamp();
        this.appSettingId = req.appSettingId;
        this.debug = req.getDebug();
    }

}
