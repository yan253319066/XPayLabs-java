package com.yan.xpay.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.yan.xpay.service.impl.MerchantGoogleCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleAuthConfig {

	@Autowired
	MerchantGoogleCredentialRepository merchantGoogleCredentialRepository;

	@Bean
	public GoogleAuthenticator googleAuthenticator() {
		GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
			.setTimeStepSizeInMillis(30_000)  // 验证码有效期30秒
			.setWindowSize(1)                 // 允许时间窗口偏移（解决时钟不同步问题）
			.build();

		GoogleAuthenticator authenticator = new GoogleAuthenticator(config);
		authenticator.setCredentialRepository(merchantGoogleCredentialRepository);  // 绑定存储类
		return authenticator;
	}
}
