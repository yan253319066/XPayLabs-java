package com.yan.xpay.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.domain.vo.AddressPoolVo;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.AddressPoolMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemAddressCache {
	public final static String redis_address_key = "xpay:address:";
	private final AddressPoolMapper addressPoolMapper;
	public SystemAddressCache(AddressPoolMapper addressPoolMapper){
		this.addressPoolMapper = addressPoolMapper;
		initialize();
	}
	private void initialize() {
		List<AddressPoolVo> addressPoolList = addressPoolMapper.selectVoList(new LambdaQueryWrapper<AddressPool>());
		Map<Chain, Map<String, String>> systemAddressMap = addressPoolList.stream()
			.collect(Collectors.groupingBy(
				AddressPoolVo::getChain,
				Collectors.toMap(
					vo -> Optional.ofNullable(vo.getAddress())
						.map(String::toLowerCase)
						.orElse(""),
					AddressPoolVo::getAddress,
					(existing, replacement) -> existing
				)
			));
		systemAddressMap.forEach((chain, addressMap)->{
			RedisUtils.setCacheMap(redis_address_key + chain.name(), addressMap);
		});
	}

	public String getAddress(Chain chain, String address) {
		if(StrUtil.isBlank(address))
			return null;
		return RedisUtils.getCacheMapValue(redis_address_key + chain, address.toLowerCase());
	}

	public void setAddress(Chain chain, String address) {
		RedisUtils.setCacheMapValue(redis_address_key + chain, address.toLowerCase(), address);
	}
	public boolean existAddress(Chain chain, String address) {
		return StrUtil.isNotBlank(getAddress(chain, address));
	}
}
