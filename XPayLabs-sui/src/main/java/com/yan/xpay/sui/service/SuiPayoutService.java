package com.yan.xpay.sui.service;

import cn.hutool.core.date.DateUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.GeneralHotAddress;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.domain.PaymentOrder;
import com.yan.xpay.enums.MerchantSysVersion;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.sui.utils.SuiUtils;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.SecureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuiPayoutService {
	private final WalletService walletService;
	private final MerchantMapper merchantMapper;
	private final AssetTypeCache assetTypeCache;
	private final PaymentOrderMapper paymentOrderMapper;

	@Transactional
	public void payout(PaymentOrder order) {
		Merchant merchant = merchantMapper.selectById(order.getMerchantId());
		if (merchant == null){
			log.error("[{}]未找到商家 {}", order.getChain(), order.getMerchantId());
			return;
		}

		AssetType assetType = assetTypeCache.getBySymbol(order.getChain(), order.getSymbol());
		GeneralHotAddress generalHotAddress = walletService.getHotWallet(merchant, assetType);
		if (generalHotAddress == null){
			log.error("未找到 {} 的地址", order.getMerchantId());
			return ;
		}
		String txId;
		String feeTxId = "";
		BigInteger amount = AmountUtils.toAmount(order.getAmount(), assetType.getDecimals());
		BigInteger suiBalance = SuiService.getBalance(generalHotAddress.getHotAddress(), null, SuiUtils.getNetwork(order.getChain())).toBigInteger();

		String privateKey = SecureUtils.decodePrivateKey(
			generalHotAddress.getKeystore(),
			generalHotAddress.getEncrypt());

		BigInteger fee = AmountUtils.toAmount(order.getHandingFee(), assetType.getDecimals());
		if(assetTypeCache.isNativeToken(order.getChain(), order.getSymbol())){
			BigInteger gasFee = SuiUtils.getGasBudget(order.getChain());
			if (amount.compareTo(suiBalance.subtract(fee).subtract(gasFee)) > 0) {
				log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", order.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.add(gasFee).add(fee), suiBalance);
				return ;
			}
			txId = SuiService.transferSUI(privateKey, order.getReceiveAddress(), amount, SuiUtils.getNetwork(order.getChain()));
			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3)
				feeTxId = SuiService.transferSUI(privateKey, assetType.getColdAddress(), fee, SuiUtils.getNetwork(order.getChain()));
		}else{
			BigInteger tokenBalance = SuiService.getBalance(generalHotAddress.getHotAddress(), assetType.getContractAddress(), SuiUtils.getNetwork(order.getChain())).toBigInteger();
			BigInteger gasFee = SuiUtils.getGasBudget(order.getChain());
			if (gasFee.compareTo(suiBalance) > 0) {
				log.error("[{}] 热钱包手续费不足 {} {}", assetType.getChain(), generalHotAddress.getHotAddress(), suiBalance);
				return ;
			}
			if (amount.compareTo(tokenBalance.subtract(fee)) > 0) {
				log.error("[{}] 热钱包余额不足 {} - 需要: {}, 当前: {}", order.getChain(), assetType.getSymbol(), amount, tokenBalance);
				return ;
			}

			txId = SuiService.transferToken(privateKey, order.getReceiveAddress(), amount, assetType.getContractAddress(), SuiUtils.getNetwork(order.getChain()));

			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) {
				feeTxId = SuiService.transferToken(privateKey, order.getReceiveAddress(), fee, assetType.getContractAddress(), SuiUtils.getNetwork(order.getChain()));
			}
		}

		order.setTxId(txId);
		order.setStatus(OrderStatus.PENDING);
		order.setPayAddress(generalHotAddress.getHotAddress());
		order.setNotifyStatus(NotifyStatus.SUCCESS);
		order.setNotifyTime(DateUtil.date());
		paymentOrderMapper.updateById(order);
	}
}
