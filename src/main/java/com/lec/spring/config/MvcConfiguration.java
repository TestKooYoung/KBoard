package com.lec.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration {


    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }


    @Configuration
    public static class LocalMvcConfiguration implements WebMvcConfigurer{

        @Value("${app.upload.path}")
        private String uploadDir;
        //  파일 업로드
        // resource 경로 추가

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            System.out.println("LocalMvcConfiguration.addResourceHandlers() 호출");

            registry
                    .addResourceHandler("/upload/**")
                    .addResourceLocations("file:" + uploadDir + "/");

            //  /upload/** URL 로 request 가 들어오면
            // upload/ 경로의 resource 가 동작케 함.
            // IntelliJ 의 경우 이 경로를 module 이 아닌 project 이하에 생성해야 한다.

        }

    }
}
