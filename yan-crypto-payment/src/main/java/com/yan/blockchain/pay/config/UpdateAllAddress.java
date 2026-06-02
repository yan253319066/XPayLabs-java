package com.yan.blockchain.pay.config;

import com.yan.blockchain.pay.service.InitMerchantAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 初始项目里的所有地址
 */
@Slf4j
@Component
public class UpdateAllAddress {

	public UpdateAllAddress(InitMerchantAddressService initMerchantAddressService) {
		initMerchantAddressService.modifyAllAddress();
	}

}
