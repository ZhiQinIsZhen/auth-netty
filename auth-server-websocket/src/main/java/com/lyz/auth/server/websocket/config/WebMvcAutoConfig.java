package com.lyz.auth.server.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Desc:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/5/23 16:51
 */
@Configuration
public class WebMvcAutoConfig extends WebMvcConfigurationSupport {

    /**
     * 允许加载本地静态资源
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/");
        super.addResourceHandlers(registry);
    }
}
