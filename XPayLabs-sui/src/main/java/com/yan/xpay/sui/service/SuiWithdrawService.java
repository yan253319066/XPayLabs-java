package com.yan.xpay.sui.service;

import cn.hutool.core.util.StrUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.GeneralHotAddress;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.enums.MerchantSysVersion;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
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
public class SuiWithdrawService {
	private final MerchantMapper merchantMapper;
	private final AssetTypeCache assetTypeCache;
	private final WalletService walletService;
	private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;

	@Transactional
	public void withdraw(MerchantRechargeWithdraw withdraw) {
		Merchant merchant = merchantMapper.selectById(withdraw.getMerchantId());
		if (merchant == null){
			log.error("未找到商家 {}", withdraw.getMerchantId());
			return;
		}
		AssetType assetType = assetTypeCache.getBySymbol(withdraw.getChain(), withdraw.getSymbol());
		GeneralHotAddress generalHotAddress = walletService.getHotWallet(merchant, assetType);
		if (generalHotAddress == null){
			log.error("未找到 {} 的地址", withdraw.getMerchantId());
			return;
		}
		String txId;
		String feeTxId;
		BigInteger amount = AmountUtils.toAmount(withdraw.getAmount(), assetType.getDecimals());
		BigInteger suiBalance = SuiService.getBalance(generalHotAddress.getHotAddress(), null, SuiUtils.getNetwork(assetType.getChain())).toBigInteger();

		String privateKey = SecureUtils.decodePrivateKey(
			generalHotAddress.getKeystore(),
			generalHotAddress.getEncrypt());

		withdraw.setPayAddress(generalHotAddress.getHotAddress());
		BigInteger fee = AmountUtils.toAmount(withdraw.getFee(), assetType.getDecimals());
		if(assetTypeCache.isNativeToken(withdraw.getChain(), withdraw.getSymbol())){
			BigInteger gas = SuiUtils.getGasBudget(assetType.getChain());
			if (amount.compareTo(suiBalance.subtract(fee).subtract(gas)) > 0) {
				log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", withdraw.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.add(gas).add(fee), suiBalance);
				return;
			}

			txId = SuiService.transferSUI(privateKey, withdraw.getReceiveAddress(), amount, SuiUtils.getNetwork(assetType.getChain()));

			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) {
					feeTxId = SuiService.transferSUI(privateKey, withdraw.getReceiveAddress(), fee, SuiUtils.getNetwork(assetType.getChain()));
			}
		}else{
			BigInteger tokenBalance = SuiService.getBalance(generalHotAddress.getHotAddress(), assetType.getContractAddress(), SuiUtils.getNetwork(assetType.getChain())).toBigInteger();
			BigInteger gas = SuiUtils.getGasBudget(assetType.getChain());
			if (gas.compareTo(suiBalance) > 0) {
				log.error("[{}] 热钱包手续费不足 {} {}", assetType.getChain(), generalHotAddress.getHotAddress(), suiBalance);
				return ;
			}
			if (amount.compareTo(tokenBalance.subtract(fee)) > 0) {
				log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", withdraw.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.add(fee), tokenBalance);
				return ;
			}

			txId = SuiService.transferToken(privateKey, withdraw.getReceiveAddress(), amount, assetType.getContractAddress(), SuiUtils.getNetwork(assetType.getChain()));

			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) {
				feeTxId = SuiService.transferToken(privateKey, withdraw.getReceiveAddress(), fee, assetType.getContractAddress(), SuiUtils.getNetwork(assetType.getChain()));
			}
		}

		if (StrUtil.isNotBlank(txId)) {
			withdraw.setTxId(txId);
			withdraw.setStatus(RechargeWithdrawStatus.SUBMITTED);
			merchantRechargeWithdrawMapper.updateById(withdraw);
		}
	}
}
