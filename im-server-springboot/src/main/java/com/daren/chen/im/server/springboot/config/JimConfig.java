package com.daren.chen.im.server.springboot.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.daren.chen.im.core.config.Config;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/12/2 10:38
 */
@Component
@ConfigurationProperties(prefix = "jim")
@EqualsAndHashCode(callSuper = true)
@Data
public class JimConfig extends Config implements Serializable {
    private static final long serialVersionUID = 6017605582640252788L;

}
