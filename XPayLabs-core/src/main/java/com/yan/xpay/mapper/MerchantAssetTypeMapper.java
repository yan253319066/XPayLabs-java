package com.yan.xpay.mapper;

import com.yan.xpay.domain.MerchantAssetType;
import com.yan.xpay.domain.vo.AssetTypeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

public interface MerchantAssetTypeMapper extends BaseMapperPlus<MerchantAssetType, MerchantAssetType> {
	/**
	 * 查询enabled=enabled并且status=enabled的数据
	 */
	@Select("<script>" +
		"SELECT at.id,  at.chain,  at.symbol,  at.contract_address,  at.decimals,  at.network,  at.enabled  " +
		"FROM t_asset_type at " +
		"INNER JOIN t_merchant_asset_type mat ON at.id  = mat.asset_type_id  " +
		"WHERE at.enabled  = 'ENABLED' " +
		"AND mat.status  = 'ENABLED' " +
		"AND mat.merchant_id  = #{merchantId} " +
		"<if test='network != null and network != \"\"'>" +
		" AND at.network  = #{network}" +
		"</if>" +
		"</script>")
	List<AssetTypeVo> merchantAssetTypeList(@Param("merchantId")Long merchantId, @Param("network") String network);
}
