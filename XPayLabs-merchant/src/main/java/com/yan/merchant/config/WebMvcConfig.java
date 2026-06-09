package com.yan.merchant.config;

import com.yan.merchant.interceptor.TwoFactorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TwoFactorInterceptor twoFactorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(twoFactorInterceptor)
            .addPathPatterns("/xpay/**")
            .excludePathPatterns(
                "/xpay/merchant/merchantInfo",
                "/xpay/merchant/bind2fa",
                "/xpay/merchant/verify2fa"
            );
    }
}
