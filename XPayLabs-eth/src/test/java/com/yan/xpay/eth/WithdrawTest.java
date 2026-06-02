package com.yan.xpay.eth;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.SecureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.exception.CipherException;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
public class WithdrawTest {
	@Autowired
	EthService ethService;

	@Test
	public void withdrawTest() {
		String _keystore = "";
		String encrypt = "";
		String toAddress = "";

		byte[] key = SecureUtil.decode(encrypt);
		AES aes = SecureUtil.aes(key);
		String keystore = aes.decryptStr(_keystore);

		Credentials credentials;
		try {
			ObjectMapper mapper = new ObjectMapper();
			WalletFile walletFile = mapper.readValue(keystore,  WalletFile.class);
			credentials = Credentials.create(Wallet.decrypt(encrypt, walletFile));
		} catch (CipherException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String txid = ethService.sendEth(Chain.ETH, credentials, toAddress, AmountUtils.toAmount(new BigDecimal("0.091"), 18));
		log.info("txid {}", txid);
	}

}
