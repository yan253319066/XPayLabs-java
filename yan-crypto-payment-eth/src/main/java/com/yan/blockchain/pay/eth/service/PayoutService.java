package com.yan.blockchain.pay.eth.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.service.WalletService;
import com.yan.xpay.utils.AmountUtils;
import com.yan.xpay.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.exception.CipherException;
import org.web3j.tx.Transfer;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {
	private final WalletService walletService;
	private final MerchantMapper merchantMapper;
	private final AssetTypeCache assetTypeCache;
	private final EthService ethService;
	private final NonceManager nonceManager;
	private final PaymentOrderMapper paymentOrderMapper;

	@Transactional
	public void payout(PaymentOrder order) {
		Merchant merchant = merchantMapper.selectById(order.getMerchantId());
		if (merchant == null){
			log.error("未找到商家 {}", order.getMerchantId());
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
		BigInteger ethBalance = ethService.getBalance(assetType.getChain(), generalHotAddress.getHotAddress());

		byte[] key = SecureUtil.decode(generalHotAddress.getEncrypt());
		AES aes = SecureUtil.aes(key);
		String keystore = aes.decryptStr(generalHotAddress.getKeystore());

		Credentials credentials;
		try {
			ObjectMapper mapper = new ObjectMapper();
			WalletFile walletFile = mapper.readValue(keystore,  WalletFile.class);
			credentials = Credentials.create(Wallet.decrypt(generalHotAddress.getEncrypt(), walletFile));
		} catch (CipherException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		BigInteger fee = AmountUtils.toAmount(order.getHandingFee(), assetType.getDecimals());
		if(assetTypeCache.isNativeToken(order.getChain(), order.getSymbol())){
			BigInteger gasPrice = ethService.getGasPrice(assetType.getChain());
			BigInteger gasLimit = Transfer.GAS_LIMIT; // 21000
			BigInteger txFee = gasPrice.multiply(gasLimit);
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) txFee = txFee.multiply(BigInteger.TWO);
			if (amount.compareTo(ethBalance.subtract(fee).subtract(txFee)) > 0) {
				log.error("[{}] 热钱包余额不足 {} {} - 需要: {}, 当前: {}", order.getChain(), assetType.getSymbol(), generalHotAddress.getHotAddress(), amount.add(txFee).add(fee), ethBalance);
				return ;
			}

			BigInteger nonce = nonceManager.getNextNonce(assetType.getChain(),  credentials.getAddress());
			try {
				txId = ethService.sendEth(assetType.getChain(), credentials, order.getReceiveAddress(), amount, nonce);
			}catch (RuntimeException e) {
				nonceManager.confirmNonce(assetType.getChain(), credentials.getAddress(),  nonce);
				throw new RuntimeException(e);
			}

			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) {
				nonce = nonceManager.getNextNonce(assetType.getChain(),  credentials.getAddress());
				try {
					feeTxId = ethService.sendEth(assetType.getChain(), credentials, assetType.getColdAddress(), fee, nonce);
				}catch (RuntimeException e) {
					nonceManager.confirmNonce(assetType.getChain(), credentials.getAddress(),  nonce);
					throw new RuntimeException(e);
				}
			}
		}else{
			BigInteger erc20Balance = ethService.getErc20Balance(assetType.getChain(), assetType.getContractAddress(), generalHotAddress.getHotAddress());
			BigInteger gasLimit = ethService.estimateGasLimit(assetType.getChain(), generalHotAddress.getHotAddress(), assetType.getContractAddress(), amount);
			BigInteger txFee = gasLimit.multiply(ethService.getGasPrice(assetType.getChain()));
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) txFee = txFee.multiply(BigInteger.TWO);
			if (txFee.compareTo(ethBalance) > 0) {
				log.error("[{}] 热钱包手续费不足 {} {}", assetType.getChain(), generalHotAddress.getHotAddress(), ethBalance);
				return ;
			}
			if (amount.compareTo(erc20Balance.subtract(fee)) > 0) {
				log.error("[{}] 热钱包余额不足 {} - 需要: {}, 当前: {}", order.getChain(), assetType.getSymbol(), amount, erc20Balance);
				return ;
			}

			BigInteger nonce = nonceManager.getNextNonce(assetType.getChain(),  credentials.getAddress());
			try {
				txId = ethService.sendErc20(assetType.getChain(), credentials, assetType.getContractAddress(), order.getReceiveAddress(), amount, nonce);
			}catch (RuntimeException e) {
				nonceManager.confirmNonce(assetType.getChain(), credentials.getAddress(),  nonce);
				throw new RuntimeException(e);
			}

			//手续费转入平台冷钱包地址
			if(fee.compareTo(BigInteger.ZERO) > 0 && merchant.getMerchantSysVersion() != MerchantSysVersion.V3) {
				nonce = nonceManager.getNextNonce(assetType.getChain(),  credentials.getAddress());
				try {
					feeTxId = ethService.sendErc20(assetType.getChain(), credentials, assetType.getContractAddress(), assetType.getColdAddress(), fee, nonce);
				}catch (RuntimeException e) {
					nonceManager.confirmNonce(assetType.getChain(), credentials.getAddress(),  nonce);
					throw new RuntimeException(e);
				}
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
