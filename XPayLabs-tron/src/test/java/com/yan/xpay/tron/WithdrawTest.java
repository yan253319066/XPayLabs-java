package com.yan.xpay.tron;

import com.yan.blockchain.pay.tron.service.TronService;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.SecureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tron.trident.core.ApiWrapper;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class WithdrawTest {
	@Autowired
	TronService tronService;

	@Test
	public void withdrawTest() {
		String keystore = "";
		String encrypt = "";
		String contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
		String privateKey = SecureUtils.decodePrivateKey(keystore, encrypt);
		ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey);
		String toAddress = "";
		//转TRX
//		String txid = tronService.sendTrx(wrapper, toAddress, AmountUtils.toAmount(new BigDecimal("190.8"), 6).longValue());
//		log.info("txid {}", txid);
		//转USDT
		String txid = tronService.sendTrc20(wrapper, contractAddress, toAddress, AmountUtils.toAmount(new BigDecimal("6"), 6).longValue());
		log.info("txid {}", txid);
	}
}
