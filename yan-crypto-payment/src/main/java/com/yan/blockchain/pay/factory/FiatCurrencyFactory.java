package com.yan.blockchain.pay.factory;

import cn.hutool.extra.spring.SpringUtil;
import com.yan.blockchain.pay.service.FiatCurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FiatCurrencyFactory {
	public final static String defaultName = "jdpay";
	public FiatCurrencyService getService(){
		return SpringUtil.getBean(defaultName+"FiatCurrencyService", FiatCurrencyService.class);
	}
	public FiatCurrencyService getService(String payName){
		return SpringUtil.getBean(payName+"FiatCurrencyService", FiatCurrencyService.class);
	}
}
