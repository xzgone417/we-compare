package com.tencent.timi.annualparty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://timish.woa.com/", "http://timish.woa.com/")
                .allowedMethods("GET", "POST")
                        .allowedHeaders("*")
                        .allowCredentials(true);
    }
}