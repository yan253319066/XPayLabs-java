package com.yan.xpay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class MerchantGoogleCredentialRepository implements ICredentialRepository {

	private final MerchantMapper merchantMapper;

	@Override
	public String getSecretKey(String username) {
		Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, username));
		if(merchant == null) throw new ServiceException("Merchant name does not exist.");
		return merchant.getGoogleSecretkey();
	}

	@Override
	public void saveUserCredentials(String username, String secretKey, int validationCode, List<Integer> scratchCodes) {
		Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getName, username));
		if(merchant == null) throw new ServiceException("Merchant name does not exist.");
		merchant.setGoogleSecretkey(secretKey);
		merchantMapper.updateById(merchant);
	}
}
