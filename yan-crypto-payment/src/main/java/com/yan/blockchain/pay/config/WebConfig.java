package com.yan.blockchain.pay.config;

import com.yan.blockchain.pay.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/**")  // 拦截所有请求
//			.excludePathPatterns("/**/register")  // 排除注册接口
			.excludePathPatterns("/v1/order/notify/**")  // 排除订单通知接口及其子路径
			.excludePathPatterns("/v1/order/pay") // 排除支付接口
			.excludePathPatterns("/v1/order/getOrderStatus") // 排除查询订单状态接口
			.excludePathPatterns("/favicon.ico")
			.excludePathPatterns("/")
			.excludePathPatterns("/**/webhook/**")
			.excludePathPatterns("/**/fiatCurrencyWebhook/**")
			.excludePathPatterns("/v3/api-docs");

	}
}

