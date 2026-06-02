package com.yan.blockchain.pay.service;

import cn.hutool.core.util.ObjectUtil;
import com.yan.blockchain.pay.factory.FiatCurrencyFactory;
import com.yan.blockchain.pay.vo.FiatCurrencyQueryResult;
import com.yan.xpay.domain.FiatcurrencyOrder;
import com.yan.xpay.domain.SimpleTransfer;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.enums.OrderType;
import com.yan.xpay.service.IFiatcurrencyOrderService;
import com.yan.xpay.service.IMerchantAssetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FiatCurrencyStatusService {

	private final IFiatcurrencyOrderService fiatcurrencyOrderService;
	private final FiatCurrencyFactory fiatCurrencyFactory;
	private final IMerchantAssetsService merchantAssetsService;

	/**
	 * 根据第三方法币查询结果修改数据
	 * @param order
	 */
	@Transactional
	public boolean updateFiatCurrencyStatus(FiatcurrencyOrderVo order) {
		if(OrderType.COLLECTION == order.getOrderType()) {
			FiatCurrencyQueryResult result = fiatCurrencyFactory.getService(order.getChannelCode()).fiatCurrencyQueryInV2(order.getOrderNo());
			if(ObjectUtil.isNotNull(result)) {
				switch (result.getStatus()) {
					case "SUCCESS" :
						if("SUCCESS".equals(order.getStatus().name())) return false;
						SimpleTransfer transfer = new SimpleTransfer();
						transfer.setSymbol(order.getCurrency());
						transfer.setRate(order.getHandingRate());
						transfer.setFee(order.getHandingFee());
						transfer.setRemark("法币代收");
						transfer.setType(AssetOperType.FIAT_CURRENCY_PAYIN);
						transfer.setMerchantId(order.getMerchantId());
						transfer.setTransactionNo(order.getOrderNo());
						transfer.setAmount(order.getAmount());
						merchantAssetsService.transfer(transfer);
						order.setStatus(FiatcurrencyOrderStatus.SUCCESS);
						order.setActualAmount(new BigDecimal(result.getActualAmount()));
						order.setCallbackContent(result.getOriginal());
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						return true;
					case "WAIT":
						if("WAIT".equals(order.getStatus().name())) return false;
						order.setStatus(FiatcurrencyOrderStatus.WAIT);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						break;
					case "PADDING":
						if("PADDING".equals(order.getStatus().name())) return false;
						order.setStatus(FiatcurrencyOrderStatus.PADDING);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						break;
					case "FAIL":
						if("FAIL".equals(order.getStatus().name())) return false;
						order.setStatus(FiatcurrencyOrderStatus.FAIL);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						return true;
				}
			}
		}else if(OrderType.PAYOUT == order.getOrderType()) {
			FiatCurrencyQueryResult result = fiatCurrencyFactory.getService(order.getChannelCode()).fiatCurrencyQueryOutV2(order.getOrderNo());
			if(ObjectUtil.isNotNull(result)) {
				switch (result.getStatus()) {
					case "SUCCESS" :
						if("SUCCESS".equals(order.getStatus().name())) return false;
						SimpleTransfer transfer = new SimpleTransfer();
						transfer.setSymbol(order.getCurrency());
						transfer.setRate(order.getHandingRate());
						transfer.setFee(order.getHandingFee());
						transfer.setRemark("法币代付");
						transfer.setType(AssetOperType.FIAT_CURRENCY_PAYOUT);
						transfer.setMerchantId(order.getMerchantId());
						transfer.setTransactionNo(order.getOrderNo());
						transfer.setAmount(order.getAmount());
						merchantAssetsService.transfer(transfer);
						order.setStatus(FiatcurrencyOrderStatus.SUCCESS);
						order.setActualAmount(new BigDecimal(result.getActualAmount()));
						order.setCallbackContent(result.getOriginal());
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						return true;
					case "WAIT":
						if("WAIT".equals(order.getStatus().name())) return false;
						order.setStatus(FiatcurrencyOrderStatus.WAIT);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						break;
					case "PADDING":
						if("PADDING".equals(order.getStatus().name())) return false;
						order.setStatus(FiatcurrencyOrderStatus.PADDING);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						break;
					case "FAIL":
						if("FAIL".equals(order.getStatus().name())) return false;
						SimpleTransfer refund = new SimpleTransfer();
						refund.setSymbol(order.getCurrency());
						refund.setRate(order.getHandingRate());
						refund.setFee(order.getHandingFee());
						refund.setRemark("商家法币代付失败，资金解冻");
						refund.setType(AssetOperType.FIAT_CURRENCY_PAYOUT_REFUND);
						refund.setMerchantId(order.getMerchantId());
						refund.setTransactionNo(order.getOrderNo());
						refund.setAmount(order.getAmount());
						merchantAssetsService.transfer(refund);
						order.setStatus(FiatcurrencyOrderStatus.FAIL);
						fiatcurrencyOrderService.updateFiatCurrency(MapstructUtils.convert(order, FiatcurrencyOrder.class));
						return true;
				}
			}
		}
		return false;
	}
}
