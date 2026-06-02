package com.yan.blockchain.pay.tron.service;

import cn.hutool.core.util.StrUtil;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.utils.FeeeUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.MerchantRechargeWithdrawMapper;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.SecureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Response;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawTronService {
	private final MerchantMapper merchantMapper;
	private final TronService tronService;
	private final AssetTypeCache assetTypeCache;
	private final WalletService walletService;
	private final TronConfig tronConfig;
	private final MerchantRechargeWithdrawMapper merchantRechargeWithdrawMapper;

	@Transactional
	public void withdraw(MerchantRechargeWithdraw withdraw) {
		Merchant merchant = merchantMapper.selectById(withdraw.getMerchantId());
		AssetType assetType = assetTypeCache.getBySymbol(withdraw.getChain(), withdraw.getSymbol());
		GeneralHotAddress generalHotAddress = walletService.getHotWallet(merchant, assetType);
		if (generalHotAddress == null){
			log.error("未找到 {} 的地址", withdraw.getMerchantId());
			return;
		}

		String privateKey = SecureUtils.decodePrivateKey(
			generalHotAddress.getKeystore(),
			generalHotAddress.getEncrypt());

		ApiWrapper wrapper;
		if(assetType.getChain() == Chain.TRON)
			wrapper = ApiWrapper.ofMainnet(privateKey);
		else wrapper = ApiWrapper.ofShasta(privateKey);
		withdraw.setPayAddress(wrapper.keyPair.toBase58CheckAddress());
		try {
			String txId;
			String feeTxId;
			BigInteger amount = AmountUtils.toAmount(withdraw.getAmount(), assetType.getDecimals());
			Long trxBalance = tronService.getTrxBalance(assetType.getChain(), generalHotAddress.getHotAddress());
			BigInteger fee = AmountUtils.toAmount(withdraw.getFee(), assetType.getDecimals());
			if(assetTypeCache.isNativeToken(withdraw.getChain(), withdraw.getSymbol())){
				BigInteger txFee = new BigInteger("2100000");
				if (amount.longValue() > trxBalance - fee.longValue() - txFee.longValue()) {
					log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", withdraw.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.longValue()+fee.longValue()+txFee.longValue(), trxBalance);
					return;
				}
				txId = tronService.sendTrx(wrapper, withdraw.getReceiveAddress(), amount.longValue());
				//手续费转入平台冷钱包地址
				if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3)
					feeTxId = tronService.sendTrx(wrapper, assetType.getColdAddress(), fee.longValue());
			}else{
				BigInteger trc20Balance = tronService.getTrc20Balance(assetType.getChain(), generalHotAddress.getHotAddress(), assetType.getContractAddress());
				if(amount.compareTo(trc20Balance.subtract(fee)) >= 0 ){
					log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", withdraw.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.longValue()+fee.longValue(), trc20Balance);
					return;
				}
				String energyApikey;
				if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
					energyApikey = tronConfig.getEnergyApikey();
				}else {
					energyApikey = merchant.getEnergyApikey();
				}
				if(StrUtil.isNotBlank(energyApikey) && assetType.getChain() == Chain.TRON){
					long needEnergy = FeeeUtil.estimateEnergy(generalHotAddress.getHotAddress(), withdraw.getReceiveAddress(), energyApikey);
					if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) needEnergy = needEnergy + 65000L;
					Response.AccountResourceMessage resource = tronService.getAccountResource(assetType.getChain(), generalHotAddress.getHotAddress());
					long toEnergyRemaining = resource.getEnergyLimit() - resource.getEnergyUsed();
					if(toEnergyRemaining < needEnergy) {
						int res = FeeeUtil.leaseEnergy(generalHotAddress.getHotAddress(), needEnergy - toEnergyRemaining, energyApikey);
						if(res == 0) {
							log.info("能量已租，可以发送提币。 {}", generalHotAddress.getHotAddress());
							try {
								Thread.sleep(500L);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
						return ;
					}
				}else{
					BigInteger txFee = AmountUtils.toAmount(tronConfig.getTxFee(), assetType.getDecimals());
					if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) txFee = txFee.multiply(BigInteger.TWO);
					if (txFee.longValue() > trxBalance) {
						log.error("[{}] 热钱包手续费不足 {} {}", assetType.getChain(), generalHotAddress.getHotAddress(), trxBalance);
						return ;
					}
				}
				txId = tronService.sendTrc20(wrapper, assetType.getContractAddress(), withdraw.getReceiveAddress(), amount.longValue());
				//手续费转入平台冷钱包地址
				if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3)
					feeTxId = tronService.sendTrc20(wrapper, assetType.getContractAddress(), assetType.getColdAddress(), fee.longValue());
			}

			withdraw.setTxId(txId);
			withdraw.setStatus(RechargeWithdrawStatus.SUBMITTED);
			merchantRechargeWithdrawMapper.updateById(withdraw);
		}finally {
			wrapper.close();
		}

	}
}
