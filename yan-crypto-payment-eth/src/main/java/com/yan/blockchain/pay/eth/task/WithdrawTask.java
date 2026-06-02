package com.yan.blockchain.pay.eth.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.service.WithdrawService;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawTask {
	private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;
	private final Map<Chain, EthConfig.ChainConfig> chainConfigs;
	private final WithdrawService withdrawService;
	@Scheduled(fixedDelay = 5000, initialDelay = 2000)
	public void withdraw() {
		List<MerchantRechargeWithdraw>
			withdrawList = merchantRechargeWithdrawMapper.selectList(new LambdaQueryWrapper<MerchantRechargeWithdraw>().eq(MerchantRechargeWithdraw::getType, RechargeWithdraw.WITHDRAW).eq(
			MerchantRechargeWithdraw::getStatus, RechargeWithdrawStatus.APPROVED).in(MerchantRechargeWithdraw::getChain, chainConfigs.keySet()));
		withdrawList.forEach(withdraw->{
			try {
				withdrawService.withdraw(withdraw);
			} catch (Exception e) {
				log.info("提币出错 ", e);
			}
		});
	}
}
