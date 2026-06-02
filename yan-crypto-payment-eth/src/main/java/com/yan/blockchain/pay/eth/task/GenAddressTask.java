package com.yan.blockchain.pay.eth.task;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.xpay.mapper.AddressPoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.crypto.*;
import org.web3j.crypto.exception.CipherException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenAddressTask {

	private final AddressPoolMapper addressPoolMapper;
	private final Map<Chain, EthConfig.ChainConfig> chainConfigs;
	private final SystemAddressCache systemAddressCache;

	@Scheduled(fixedDelay = 1000 * 60 * 30)
	public void run(){
		chainConfigs.keySet().forEach(chain -> {
			Long count = addressPoolMapper.selectCount(new LambdaQueryWrapper<AddressPool>().eq(AddressPool::getChain, chain).eq(AddressPool::getUsed, AddressStatus.UNUSED));
			if( count != null && count >= 100) return;
			for (int i = 0; i < 100; i++) {
				createTronAccount(chain);
			}
		});
	}

	@Async
	public void createTronAccount(Chain chain) {
		try {
			String pwd = IdUtil.simpleUUID();

			ECKeyPair keyPair = Keys.createEcKeyPair();
			WalletFile walletFile = Wallet.createStandard(pwd,  keyPair);
			String address = "0x" + walletFile.getAddress();

			AddressPool addressPool = new AddressPool();
			addressPool.setAddress(address);
			byte[] key = SecureUtil.decode(pwd);
			AES aes = SecureUtil.aes(key);
			addressPool.setKeystore(aes.encryptHex(JSONUtil.toJsonStr(walletFile)));
			addressPool.setEncrypt(pwd);
			addressPool.setChain(chain);
			addressPool.setUsed(AddressStatus.UNUSED);
			addressPool.setType(AddressType.GENERAL);
			addressPoolMapper.insert(addressPool);
			systemAddressCache.setAddress(chain, addressPool.getAddress());
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (CipherException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {
		String pwd = IdUtil.simpleUUID();

		ECKeyPair keyPair = Keys.createEcKeyPair();
		WalletFile walletFile = Wallet.createStandard(pwd,  keyPair);
		String address = "0x" + walletFile.getAddress();

		System.out.println("Private  Key: " + keyPair.getPrivateKey().toString(16));
		System.out.println("Public  Key: " + keyPair.getPublicKey().toString(16));
		System.out.println("Address:  " + address);
		System.out.println("walletFile:  " + JSONUtil.toJsonStr(walletFile));

//		WalletFile walletFile1 = JSONUtil.toBean(JSONUtil.toJsonStr(walletFile), WalletFile.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			WalletFile walletFile1 = mapper.readValue(JSONUtil.toJsonStr(walletFile),  WalletFile.class);
			System.out.println(walletFile1.getAddress());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

//		ECKeyPair keyPair1 = Wallet.decrypt(pwd,  walletFile);
//		System.out.println("Private  Key: " + keyPair1.getPrivateKey().toString(16));
//		System.out.println("Public  Key: " + keyPair1.getPublicKey().toString(16));

	}
}
