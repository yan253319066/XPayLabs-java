package com.yan.xpay.config;

import cn.hutool.core.util.StrUtil;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.service.IAssetTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AssetTypeCache {

	// 按链分类的资产类型映射
	private volatile Map<Chain, Map<String, AssetType>> chainToSymbolMap = Map.of();
	private volatile Map<Chain, Map<String, AssetType>> chainToContractMap = Map.of();

	private final IAssetTypeService assetTypeService;

	public AssetTypeCache(IAssetTypeService assetTypeService)  {
		this.assetTypeService  = assetTypeService;
		initialize();
	}

	private void initialize() {
		try {
			loadCache();
		} catch (Exception e) {
			log.error(" 多链资产类型缓存初始化失败", e);
			this.chainToSymbolMap  = Map.of();
			this.chainToContractMap  = Map.of();
		}
	}

	/**
	 * 加载/刷新缓存（线程安全）
	 */
	public synchronized void loadCache() {

		List<AssetType> assets = assetTypeService.initAssetTypeList();

		// 按链分组的符号映射
		Map<Chain, Map<String, AssetType>> newSymbolMap = assets.stream()
			.collect(Collectors.groupingBy(
				AssetType::getChain,
				Collectors.toUnmodifiableMap(
					asset -> asset.getSymbol().toUpperCase(),
					Function.identity(),
					(oldVal, newVal) -> oldVal)));

		// 按链分组的合约映射
		Map<Chain, Map<String, AssetType>> newContractMap = assets.stream()
			.collect(Collectors.groupingBy(
				AssetType::getChain,
				Collectors.toMap(   // 使用toMap而不是toUnmodifiableMap
					asset -> asset.getContractAddress()  != null ?
						asset.getContractAddress().toLowerCase()  :
						null,
					Function.identity(),
					(oldVal, newVal) -> oldVal,
					HashMap::new  // 指定使用HashMap允许null键
				)));
//		log.info("chainToContractMap {}", JSONUtil.toJsonPrettyStr(newContractMap));
		this.chainToSymbolMap  = newSymbolMap;
		this.chainToContractMap  = newContractMap;
	}

	/**
	 * 通过链和符号获取资产
	 */
	public AssetType getBySymbol(Chain chain, String symbol) {
		if (chain == null || symbol == null) return null;

		return Optional.ofNullable(chainToSymbolMap.get(chain))
			.map(m -> m.get(symbol.toUpperCase()))
			.orElse(null);
	}

	/**
	 * 通过链和合约地址获取资产（适用于ERC20/TRC20/BEP20等）
	 */
	public AssetType getByContractAddress(Chain chain, String contractAddress) {
		if (chain == null) return null;

		return Optional.ofNullable(chainToContractMap.get(chain))
			.map(m -> contractAddress != null
				? m.get(contractAddress.toLowerCase())
				: m.get(null))
			.orElse(null);
	}

	/**
	 * 获取某条链的所有可用资产类型
	 */
	public List<AssetType> getAssetsByChain(Chain chain) {
		if (chain == null) return List.of();

		return Optional.ofNullable(chainToContractMap.get(chain))
			.map(Map::entrySet) // 先取 entrySet
			.map(entries -> entries.stream()
				.sorted(Comparator.comparing(
					e -> e.getKey(),  // 排序 key
					Comparator.nullsLast(Comparator.naturalOrder())  // null 放最后
				))
				.map(Map.Entry::getValue) // 取 value
				.collect(Collectors.toList())  // 收集为 List
			)
			.orElseGet(ArrayList::new);
	}

	/**
	 * 判断是否为原生代币（如TRX/ETH/BNB）
	 */
	public boolean isNativeToken(Chain chain, String symbol) {
		AssetType asset = getBySymbol(chain, symbol);
		return StrUtil.isBlank(asset.getContractAddress());
	}

	/**
	 * 根据链获取原生代币（如 ETH、BNB、TRX）
	 * @param chain 链类型
	 * @return 该链的原生代币（若无则返回 null）
	 */
	public AssetType getNativeToken(Chain chain) {
		if (chain == null) {
			return null;
		}

		// 遍历该链的所有资产，找到 contractAddress 为空的代币
		return Optional.ofNullable(chainToContractMap.get(chain))
			.map(Map::values) // 获取所有资产
			.flatMap(assets -> assets.stream()
				.filter(asset -> StrUtil.isBlank(asset.getContractAddress()))  // 原生代币无合约地址
				.findFirst() // 取第一个（通常一个链只有一个原生代币）
			)
			.orElse(null);
	}

	public Set<Chain> getChains(){
		return chainToSymbolMap.keySet();
	}

}
