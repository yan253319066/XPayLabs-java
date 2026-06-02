package com.yan.blockchain.pay.controller;

import cn.hutool.core.bean.BeanUtil;
import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.xpay.domain.bo.AssetTypeBo;
import com.yan.xpay.domain.vo.AssetTypeVo;
import com.yan.xpay.domain.SupportSymbol;
import com.yan.xpay.enums.BlockchainNetwork;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IAssetTypeService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.enums.Status;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Support Symbol
 */
@VerifySign
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/symbol")
public class SymbolController {

	private final IAssetTypeService assetTypeService;

	/**
	 * Get support symbol
	 * @param chain
	 * @param symbol
	 * @return
	 */
	@RateLimiter(count = 50, time = 10, limitType = LimitType.IP)
	@GetMapping("/supportSymbols")
	public R<List<SupportSymbol>> supportSymbols(@RequestParam(required = false) Chain chain,
		@RequestParam(required = false) String symbol){
		AssetTypeBo bo = new AssetTypeBo();
		bo.setChain(chain);
		bo.setSymbol(symbol);
		bo.setEnabled(Status.ENABLED);
		bo.setNetwork(BlockchainNetwork.MAIN);
		List<AssetTypeVo> assetTypeVoList = assetTypeService.queryList(bo);
		List<SupportSymbol> supportSymbols = new ArrayList<>();
		assetTypeVoList.forEach(assetTypeVo -> {
			SupportSymbol supportSymbol = new SupportSymbol();
			BeanUtil.copyProperties(assetTypeVo, supportSymbol);
			supportSymbols.add(supportSymbol);
		});
		return R.ok(supportSymbols);
	}
}
