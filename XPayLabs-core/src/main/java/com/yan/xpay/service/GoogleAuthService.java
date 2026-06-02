package com.yan.xpay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.enums.GoogleStatus;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.service.impl.MerchantGoogleCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
	private final GoogleAuthenticator googleAuthenticator;
	private final MerchantGoogleCredentialRepository merchantGoogleCredentialRepository;
	private final MerchantMapper merchantMapper;

	public String generateSecretKey(String username) {
		GoogleAuthenticatorKey key = googleAuthenticator.createCredentials(username);
		return key.getKey();  // 返回Secret Key（如"JBSWY3DPEHPK3PXP"）
	}

	public String getQRCodeUrl(String username, String secretKey) {
		String issuer = "XPayLabs"; // 自定义发行方名称
		return String.format(
			"otpauth://totp/%s:%s?secret=%s&issuer=%s",
			issuer, username, secretKey, issuer
		);
		// 示例URL：otpauth://totp/MyApp:user1?secret=JBSWY3DPEHPK3PXP&issuer=MyApp
	}

	public boolean verifyCode(String username, int code) {
		String secretKey = merchantGoogleCredentialRepository.getSecretKey(username);
		boolean b = googleAuthenticator.authorize(secretKey, code);
		if(b) {
			Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, username));
			merchant.setGoogleStatus(GoogleStatus.BOUND);
			merchantMapper.updateById(merchant);
		}
		return b;
	}
}
