package com.yan.xpay.sui.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.service.SuiWithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawTronTask {
	private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;
	private final SuiWithdrawService suiWithdrawService;
	private final SuiConfig suiConfig;

	@Scheduled(fixedDelay = 5000, initialDelay = 2000)
	public void withdraw() {
		List<MerchantRechargeWithdraw> withdrawList = merchantRechargeWithdrawMapper.selectList(new LambdaQueryWrapper<MerchantRechargeWithdraw>().eq(MerchantRechargeWithdraw::getType, RechargeWithdraw.WITHDRAW).eq(
			MerchantRechargeWithdraw::getStatus, RechargeWithdrawStatus.APPROVED).in(MerchantRechargeWithdraw::getChain, suiConfig.getNetworks()));
		withdrawList.forEach(withdraw->{
			try {
				suiWithdrawService.withdraw(withdraw);
			} catch (Exception e) {
				log.info("提币出错 ", e);
			}
		});
	}
}
