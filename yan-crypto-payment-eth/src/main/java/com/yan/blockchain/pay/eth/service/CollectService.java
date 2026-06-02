package com.yan.blockchain.pay.eth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.bo.MerchantCostDetailBo;
import com.yan.xpay.service.IAddressPoolService;
import com.yan.xpay.service.IMerchantCostDetailService;
import com.yan.xpay.service.IUserAddressService;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.domain.*;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.CollectRecordMapper;
import com.yan.xpay.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.exception.CipherException;
import org.web3j.tx.Transfer;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectService {

	private final IAddressPoolService addressPoolService;
	private final AssetTypeCache assetTypeCache;
	private final MerchantMapper merchantMapper;
	private final CollectRecordMapper collectRecordMapper;
	private final EthService ethService;
	private final WalletService walletService;
	private final IUserAddressService userAddressService;
	private final IMerchantCostDetailService merchantCostDetailService;
	private final NonceManager nonceManager;

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
					log.error("[{}] 没有配置冷钱包地址 {}", assetType.getChain(), assetType.getSymbol());
					return ;
				}

				if (!assetTypeCache.isNativeToken(assetType.getChain(), assetType.getSymbol()))  {
					b = processErc20Collection(merchant, generalColdAddress, assetType, userAddress);
				} else {
					b = processCollection(merchant, generalColdAddress, assetType, userAddress);
				}

			}catch (Exception e){
				throw new RuntimeException("["+assetType.getChain()+"-"+assetType.getSymbol()+"]归集报错 ", e);
			}
			if(b){
				userAddress.setCollectible(UserAddressCollectible.PROCESS);
				userAddressService.updateById(userAddress);
			}

	}


	private boolean processErc20Collection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {
		BigInteger erc20Balance =ethService.getErc20Balance(assetType.getChain(), assetType.getContractAddress(), userAddress.getAddress());

		if (erc20Balance.compareTo(BigInteger.ZERO) <= 0 || erc20Balance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) < 0) {
			return false;
		}

		BigInteger fee = BigInteger.ZERO;

		BigInteger actualAmount = erc20Balance.subtract(fee);
		if(actualAmount.compareTo(BigInteger.ZERO) <= 0) {
			log.info("[{}] {} 余额不足 {} balance {} fee {}", assetType.getChain(), userAddress.getAddress(), assetType.getSymbol(), erc20Balance, fee);
			return false;
		}

		BigInteger ethBalance = ethService.getBalance(assetType.getChain(), userAddress.getAddress());
		BigInteger gasLimit = ethService.estimateGasLimit(assetType.getChain(), userAddress.getAddress(), assetType.getContractAddress(), erc20Balance);
		BigInteger txFee = gasLimit.multiply(ethService.getGasPrice(assetType.getChain()).multiply(BigInteger.TWO));

		if (ethBalance.compareTo(txFee) >= 0) {
			return executeCollection(merchant, generalColdAddress, userAddress, assetType, actualAmount, txFee, fee);
		} else {
			if(UserAddressCollectible.SENT_TXFEE == userAddress.getCollectible()) {
				log.info("[{}] 等待接收手续费 user address {}", assetType.getChain(), userAddress.getAddress());
				return false;
			}
			sendTransactionFee(merchant, assetType, userAddress, txFee.subtract(ethBalance));
		}
		return false;
	}

	private boolean processCollection(Merchant merchant, GeneralColdAddress generalColdAddress, AssetType assetType, UserAddress userAddress) {

		BigInteger ethBalance = ethService.getBalance(assetType.getChain(), userAddress.getAddress());
		BigInteger fee = BigInteger.ZERO;
		BigInteger gasPrice = ethService.getGasPrice(assetType.getChain());
		BigInteger gasLimit = Transfer.GAS_LIMIT; // 21000
		BigInteger txFee = gasPrice.multiply(gasLimit);
		BigInteger tokenGasLimit = ethService.estimateGasLimit(assetType.getChain(), userAddress.getAddress(), assetType.getContractAddress(), new BigInteger("100000000000000000000000"));
		BigInteger tokenTxFee = tokenGasLimit.multiply(gasPrice);
		BigInteger maxTransferAmount = ethBalance.subtract(txFee).subtract(tokenTxFee).subtract(fee);//保留token手续费不提取
		if (ethBalance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) < 0) {
			return false;
		}

		if(maxTransferAmount.compareTo(BigInteger.ZERO)  <= 0){
			log.error("[{}] {} 余额不足, {} 无法收集 fee {} txFee {} ethBalance {} address {}", assetType.getChain(), assetType.getSymbol(), userAddress.getAddress(), fee, txFee.multiply(BigInteger.TWO), ethBalance, userAddress.getAddress());
			return false;
		}

		return executeCollection(merchant, generalColdAddress, userAddress, assetType, maxTransferAmount, txFee, fee);
	}

	private boolean executeCollection(Merchant merchant, GeneralColdAddress generalColdAddress, UserAddress userAddress, AssetType assetType, BigInteger amount, BigInteger txFee, BigInteger fee) {

		AddressPool userWallet = getUserAddress(assetType.getChain(), userAddress.getAddress());
		if (userWallet == null) {
			return false;
		}

		String txId = sendAssetToColdWallet(merchant, assetType, generalColdAddress, userWallet, userAddress, amount);
		if (StrUtil.isNotBlank(txId))  {
			CollectRecord collectRecord = createCollectRecord(merchant, txId, assetType.getChain(), assetType.getSymbol(), userAddress, generalColdAddress.getColdAddress(), amount, fee, merchant.getFeeRatio(), txFee);

			log.info("[{}] 归集已送链上 - 交易ID: {} to {}", assetType.getChain(), txId, generalColdAddress.getColdAddress());
			return true;
		}
		return false;
	}

	private AddressPool getUserAddress(Chain chain, String toAddress) {
		AddressPool userAddress = addressPoolService.getUserAddress(chain, toAddress);

		if (userAddress == null) {
			log.error("[{}] 未找到用户地址: {}", chain, toAddress);
		}
		return userAddress;
	}

	private String sendAssetToColdWallet(Merchant merchant, AssetType assetType,
		GeneralColdAddress generalColdAddress, AddressPool userWallet, UserAddress userAddress, BigInteger amount) {

		byte[] key = SecureUtil.decode(userWallet.getEncrypt());
		AES aes = SecureUtil.aes(key);
		String keystore = aes.decryptStr(userWallet.getKeystore());

		Credentials credentials;
		try {
			ObjectMapper mapper = new ObjectMapper();
			WalletFile walletFile = mapper.readValue(keystore,  WalletFile.class);
			credentials = Credentials.create(Wallet.decrypt(userWallet.getEncrypt(), walletFile));
		} catch (CipherException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		String txId;
		if (StrUtil.isNotBlank(assetType.getContractAddress())) {
			txId = ethService.sendErc20(assetType.getChain(),credentials,assetType.getContractAddress(), generalColdAddress.getColdAddress(),amount);

		} else {
			txId = ethService.sendEth(assetType.getChain(), credentials, generalColdAddress.getColdAddress(), amount);
		}
		if(StrUtil.isBlank(txId)) throw new ServiceException("转账失败"+userAddress.getAddress() + "-" + userAddress.getChain()+"-"+assetType.getSymbol());
		return txId;
	}

	private void sendTransactionFee(Merchant merchant, AssetType assetType, UserAddress userAddress, BigInteger neededAmount) {

		GeneralHotAddress hotAddress = walletService.getHotWallet(merchant, assetType);

		if (hotAddress == null) {
			log.error("[{}] 未找到热钱包地址", userAddress.getChain());
			return;
		}
		BigInteger hotEthBalance = ethService.getBalance(userAddress.getChain(), hotAddress.getHotAddress());
		if (neededAmount.compareTo(hotEthBalance) > 0) {
			log.error("[{}] 热钱包余额不足 {} - 需要: {}, 当前: {}", userAddress.getChain(), hotAddress.getHotAddress(), neededAmount, hotEthBalance);
			return;
		}

		byte[] key = SecureUtil.decode(hotAddress.getEncrypt());
		AES aes = SecureUtil.aes(key);
		String keystore = aes.decryptStr(hotAddress.getKeystore());

		Credentials credentials;
		try {
			ObjectMapper mapper = new ObjectMapper();
			WalletFile walletFile = mapper.readValue(keystore,  WalletFile.class);
			credentials = Credentials.create(Wallet.decrypt(hotAddress.getEncrypt(), walletFile));
		} catch (CipherException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		String txId;
		BigInteger nonce = nonceManager.getNextNonce(assetType.getChain(),  credentials.getAddress());
		try {
			txId = ethService.sendEth(userAddress.getChain(), credentials, userAddress.getAddress(), neededAmount);
		}catch (RuntimeException e) {
			nonceManager.confirmNonce(assetType.getChain(), credentials.getAddress(),  nonce);
			throw new RuntimeException(e);
		}

		if (StrUtil.isNotBlank(txId))  {
			log.info("[{}] 手续费发送成功 - 交易ID: {}", userAddress.getChain(), txId);
			userAddress.setCollectible(UserAddressCollectible.SENT_TXFEE);
			userAddressService.updateById(userAddress);

			AssetType nativeToken = assetTypeCache.getNativeToken(assetType.getChain());

			MerchantCostDetailBo merchantCostDetailBo = new MerchantCostDetailBo();
			merchantCostDetailBo.setCostType(CostType.SEND_GAS);
			merchantCostDetailBo.setChain(assetType.getChain());
			merchantCostDetailBo.setSymbol(nativeToken.getSymbol());
			merchantCostDetailBo.setMerchantId(merchant.getId());
			merchantCostDetailBo.setBusinessId(txId);
			merchantCostDetailBo.setAmount(AmountUtils.fromAmount(neededAmount.toString(), nativeToken.getDecimals()).negate());
			merchantCostDetailService.insertByBo(merchantCostDetailBo);

		}
	}

	private CollectRecord createCollectRecord(Merchant merchant, String txId, Chain chain, String symbol, UserAddress userAddress, String to, BigInteger amount, BigInteger fee, BigDecimal feeRatio, BigInteger txFee){
		if(merchant == null) return null;
        AssetType assetType = assetTypeCache.getBySymbol(userAddress.getChain(), userAddress.getSymbol());
		CollectRecord record = new CollectRecord();
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3)
			record.setMerchantId(0L);
		else record.setMerchantId(merchant.getId());
		record.setTxId(txId);
		record.setChain(chain);
		record.setSymbol(symbol);
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
