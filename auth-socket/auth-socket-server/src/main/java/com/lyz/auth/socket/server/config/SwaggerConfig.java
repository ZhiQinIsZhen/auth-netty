package com.lyz.auth.socket.server.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.google.common.collect.Sets;
import com.lyz.auth.common.util.constant.CommonConstant;
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
 * Desc:swagger config
 *
 * @author lyz
 * @version 1.0.0
 * @date 2024/4/23 15:51
 */
@EnableKnife4j
@EnableSwagger2WebMvc
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    private final OpenApiExtensionResolver openApiExtensionResolver;
    private final static Set<String> PROTOCOL = Sets.newHashSet(CommonConstant.SCHEME_HTTPS, CommonConstant.SCHEME_HTTP);

    public SwaggerConfig(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(CommonConstant.DEFAULT_TITLE)
                .description(CommonConstant.DEFAULT_DESC)
                .termsOfServiceUrl("http://127.0.0.1:7070/")
                .contact(new Contact(CommonConstant.PROJECT_AUTHOR, CommonConstant.GITHUB_URL, CommonConstant.AUTHOR_EMAIL))
                .version(CommonConstant.DEFAULT_VERSION)
                .build();
    }

    @Bean
    public Docket nettyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .protocols(PROTOCOL)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lyz.auth.socket.server.controller"))
                .paths(PathSelectors.any())
                .build()
                .extensions(openApiExtensionResolver.buildSettingExtensions())
                .groupName("Netty服务端-API");
    }
}
