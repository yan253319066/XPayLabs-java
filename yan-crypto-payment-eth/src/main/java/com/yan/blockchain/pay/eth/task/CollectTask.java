package com.yan.blockchain.pay.eth.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.UserAddressCollectible;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.service.CollectService;
import com.yan.xpay.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectTask {
	private final UserAddressMapper userAddressMapper;
	private final CollectService collectService;
	private final Map<Chain, EthConfig.ChainConfig> chainConfigs;

	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		List<UserAddress> userAddresses = userAddressMapper.selectList(
			new LambdaQueryWrapper<UserAddress>().in(UserAddress::getChain, chainConfigs.keySet()).in(UserAddress::getCollectible, List.of(UserAddressCollectible.YES, UserAddressCollectible.SENT_TXFEE)));

		userAddresses.forEach(userAddress -> {
			try {
				collectService.collect(userAddress);
			}catch (Exception e){
				log.error(" 归集失败: userAddress {}", userAddress.getAddress(),  e);
			}
		});
	}
}
