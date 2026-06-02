package com.yan.xpay.sui.task;

import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.constant.RedisConstant;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.service.SuiService;
import com.yan.xpay.sui.utils.SuiUtils;
import com.yan.xpay.utils.AmountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuiGasTask {
	private final SuiConfig suiConfig;
	private final AssetTypeCache assetTypeCache;

	@Scheduled(fixedDelay = 1000 * 60 * 5, initialDelay = 2000)
	public void run(){
		suiConfig.getNetworks().forEach((chain)-> {
			try {
				AssetType assetType = assetTypeCache.getBySymbol(chain,"SUI");
                if(assetType == null){
                    log.error("AssetType not found");
                    return;
                }
				BigDecimal gas;
				if(chain.name().contains("TEST")) {
					gas = SuiService.estimateTokenTransferGasFee(assetType.getHotAddress(),
						"0x8710b4f227c791b77b6787c456a7e028a7a1ae659d2d853537a73a699f0ed3e3", BigInteger.valueOf(1000),
						"0x07cdd3c48995e898f6f36c294a086cda0c92a9cb7a8b4ee0b0ebce69a48d59bf::simple_token::SIMPLE_TOKEN",
						SuiUtils.getNetwork(chain));
				}else {
					String sender = assetType.getHotAddress();
					String coinType = "0xdba34672e30cb065b1f93e3ab55318768fd6fef66c15942c9f7cb846e2f900e7::usdc::USDC";
					BigDecimal balance = SuiService.getBalance(sender, coinType, SuiUtils.getNetwork(chain));
					if(balance.compareTo(BigDecimal.ZERO) <= 0) sender = "0xac5bceec1b789ff840d7d4e6ce4ce61c90d190a7f8c4f4ddf0bff6ee2413c33c";
					gas = SuiService.estimateTokenTransferGasFee(sender,
						"0x8710b4f227c791b77b6787c456a7e028a7a1ae659d2d853537a73a699f0ed3e3", BigInteger.valueOf(1000),
						coinType,
						SuiUtils.getNetwork(chain));
				}
				if(gas.compareTo(BigDecimal.ZERO) <= 0) throw new Exception("gas获取失败");
				RedisUtils.setCacheMapValue(RedisConstant.GAS_KEY, chain.name(), AmountUtils.fromAmount(getGas(gas, new BigDecimal("20")), 9));
			} catch (Exception e) {
				log.error("[{}] gas获取失败 {}", chain, e.getMessage());
			}
		});
	}

	private BigDecimal getGas(BigDecimal original, BigDecimal percentage) {
		// 计算逻辑：100 × (1 + 20/100) = 100 × 1.2
		BigDecimal result = original.multiply(
			BigDecimal.ONE.add(percentage.divide(new BigDecimal("100"), 6, RoundingMode.DOWN))
		);
		return result;
	}
}
