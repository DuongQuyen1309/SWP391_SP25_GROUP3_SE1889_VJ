package com.demoproject.configuration;

import com.demoproject.interceptor.UserProfileInterceptor;
import com.demoproject.service.AccountService;
import com.demoproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public WebConfig(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserProfileInterceptor(userService, accountService))
                .addPathPatterns("/product/**","/account/**","/customer/**","/warehouse/**","/home","/warehouse","/zone/**","/user/changepw")
                .excludePathPatterns("/user/userprofile"); // Không áp dụng cho user profile
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/");
    }
}