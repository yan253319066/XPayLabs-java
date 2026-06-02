package com.yan.blockchain.pay.tron.service;

import cn.hutool.core.util.StrUtil;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.service.IAddressPoolService;
import com.yan.xpay.service.IMerchantCostDetailService;
import com.yan.xpay.service.IUserAddressService;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.domain.*;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.FeeeUtil;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.CollectRecordMapper;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.utils.SecureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectTronService {
	private final TronService tronService;
	private final IAddressPoolService addressPoolService;
	private final AssetTypeCache assetTypeCache;
	private final MerchantMapper merchantMapper;
	private final CollectRecordMapper collectRecordMapper;
	private final WalletService walletService;
	private final IUserAddressService userAddressService;
	private final IMerchantCostDetailService merchantCostDetailService;
	private final TronConfig tronConfig;

	@Transactional
	public void collect(UserAddress userAddress) {
		Merchant merchant = merchantMapper.selectById(userAddress.getMerchantId());
		if(merchant == null){
			log.error("未找到商家 {}", userAddress.getMerchantId());
			return ;
		}

		AssetType assetType = assetTypeCache.getBySymbol(userAddress.getChain(), userAddress.getSymbol());
		boolean b;
		try {
			GeneralColdAddress generalColdAddress = walletService.getColdWallet(merchant, assetType);
			if (generalColdAddress == null) {
				log.error("没有配置冷钱包地址 {} {}", assetType.getChain(), assetType.getSymbol());
				return ;
			}

			if (assetTypeCache.isNativeToken(assetType.getChain(), assetType.getSymbol()))  {
				b = processTrxCollection(merchant, generalColdAddress, assetType, userAddress);
			} else {
				b = processTrc20Collection(merchant, generalColdAddress, assetType, userAddress);
			}

		}catch (Exception e){
			throw new RuntimeException("["+assetType.getChain()+"-"+assetType.getSymbol()+"]归集报错 ", e);
		}
		if(b){
			userAddress.setCollectible(UserAddressCollectible.PROCESS);
			userAddressService.updateById(userAddress);
		}
	}

	private boolean processTrc20Collection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {
		BigInteger trc20Balance = tronService.getTrc20Balance(assetType.getChain(),
			userAddress.getAddress(),
			assetType.getContractAddress());

		if (trc20Balance.longValue() <= 0 || trc20Balance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) < 0) {
			return false;
		}

		BigDecimal fee = BigDecimal.ZERO;

		long actualAmount = trc20Balance.longValue() - fee.longValue();
		if(actualAmount <= 0) {
			log.info("[{}] {} 余额不足 {} balance {} fee {}", assetType.getChain(), assetType.getSymbol(), userAddress.getAddress(), trc20Balance, fee);
			return false;
		}

		long trxBalance = tronService.getTrxBalance(assetType.getChain(), userAddress.getAddress());

		String energyApikey;
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
			energyApikey = tronConfig.getEnergyApikey();
		}else {
			energyApikey = merchant.getEnergyApikey();
		}
		if(StrUtil.isNotBlank(energyApikey) && assetType.getChain() == Chain.TRON){
			long txFee = 1100000L;
			long needEnergy = FeeeUtil.estimateEnergy(userAddress.getAddress(), generalColdAddress.getColdAddress(), energyApikey) + 500;
			Response.AccountResourceMessage resource = tronService.getAccountResource(assetType.getChain(), userAddress.getAddress());
			long toEnergyRemaining = resource.getEnergyLimit() - resource.getEnergyUsed();
			if(toEnergyRemaining >= needEnergy){
				return executeCollection(merchant, generalColdAddress, assetType, userAddress, actualAmount, txFee, fee.longValue());
			}else {
				if(UserAddressCollectible.SENT_TXFEE == userAddress.getCollectible()) {
					log.info("[{}] 等待接收手续费和能量 userAddress {}", assetType.getChain(), userAddress.getAddress());
					return false;
				}
				String txId = null;
				if(!tronService.isActivated(assetType.getChain(), userAddress.getAddress()))
					txId = sendTransactionFee(merchant, assetType, userAddress, txFee);
                log.info("[{}] 开始租赁能量 {} {} ", assetType.getChain(), userAddress.getAddress(), needEnergy - toEnergyRemaining);
				int res = FeeeUtil.leaseEnergy(userAddress.getAddress(), needEnergy - toEnergyRemaining, energyApikey);
				if (StrUtil.isNotBlank(txId) && res == 0)  {
					log.info("[TRON] 手续费和能量发送成功");
					userAddress.setCollectible(UserAddressCollectible.SENT_TXFEE);
					userAddressService.updateById(userAddress);
				}
			}
		}else{
			BigInteger txFee = AmountUtils.toAmount(tronConfig.getTxFee(),  6);

			if (trxBalance >= txFee.longValue()) {
				return executeCollection(merchant, generalColdAddress, assetType, userAddress, actualAmount, txFee.longValue(), fee.longValue());
			} else {
				if(UserAddressCollectible.SENT_TXFEE == userAddress.getCollectible()) {
					log.info("[{}] 等待接收手续费 userAddress {}", assetType.getChain(), userAddress.getAddress());
					return false;
				}
				long neededAmount = txFee.longValue() - trxBalance;
				String txId = sendTransactionFee(merchant, assetType, userAddress, neededAmount);
				if (StrUtil.isNotBlank(txId))  {
					log.info("[{}] 手续费发送成功 - 交易ID: {}", assetType.getChain(), txId);
					userAddress.setCollectible(UserAddressCollectible.SENT_TXFEE);
					userAddressService.updateById(userAddress);

					AssetType nativeToken = assetTypeCache.getNativeToken(assetType.getChain());

					MerchantCostDetailBo merchantCostDetailBo = new MerchantCostDetailBo();
					merchantCostDetailBo.setCostType(CostType.SEND_GAS);
					merchantCostDetailBo.setChain(assetType.getChain());
					merchantCostDetailBo.setSymbol(nativeToken.getSymbol());
					merchantCostDetailBo.setMerchantId(merchant.getId());
					merchantCostDetailBo.setBusinessId(txId);
					merchantCostDetailBo.setAmount(AmountUtils.fromAmount(String.valueOf(neededAmount), nativeToken.getDecimals()).negate());
					merchantCostDetailService.insertByBo(merchantCostDetailBo);

				}
			}
		}
		return false;
	}

	private boolean processTrxCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {
		Long trxBalance = tronService.getTrxBalance(assetType.getChain(), userAddress.getAddress());
		BigDecimal fee = BigDecimal.ZERO;
		long txFee = 500000L;
		BigInteger tokenTxFee = AmountUtils.toAmount(tronConfig.getTxFee(),  6);
		if (trxBalance  >= AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals()).longValue()) {
			long actualAmount = trxBalance - fee.longValue() - txFee - tokenTxFee.longValue(); //保留token手续费不提取
			if(actualAmount <= 0) {//保留token手续费不提取
//				log.error("[{}] {} 余额不足, {} 无法收集 fee {} txFee {} trxBalance {} address {}", assetType.getChain(), assetType.getSymbol(), userAddress.getAddress(), fee, txFee + tokenTxFee.longValue(), trxBalance, userAddress.getAddress());
				return false;
			}
			return executeCollection(merchant, generalColdAddress, assetType, userAddress, actualAmount, txFee, fee.longValue());
		}
		return false;
	}

	private boolean executeCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress, Long amount, Long txFee, Long fee) {

		AddressPool userWallet = getUserAddress(assetType.getChain(), userAddress.getAddress());
		if (userWallet == null) {
			return false;
		}

		String txId = sendAssetToColdWallet(assetType, generalColdAddress, userWallet, amount);
		if (StrUtil.isNotBlank(txId))  {
			CollectRecord collectRecord = createCollectRecord(merchant, txId, assetType.getChain(), assetType.getSymbol(), assetType.getContractAddress(), userAddress, generalColdAddress.getColdAddress(), amount, fee, merchant.getFeeRatio(), txFee);

			log.info("[{}] 归集已送链上 - 交易ID: {} to {}", assetType.getChain(), txId, generalColdAddress.getColdAddress());
			return true;
		}
		return false;
	}

	private AddressPool getUserAddress(Chain chain, String toAddress) {
		AddressPool userAddress = addressPoolService.getUserAddress(chain, toAddress);
		if (userAddress == null) {
			log.error(" 未找到用户地址: {}", toAddress);
		}
		return userAddress;
	}

	private String sendAssetToColdWallet(AssetType assetType,
		GeneralColdAddress generalColdAddress, AddressPool userWallet, Long amount) {

		String privateKey = SecureUtils.decodePrivateKey(
			userWallet.getKeystore(),
			userWallet.getEncrypt());

		ApiWrapper wrapper;
		if(assetType.getChain() == Chain.TRON)
			wrapper = ApiWrapper.ofMainnet(privateKey);
		else wrapper = ApiWrapper.ofShasta(privateKey);

		try {
			String txId;
			if (StrUtil.isNotBlank(assetType.getContractAddress())) {
				txId = tronService.sendTrc20(
					wrapper,
					assetType.getContractAddress(),
					generalColdAddress.getColdAddress(),
					amount
				);
			} else {
				txId = tronService.sendTrx(
					wrapper,
					generalColdAddress.getColdAddress(),
					amount
				);
			}
			return txId;
		}finally {
			wrapper.close();
		}

	}

	private String sendTransactionFee(Merchant merchant, AssetType assetType, UserAddress userAddress, long neededAmount) {
		GeneralHotAddress hotAddress = walletService.getHotWallet(merchant, assetType);

		if (hotAddress == null) {
			log.error(" [{}] 未找到热钱包地址", assetType.getChain());
			return null;
		}

		long hotTrxBalance = tronService.getTrxBalance(assetType.getChain(), hotAddress.getHotAddress());
		log.info(" [{}}] 热钱包 {} 余额: {} 需要的费用: {}", assetType.getChain(), hotAddress.getHotAddress(), hotTrxBalance, neededAmount);
		if (neededAmount > hotTrxBalance) {
			log.error(" [{}] 热钱包余额不足 {} - 需要: {}, 当前: {}", assetType.getChain(), hotAddress.getHotAddress(), neededAmount, hotTrxBalance);
			return null;
		}

		String privateKey = SecureUtils.decodePrivateKey(
			hotAddress.getKeystore(),
			hotAddress.getEncrypt());
		ApiWrapper wrapper;
		if(assetType.getChain() == Chain.TRON)
			wrapper = ApiWrapper.ofMainnet(privateKey);
		else wrapper = ApiWrapper.ofShasta(privateKey);
		try {
			log.info(" [{}] 发送手续费 - HotAddress: {}  -  userAddress: {}", assetType.getChain(), hotAddress.getHotAddress(), userAddress.getAddress());
			String txId = tronService.sendTrx(
				wrapper,
				userAddress.getAddress(),
				neededAmount
			);
			return txId;
		}finally {
			wrapper.close();
		}

	}

	private CollectRecord createCollectRecord(Merchant merchant, String txId, Chain chain, String symbol, String contractAddress,  UserAddress userAddress, String to, Long amount, Long fee, BigDecimal feeRatio, Long txFee){
		if(merchant == null) return null;
        AssetType assetType = assetTypeCache.getBySymbol(userAddress.getChain(), userAddress.getSymbol());
		CollectRecord record = new CollectRecord();
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3)
			record.setMerchantId(0L);
		else record.setMerchantId(merchant.getId());
		record.setTxId(txId);
		record.setChain(chain);
		record.setSymbol(symbol);
		if(StrUtil.isNotBlank(contractAddress))
			record.setContractAddress(contractAddress);
		record.setStatus(BlockchainStatus.PENDING);
		record.setFromAddress(userAddress.getAddress());
		record.setToAddress(to);
		if(amount != null){
            BigDecimal _amount =  AmountUtils.fromAmount(amount.toString(), assetType.getDecimals());
			record.setAmount(_amount);
			record.setCollectAmount(_amount);
		}
		if(fee != null)
			record.setFee(AmountUtils.fromAmount(fee.toString(), assetType.getDecimals()));
		record.setFeeRatio(feeRatio);
		if(txFee != null)
			record.setTxFee(AmountUtils.fromAmount(txFee.toString(), assetType.getDecimals()));
		collectRecordMapper.insert(record);

		return record;
	}

}
