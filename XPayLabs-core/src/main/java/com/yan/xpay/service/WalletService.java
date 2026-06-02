package com.yan.xpay.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.*;
import com.yan.xpay.enums.*;
import com.yan.xpay.mapper.MerchantAddressMapper;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.utils.AmountUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
	private final MerchantAddressMapper merchantAddressMapper;
	private final IAddressPoolService addressPoolService;
	private final IUserAddressService userAddressService;
	private final MerchantMapper merchantMapper;

	/**
	 * 触发收集
	 * @param address
	 * @param balance
	 * @param assetType
	 */
	public void triggerCollection(AssetType assetType, String address, BigInteger balance) {
		UserAddress userAddress = userAddressService.getUserAddress(assetType.getChain(), assetType.getSymbol(), address);
		if(userAddress == null) return;
		Merchant merchant = merchantMapper.selectById(userAddress.getMerchantId());
		GeneralColdAddress generalColdAddress = getColdWallet(merchant, assetType);
		if(generalColdAddress != null && balance.compareTo(AmountUtils.toAmount(generalColdAddress.getCollectAmount(), assetType.getDecimals())) >= 0){
			log.info("触发归集： {} {} {} {}", assetType.getChain(), assetType.getSymbol(), address, balance);
			userAddress.setCollectible(UserAddressCollectible.YES);
		}
		userAddress.setAmount(AmountUtils.fromAmount(balance.toString(), assetType.getDecimals()));
		userAddressService.updateById(userAddress);
	}

	public GeneralColdAddress getColdWallet(Merchant merchant, AssetType assetType) {
		GeneralColdAddress generalColdAddress = new GeneralColdAddress();
		if(merchant == null || merchant.getMerchantSysVersion() == MerchantSysVersion.V3) {//V3版本归集进入平台冷钱包
			if(StrUtil.isBlank(assetType.getColdAddress())) {
				log.error(" 未找到{}链{}平台冷钱包地址", assetType.getChain(), assetType.getSymbol());
				return null;
			}
			generalColdAddress.setColdAddress(assetType.getColdAddress());
			generalColdAddress.setCollectAmount(assetType.getCollectAmount());
		}else {
			MerchantAddress merchantAddress = merchantAddressMapper.selectOne(
				new LambdaQueryWrapper<MerchantAddress>()
					.eq(MerchantAddress::getChain, assetType.getChain()).eq(MerchantAddress::getSymbol, assetType.getSymbol()).eq(MerchantAddress::getMerchantId, merchant.getId()));

			if (merchantAddress == null) {
				log.error(" getColdWallet未找到{}链商家的地址", assetType.getChain());
				return null;
			}
			if(merchant.getIntoType() == IntoType.COLD) //归集进入商家冷钱包（冷钱包私钥在商家手里）
			{
				if(StrUtil.isBlank(merchantAddress.getColdAddress())) {
					log.error(" 未找到{}链商家的冷钱包地址地址 {}", assetType.getChain(), merchantAddress.getColdAddress());
					return null;
				}
				generalColdAddress.setColdAddress(merchantAddress.getColdAddress());
			}
			//归集进入商家热钱包（热钱包私钥在平台）
			else {
				if(StrUtil.isBlank(merchantAddress.getHotAddress())) {
					log.error(" 未找到{}链商家的热钱包地址地址 {}", assetType.getChain(), merchantAddress.getHotAddress());
					return null;
				}
				generalColdAddress.setColdAddress(merchantAddress.getHotAddress());
			}
			generalColdAddress.setCollectAmount(merchantAddress.getCollectAmount());
		}
		generalColdAddress.setSymbol(assetType.getSymbol());
		generalColdAddress.setChain(assetType.getChain());
		return generalColdAddress;
	}

	public GeneralHotAddress getHotWallet(Merchant merchant, AssetType assetType) {
		GeneralHotAddress generalHotAddress = new GeneralHotAddress();
		AddressPool merchantHotAddress = null;
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3) {//V3版本归集进入平台热钱包
			merchantHotAddress = addressPoolService.getPlatformHotAddress(assetType.getChain());
			if (merchantHotAddress == null) {
				log.error(" 未找到{}链平台的热钱包地址", assetType.getChain());
				return null;
			}
			generalHotAddress.setHotAddress(merchantHotAddress.getAddress());
		}else {
			MerchantAddress merchantAddress = merchantAddressMapper.selectOne(
				new LambdaQueryWrapper<MerchantAddress>()
					.eq(MerchantAddress::getChain, assetType.getChain()).eq(MerchantAddress::getSymbol, assetType.getSymbol()).eq(MerchantAddress::getMerchantId, merchant.getId()));
			if (merchantAddress == null) {
				log.error(" getHotWallet未找到{}链商家的钱包地址", assetType.getChain());
				return null;
			}
			merchantHotAddress = addressPoolService.getUserAddress(assetType.getChain(), merchantAddress.getHotAddress());
			if (merchantHotAddress == null) {
				log.error(" 未找到{}链商家的热钱包地址 {}", assetType.getChain(), merchantAddress.getHotAddress());
				return null;
			}
			generalHotAddress.setHotAddress(merchantAddress.getHotAddress());
		}
		generalHotAddress.setKeystore(merchantHotAddress.getKeystore());
		generalHotAddress.setEncrypt(merchantHotAddress.getEncrypt());
		generalHotAddress.setChain(assetType.getChain());
		return generalHotAddress;
	}
}
