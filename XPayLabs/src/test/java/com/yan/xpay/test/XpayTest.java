package com.yan.xpay.test;

import com.yan.xpay.domain.bo.MerchantBo;
import com.yan.xpay.enums.*;
import com.yan.xpay.service.IMerchantService;
import com.yan.xpay.utils.FeeUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class XpayTest {

	@Autowired
	IMerchantService merchantService;

	@Test
	public void getTronPlatformFee() {
		BigDecimal fee = FeeUtils.getTronPlatformFee(false, Chain.TRON_TEST, "USDT", new BigDecimal("3.01"), new BigDecimal("0.5"), "TZ9U6vrKRkvFMDZN5f5Cp49FPRpnxrVc7Z");
		log.info("fee {}", fee);
		BigDecimal fee2 = FeeUtils.getPlatformFee(false, Chain.BSC_TEST, "USDT", new BigDecimal("3.01"), new BigDecimal("0.5"));
		log.info("fee2 {}", fee2);
	}

//	@Test
	public void MerchantSecret(){
		MerchantBo bo = new MerchantBo();
		bo.setName("upay_india");
		bo.setCallbackUrl("https://www.baidu.com");
		bo.setFeeRatio(BigDecimal.ZERO);
		bo.setWithdrawalType(WithdrawalType.AUTO);
		bo.setIntoType(IntoType.COLD);
		bo.setEnableWhitelistIp(EnableWhitelistIp.DISABLED);
		bo.setMerchantSysVersion(MerchantSysVersion.V2);
		bo.setEnergyApikey("租能量apikey");
		merchantService.registerMerchant(bo);
	}
}
