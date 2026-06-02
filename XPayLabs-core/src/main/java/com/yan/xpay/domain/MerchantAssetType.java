package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.common.core.enums.Status;

@Data
@TableName("t_merchant_asset_type")
public class MerchantAssetType {
	@TableId(type = IdType.INPUT)
	private Long assetTypeId;
	private Long merchantId;
	private Status status;
}
