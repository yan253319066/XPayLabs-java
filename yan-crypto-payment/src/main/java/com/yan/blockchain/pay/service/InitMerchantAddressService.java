package com.yan.blockchain.pay.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.*;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.UserAddressCollectible;
import com.yan.xpay.mapper.*;
import com.yan.xpay.service.IAddressPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.enums.Status;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class InitMerchantAddressService {

	private final AssetTypeMapper assetTypeMapper;
	private final IAddressPoolService addressPoolService;
	private final AddressPoolMapper addressPoolMapper;
	private final MerchantAddressMapper merchantAddressMapper;
	private final UserAddressMapper userAddressMapper;
	private final MerchantAssetTypeMapper merchantAssetTypeMapper;
	private final MerchantMapper merchantMapper;

	@Transactional
	public void modifyAllAddress(){
		//把AddressPool表里其中一个地址的AddressType改成Hot，AddressStatus改成USED，添加AssetType里的hotAddress，这是平台热钱包地址
		List<AssetType> assetTypes = assetTypeMapper.selectList(
			new LambdaQueryWrapper<AssetType>()
				.eq(AssetType::getEnabled, Status.ENABLED.name()));

		// 按照chain字段分组
		Map<Chain, List<AssetType>> assetMap = assetTypes.stream()
			.collect(Collectors.groupingBy(AssetType::getChain));
		// 遍历Map并修改AssetType
		assetMap.forEach((chain,  assetList) -> {
			boolean isHotAddressEmpty = false;
			for (AssetType assetType : assetList) {
				if(StrUtil.isBlank(assetType.getHotAddress())){
					isHotAddressEmpty = true;
					break;
				}
			}
			if(isHotAddressEmpty) {
				AddressPool addressPool = addressPoolMapper.selectOne(new LambdaQueryWrapper<AddressPool>()
					.eq(AddressPool::getChain, chain)
					.eq(AddressPool::getUsed, AddressStatus.USED)
					.eq(AddressPool::getType, AddressType.HOT));
				if(addressPool == null){
					addressPool = addressPoolMapper.selectOne(new LambdaQueryWrapper<AddressPool>()
						.eq(AddressPool::getChain, chain)
						.eq(AddressPool::getUsed, AddressStatus.UNUSED)
						.eq(AddressPool::getType, AddressType.GENERAL)
						.last("LIMIT 1 FOR UPDATE"));
				}
				if(addressPool == null) {
                    log.error("[{}] 地址池没有有效地址，等待地址池创建地址后重启服务...", chain);
                    return;
                }

				AddressPool finalAddressPool = addressPool;
				assetList.forEach(assetType  -> {
					if(StrUtil.isBlank(assetType.getHotAddress())){
						finalAddressPool.setUsed(AddressStatus.USED);
						finalAddressPool.setType(AddressType.HOT);
						addressPoolMapper.updateById(finalAddressPool);
						assetType.setHotAddress(finalAddressPool.getAddress());
						assetTypeMapper.updateById(assetType);
					}
				});
			}
		});


		// 获取所有MerchantAddress
		List<MerchantAddress> merchantAddressList = merchantAddressMapper.selectList();

		//往用户资产类型增加数据------只需要执行一次，因为表是后面加的 start----------
//		List<MerchantAssetType> merchantAssetTypeList = new ArrayList<>();
//		List<Merchant> merchantList = merchantMapper.selectList();
//		merchantList.forEach(merchant -> {
//			assetTypes.forEach(assetType -> {
//				MerchantAssetType merchantAssetType = new MerchantAssetType();
//				merchantAssetType.setAssetTypeId(assetType.getId());
//				merchantAssetType.setMerchantId(merchant.getId());
//				merchantAssetType.setStatus(Status.ENABLED);
//				merchantAssetTypeList.add(merchantAssetType);
//			});
//		});
//		if(!merchantAssetTypeList.isEmpty()) merchantAssetTypeMapper.insertBatch(merchantAssetTypeList);
		//往用户资产类型增加数据------只需要执行一次，因为表是后面加的 end----------

		// 按照merchantId和chain字段分组，生成Map<String, List<MerchantAddress>>
		List<MerchantAddress> updateMerchantAddressList = new ArrayList<>();
		Map<String, List<MerchantAddress>> merchantAddressMap = merchantAddressList.stream()
			.collect(Collectors.groupingBy(
				address -> address.getMerchantId()  + "_" + address.getChain()   // 组合key
			));

		// 遍历Map并修改MerchantAddress
		merchantAddressMap.forEach((key,  addressList) -> {
			String address = null;
			boolean isHotAddressEmpty = false;
			for (MerchantAddress value : addressList) {
				if (StrUtil.isBlank(value.getHotAddress())) {
					isHotAddressEmpty = true;
					address = addressPoolService.getUnAddress(addressList.get(0).getChain());
					if(StrUtil.isEmpty(address)) throw new ServiceException("No available address is available at the moment. Please try again later.");
					break;
				}
			}
			if(isHotAddressEmpty) {
				String finalAddress = address;
				addressList.forEach(merchantAddress  -> {
					if(StrUtil.isBlank(merchantAddress.getHotAddress()))//为空的就添加地址。
					{
						merchantAddress.setHotAddress(finalAddress);
						updateMerchantAddressList.add(merchantAddress);
					}
				});
			}
		});
		if(!updateMerchantAddressList.isEmpty())
			merchantAddressMapper.updateBatchById(merchantAddressList);

		// 处理缺失的MerchantAddress记录
		updateMerchantAddressList.addAll(processMissingRecords(assetTypes, merchantAddressList));

		//为了能归集才加入这个表的AddressStatus不能修改成UNUSED
		List<UserAddress> userAddressList = new ArrayList<>();
		updateMerchantAddressList.forEach(merchantAddress -> {
			UserAddress userAddress = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getAddress, merchantAddress.getHotAddress()).eq(UserAddress::getChain, merchantAddress.getChain()).eq(UserAddress::getSymbol, merchantAddress.getSymbol()));
			if(userAddress == null) {
				userAddress = new UserAddress();
				userAddress.setMerchantId(merchantAddress.getMerchantId());
				userAddress.setAddress(merchantAddress.getHotAddress());
				userAddress.setStatus(AddressStatus.USED);
				userAddress.setSymbol(merchantAddress.getSymbol());
				userAddress.setChain(merchantAddress.getChain());
				userAddress.setAmount(BigDecimal.ZERO);
				userAddress.setCollectible(UserAddressCollectible.NO);
				userAddress.setUserId("HOT");
				userAddressList.add(userAddress);
			}
		});
		if(!userAddressList.isEmpty()) userAddressMapper.insertBatch(userAddressList);
	}

	/**
	 * 处理缺失的 MerchantAddress 记录（修正：确保同 merchantId 同 chain 复用已分配地址）
	 */
	private List<MerchantAddress> processMissingRecords(List<AssetType> assetTypes, List<MerchantAddress> merchantAddressList) {
		// 1. 按 merchant 分组（数据库已存在的记录）
		Map<Long, List<MerchantAddress>> merchantMap = merchantAddressList.stream()
			.collect(Collectors.groupingBy(MerchantAddress::getMerchantId));

		// 2. 构建一个临时映射：merchantId -> (chain -> address)
		Map<Long, Map<Chain, String>> assignedMap = new HashMap<>();
		for (Map.Entry<Long, List<MerchantAddress>> e : merchantMap.entrySet()) {
			Long merchantId = e.getKey();
			Map<Chain, String> chainMap = new HashMap<>();
			for (MerchantAddress ma : e.getValue()) {
				if (StrUtil.isNotEmpty(ma.getHotAddress())) {
					chainMap.put(ma.getChain(), ma.getHotAddress());
				}
			}
			assignedMap.put(merchantId, chainMap);
		}

		List<MerchantAddress> toInsert = new ArrayList<>();

		// 3. 遍历每个商户，检查 assetTypes，缺什么补什么
		for (Map.Entry<Long, List<MerchantAddress>> entry : merchantMap.entrySet()) {
			Long merchantId = entry.getKey();
			List<MerchantAddress> merchantAddresses = entry.getValue();

			// 获取或创建该 merchant 的 chain->address 映射
			Map<Chain, String> chainAssigned = assignedMap.computeIfAbsent(merchantId, k -> new HashMap<>());

			for (AssetType asset : assetTypes) {
				Chain chain = asset.getChain();
				String symbol = asset.getSymbol();

				boolean exists = merchantAddresses.stream()
					.anyMatch(a -> a.getChain() == chain && a.getSymbol().equals(symbol));

				if (!exists) {
					// 先尝试从已有的（数据库的）记录里复用
					String address = chainAssigned.get(chain); // 优先从 assignedMap 取（包含数据库已有和已分配的）
					if (StrUtil.isEmpty(address)) {
						// 如果 assignedMap 里没有，再尝试从原始 merchantAddresses（冗余但安全）
						address = merchantAddresses.stream()
							.filter(a -> a.getChain().equals(chain))
							.map(MerchantAddress::getHotAddress)
							.findFirst()
							.orElse(null);
					}

					// 最后：如果仍没有，则从地址池取一个（并记录到 assignedMap，避免接下来重复取）
					if (StrUtil.isEmpty(address)) {
						address = addressPoolService.getUnAddress(chain);
						if (StrUtil.isEmpty(address)) {
							throw new ServiceException("生成商家地址出错，No available address is available at the moment. Please try again later.");
						}
						// 记录已分配的地址（这样同 merchant 同 chain 的后续 symbol 会复用）
						chainAssigned.put(chain, address);
					} else {
						// 若从 merchantAddresses 找到（数据库已有），也放到 assignedMap，统一后续复用逻辑
						chainAssigned.putIfAbsent(chain, address);
					}

					// 构造新 MerchantAddress
					MerchantAddress newAddr = new MerchantAddress();
					newAddr.setMerchantId(merchantId);
					newAddr.setChain(chain);
					newAddr.setSymbol(symbol);
					newAddr.setHotAddress(address);
					toInsert.add(newAddr);

					// 可选：将新条目也加入 merchantAddresses（以防别处还用到这个 list）
					merchantAddresses.add(newAddr);
				}
			}
		}

		// 批量插入（请确保 insertBatch 是幂等或有唯一索引避免重复插入）
		if (!toInsert.isEmpty()) {
			merchantAddressMapper.insertBatch(toInsert);
			log.info("MerchantAddress 同步完成，新增 {} 条记录", toInsert.size());
		}

		return toInsert;
	}

}
