package com.yan.xpay.mapper;

import com.yan.xpay.domain.MerchantAddress;
import com.yan.xpay.domain.vo.MerchantAddressVo;
import com.yan.xpay.domain.vo.MerchantAddressVo2;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 商家钱包地址Mapper接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface MerchantAddressMapper extends BaseMapperPlus<MerchantAddress, MerchantAddressVo> {
	@Select("SELECT DISTINCT cold_address AS address, merchant_id FROM t_merchant_address WHERE cold_address IS NOT NULL AND cold_address != '(NULL)'")
	List<MerchantAddressVo2> findDistinctColdAddresses();

	@Select("SELECT DISTINCT hot_address AS address, merchant_id FROM t_merchant_address WHERE hot_address IS NOT NULL AND hot_address != '(NULL)'")
	List<MerchantAddressVo2> findDistinctHotAddresses();

}
