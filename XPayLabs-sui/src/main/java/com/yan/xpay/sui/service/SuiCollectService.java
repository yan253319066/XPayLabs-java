package com.yan.xpay.sui.service;

import cn.hutool.core.util.StrUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.CollectRecordMapper;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.service.IAddressPoolService;
import com.yan.xpay.service.IMerchantCostDetailService;
import com.yan.xpay.service.IUserAddressService;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.utils.SuiUtils;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.utils.SecureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuiCollectService {
	private final IAddressPoolService addressPoolService;
	private final AssetTypeCache assetTypeCache;
	private final MerchantMapper merchantMapper;
	private final CollectRecordMapper collectRecordMapper;
	private final WalletService walletService;
	private final IUserAddressService userAddressService;
	private final IMerchantCostDetailService merchantCostDetailService;
	private final SuiConfig suiConfig;

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
				b = processCollection(merchant, generalColdAddress, assetType, userAddress);
			} else {
				b = processTokenCollection(merchant, generalColdAddress, assetType, userAddress);
			}

		}catch (Exception e){
			throw new RuntimeException("["+assetType.getChain()+"-"+assetType.getSymbol()+"]归集报错 ", e);
		}
		if(b){
			userAddress.setCollectible(UserAddressCollectible.PROCESS);
			userAddressService.updateById(userAddress);
		}
	}

	private boolean processTokenCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {
		BigInteger tokenBalance = SuiService.getBalance(userAddress.getAddress(), assetType.getContractAddress(), SuiUtils.getNetwork(assetType.getChain())).toBigInteger();

		if (tokenBalance.longValue() <= 0 || tokenBalance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) < 0) {
			return false;
		}

		BigInteger fee = BigInteger.ZERO;

		BigInteger actualAmount = tokenBalance.subtract(fee);
		if(actualAmount.compareTo(BigInteger.ZERO) <= 0) {
			log.info("[{}] {} 余额不足 {} balance {} fee {}", assetType.getChain(), assetType.getSymbol(), userAddress.getAddress(), tokenBalance, fee);
			return false;
		}

		BigInteger suiBalance = SuiService.getBalance(userAddress.getAddress(), null, SuiUtils.getNetwork(assetType.getChain())).toBigInteger();

		BigInteger gasFee = SuiUtils.getGasBudget(assetType.getChain());

		if (suiBalance.compareTo(gasFee) >= 0) {
			return executeCollection(merchant, generalColdAddress, assetType, userAddress, actualAmount, gasFee, fee);
		} else {
			if(UserAddressCollectible.SENT_TXFEE == userAddress.getCollectible()) {
				log.info("[{}] 等待接收手续费 userAddress {}", assetType.getChain(), userAddress.getAddress());
				return false;
			}
			BigInteger neededAmount = gasFee.subtract(suiBalance);
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

		return false;
	}

	private boolean processCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {
		BigInteger suiBalance = SuiService.getBalance(userAddress.getAddress(), null, SuiUtils.getNetwork(assetType.getChain())).toBigInteger();
		BigInteger fee = BigInteger.ZERO;
		BigInteger gasFee = SuiUtils.getGasBudget(assetType.getChain());
		if (suiBalance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) >= 0) {
			BigInteger actualAmount = suiBalance.subtract(fee).subtract(gasFee); //保留token手续费不提取
			if(actualAmount.compareTo(BigInteger.ZERO) <= 0) {//保留token手续费不提取
//				log.error("[{}] {} 余额不足, {} 无法收集 fee {} txFee {} trxBalance {} address {}", assetType.getChain(), assetType.getSymbol(), userAddress.getAddress(), fee, txFee + tokenTxFee.longValue(), trxBalance, userAddress.getAddress());
				return false;
			}
			return executeCollection(merchant, generalColdAddress, assetType, userAddress, actualAmount, gasFee, fee);
		}
		return false;
	}

	private boolean executeCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress, BigInteger amount, BigInteger txFee, BigInteger fee) {

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
		GeneralColdAddress generalColdAddress, AddressPool userWallet, BigInteger amount) {

		String privateKey = SecureUtils.decodePrivateKey(
			userWallet.getKeystore(),
			userWallet.getEncrypt());
		String txId;
		if (StrUtil.isNotBlank(assetType.getContractAddress())) {
			txId = SuiService.transferToken(privateKey, generalColdAddress.getColdAddress(), amount, assetType.getContractAddress(), SuiUtils.getNetwork(assetType.getChain()));
		}else {
			txId = SuiService.transferSUI(privateKey, generalColdAddress.getColdAddress(), amount, SuiUtils.getNetwork(assetType.getChain()));
		}
		return txId;

	}

	private String sendTransactionFee(Merchant merchant, AssetType assetType, UserAddress userAddress, BigInteger neededAmount) {
		GeneralHotAddress hotAddress = walletService.getHotWallet(merchant, assetType);

		if (hotAddress == null) {
			log.error(" [{}] 未找到热钱包地址", assetType.getChain());
			return null;
		}

		BigInteger hotSuiBalance = SuiService.getBalance(hotAddress.getHotAddress(), null, SuiUtils.getNetwork(assetType.getChain())).toBigInteger();
		log.info(" [{}}] 热钱包 {} 余额: {} 需要的费用: {}", assetType.getChain(), hotAddress.getHotAddress(), hotSuiBalance, neededAmount);
		if (neededAmount.compareTo(hotSuiBalance) > 0) {
			log.error(" [{}] 热钱包余额不足 {} - 需要: {}, 当前: {}", assetType.getChain(), hotAddress.getHotAddress(), neededAmount, hotSuiBalance);
			return null;
		}

		String privateKey = SecureUtils.decodePrivateKey(
			hotAddress.getKeystore(),
			hotAddress.getEncrypt());
		log.info(" [{}] 发送手续费 - HotAddress: {}  -  userAddress: {}", assetType.getChain(), hotAddress.getHotAddress(), userAddress.getAddress());
		String txId = SuiService.transferSUI(privateKey, userAddress.getAddress(), neededAmount, SuiUtils.getNetwork(assetType.getChain()));
		return txId;

	}

	private CollectRecord createCollectRecord(Merchant merchant, String txId, Chain chain, String symbol, String contractAddress,  UserAddress userAddress, String to, BigInteger amount, BigInteger fee, BigDecimal feeRatio, BigInteger txFee){
		if(merchant == null) return null;
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
			record.setAmount(new BigDecimal(amount));
			record.setCollectAmount(new BigDecimal(amount));
		}
		if(fee != null)
			record.setFee(new BigDecimal(fee));
		record.setFeeRatio(feeRatio);
		if(txFee != null)
			record.setTxFee(new BigDecimal(txFee));
		collectRecordMapper.insert(record);

		return record;
	}

}
