package com.daren.chen.im.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.daren.chen.im.server.springboot.utils.ApplicationContextProvider;

/**
 * @author chendaren
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        //
        curEnvironmentOut();
    }

    private static void curEnvironmentOut() {
        System.out.println("****************************************************************************");
        Environment env = ApplicationContextProvider.getBean(Environment.class);
        String profile = env.getProperty("spring.profiles.active");
        System.out.println("当前启动环境:" + profile);
        System.out.println("****************************************************************************");
    }

}
