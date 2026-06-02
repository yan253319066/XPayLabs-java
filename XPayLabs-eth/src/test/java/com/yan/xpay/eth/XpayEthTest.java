package com.yan.xpay.eth;

import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.enums.Chain;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class XpayEthTest {
	@Autowired
	EthService ethService;

	@Test
	public void test() throws IOException {
		BigDecimal gas = ethService.calculateGas(Chain.ETH, "0xdAC17F958D2ee523a2206206994597C13D831ec7");
		log.info("{}", gas);
	}
}
