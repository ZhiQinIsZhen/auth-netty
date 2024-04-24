package com.lyz.auth.common.codec.config;

import com.lyz.auth.common.codec.handler.req.PingReqTypeServiceImpl;
import com.lyz.auth.common.codec.handler.req.PongReqTypeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc:auto config
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 15:04
 */
@Configuration
public class AuthNettyCodecAutoConfig {

    @Bean
    public PingReqTypeServiceImpl pingReqTypeService() {
        return new PingReqTypeServiceImpl();
    }

    @Bean
    public PongReqTypeServiceImpl pongReqTypeService() {
        return new PongReqTypeServiceImpl();
    }
}
