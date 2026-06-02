package com.yan.xpay.mapper;

import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.domain.vo.PendingCollectionVO;
import com.yan.xpay.domain.vo.UserAddressVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户地址Mapper接口
 *
 * @author Yan
 * @date 2025-07-28
 */
public interface UserAddressMapper extends BaseMapperPlus<UserAddress, UserAddressVo> {
	@Select("SELECT CONCAT(CHAIN, symbol) AS chain_symbol, SUM(amount) AS total_amount \n" +
		"FROM `t_user_address` \n" +
		"WHERE merchant_id = #{merchantId} \n" +
		"GROUP BY CHAIN, symbol; ")
	List<PendingCollectionVO> getPendingCollectionBalances(@Param("merchantId") Long merchantId);
}
