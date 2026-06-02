package com.yan.login.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.yan.user.domain.bo.UserBo;
import com.yan.user.domain.vo.UserVo;
import com.yan.user.enums.GoogleStatus;
import com.yan.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {
	private final GoogleAuthenticator googleAuthenticator;
	private final UserGoogleCredentialRepository userGoogleCredentialRepository;
	private final IUserService userService;

	@Value("${spring.application.name}")
	private String appName;

	public String generateSecretKey(String username) {
		GoogleAuthenticatorKey key = googleAuthenticator.createCredentials(username);
		return key.getKey();  // 返回Secret Key（如"JBSWY3DPEHPK3PXP"）
	}

	public String getQRCodeUrl(String username, String secretKey) {
		String issuer = appName; // 自定义发行方名称
		return String.format(
			"otpauth://totp/%s:%s?secret=%s&issuer=%s",
			issuer, username, secretKey, issuer
		);
		// 示例URL：otpauth://totp/MyApp:user1?secret=JBSWY3DPEHPK3PXP&issuer=MyApp
	}

	public boolean verifyCode(String username, int code) {
		String secretKey = userGoogleCredentialRepository.getSecretKey(username);
		boolean b = googleAuthenticator.authorize(secretKey, code);
		if(b) {
			UserVo user = userService.getUser(username);
			UserBo bo = new UserBo();
			bo.setUserId(user.getUserId());
			bo.setGoogleStatus(GoogleStatus.BOUND);
			userService.updateByBo(bo);
		}
		return b;
	}
}
