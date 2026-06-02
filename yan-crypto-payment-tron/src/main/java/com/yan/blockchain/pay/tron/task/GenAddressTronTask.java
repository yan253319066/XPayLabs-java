package com.yan.blockchain.pay.tron.task;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.domain.CryptoAccount;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.AddressPoolMapper;
import com.yan.blockchain.pay.tron.service.TronService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenAddressTronTask {

	private final TronService tronService;
	private final AddressPoolMapper addressPoolMapper;
	private final TronConfig tronConfig;
	private final SystemAddressCache systemAddressCache;

	@Scheduled(fixedDelay = 1000 * 60 * 30)
	public void run(){
		tronConfig.getNetworks().forEach((chain -> {
			Long count = addressPoolMapper.selectCount(new LambdaQueryWrapper<AddressPool>().eq(AddressPool::getChain, chain).eq(AddressPool::getUsed, AddressStatus.UNUSED));
			if( count != null && count >= 100) return;
			for (int i = 0; i < 100; i++) {
				createTronAccount(chain);
			}
		}));
	}

	@Async
	public void createTronAccount(Chain chain) {
		String pwd = IdUtil.simpleUUID();
		CryptoAccount tronAccount = tronService.generateTronAccount();
		AddressPool addressPool = new AddressPool();
		addressPool.setAddress(tronAccount.getAddress());
		byte[] key = SecureUtil.decode(pwd);
		AES aes = SecureUtil.aes(key);
		addressPool.setKeystore(aes.encryptHex(tronAccount.getPrivateKey()));
		addressPool.setEncrypt(pwd);
		addressPool.setChain(chain);
		addressPool.setUsed(AddressStatus.UNUSED);
		addressPool.setType(AddressType.GENERAL);
		addressPoolMapper.insert(addressPool);
		systemAddressCache.setAddress(chain, addressPool.getAddress());
	}
}
