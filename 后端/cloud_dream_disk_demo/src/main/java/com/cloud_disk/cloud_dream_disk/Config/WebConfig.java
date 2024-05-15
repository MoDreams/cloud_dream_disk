package com.cloud_disk.cloud_dream_disk.Config;

import com.cloud_disk.cloud_dream_disk.interceptors.GlobalInterceptor;
import org.aopalliance.intercept.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private GlobalInterceptor globalInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalInterceptor).excludePathPatterns("/user/CaptCha",
                "/user/ECaptCha",
                "/user/register",
                "/user/login",
                "/user/CheckECaptCha",
                "/user/RestPassword",
                "/shardDownload",
                "/Video");
    }

}
