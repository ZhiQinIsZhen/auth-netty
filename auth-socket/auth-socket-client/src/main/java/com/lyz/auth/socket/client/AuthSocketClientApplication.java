package com.lyz.auth.socket.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Desc:启动类
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 15:45
 */
@EnableScheduling
@SpringBootApplication
public class AuthSocketClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthSocketClientApplication.class, args);
    }
}