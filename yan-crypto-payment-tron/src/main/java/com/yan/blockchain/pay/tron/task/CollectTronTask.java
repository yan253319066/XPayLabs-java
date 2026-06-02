package com.yan.blockchain.pay.tron.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.mapper.UserAddressMapper;
import com.yan.blockchain.pay.tron.service.CollectTronService;
import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.enums.UserAddressCollectible;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectTronTask {
	private final UserAddressMapper userAddressMapper;
	private final CollectTronService collectTronService;
	private final TronConfig tronConfig;

	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		List<UserAddress> userAddresses = userAddressMapper.selectList(
			new LambdaQueryWrapper<UserAddress>().in(UserAddress::getChain, tronConfig.getNetworks()).in(UserAddress::getCollectible, List.of(UserAddressCollectible.YES, UserAddressCollectible.SENT_TXFEE)));

		userAddresses.forEach(userAddress -> {
			try {
				collectTronService.collect(userAddress);
			}catch (Exception e){
				log.error(" 归集失败: userAddress {}", userAddress.getAddress(),  e);
			}
		});
	}


}

