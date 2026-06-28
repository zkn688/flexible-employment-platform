package com.example.employment.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
@EnableKnife4j
public class Knife4jConfig {

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端接口")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.employment.controller.user"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(new ApiKey("Authorization", "Authorization", "header")))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("灵活就业服务平台用户端接口文档")
                .description("用户端后端接口，包括登录注册、验证码、岗位、简历、政策、社保、文件上传和 AI 求职辅助等功能。")
                .version("1.0.0")
                .contact(new Contact("用户端开发", "", ""))
                .build();
    }

    private SecurityContext securityContext() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        SecurityReference securityReference = new SecurityReference("Authorization",
                new AuthorizationScope[]{authorizationScope});
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(securityReference))
                .operationSelector(operationContext -> true)
                .build();
    }
}
