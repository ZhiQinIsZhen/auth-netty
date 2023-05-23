package com.lyz.auth.client.netty.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Set;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/25 22:53
 */
@EnableKnife4j
@EnableSwagger2WebMvc
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class NettyClientSwaggerConfig {

    private final OpenApiExtensionResolver openApiExtensionResolver;
    private final static Set<String> PROTOCOL = Sets.newHashSet("https", "http");

    public NettyClientSwaggerConfig(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Netty-接口文档")
                .description("一个关于netty搭建的socket项目")
                .termsOfServiceUrl("http://127.0.0.1:7070/")
                .contact(new Contact("lyz", "https://github.com/ZhiQinIsZhen/auth-netty", "liyangzhen0114@foxmail.com"))
                .version("1.0.0")
                .build();
    }

    @Bean
    public Docket nettyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .protocols(PROTOCOL)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lyz.auth.client.netty.controller"))
                .paths(PathSelectors.any())
                .build()
                .extensions(openApiExtensionResolver.buildSettingExtensions())
                .groupName("Netty客户端-API");
    }
}
