package com.yan.merchant.help;

import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.service.IMerchantService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MerchantHelp {

	private final IMerchantService merchantService;

	public MerchantVo getMerchant(){
		String username = LoginHelper.getUsername();
		MerchantVo merchantVo = merchantService.getMerchantByName(username);
		if(merchantVo == null) throw new ServiceException("Merchant does not exist.");
		return merchantVo;
	}

	public Long getMerchantId(){
		return getMerchant().getId();
	}
}
