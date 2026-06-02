package com.yan.blockchain.pay.service;

import com.yan.blockchain.pay.req.*;
import com.yan.blockchain.pay.vo.FiatCurrencyInResult;
import com.yan.blockchain.pay.vo.FiatCurrencyQueryResult;
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.domain.vo.MerchantVo;

public interface FiatCurrencyService {
	String fiatCurrencyIn(FiatCurrencyInReq req, MerchantVo merchant);
	String fiatCurrencyOut(FiatCurrencyOutReq req, MerchantVo merchant);

	FiatCurrencyInResult fiatCurrencyInV2(FiatcurrencyOrderBo bo);
	boolean fiatCurrencyOutV2(FiatcurrencyOrderBo bo);

	String fiatCurrencyQueryIn(FiatCurrencyQueryInReq req);
	String fiatCurrencyQueryOut(FiatCurrencyQueryOutReq req);

	FiatCurrencyQueryResult fiatCurrencyQueryInV2(String orderNo);
	FiatCurrencyQueryResult fiatCurrencyQueryOutV2(String orderNo);

	String fiatCurrencyNotifyIn(NotifyInReq req);
	String fiatCurrencyNotifyOut(NotifyOutReq req);

	String fiatCurrencyBalance();
}
