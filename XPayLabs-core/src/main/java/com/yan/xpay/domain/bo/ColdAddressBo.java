package com.yan.xpay.domain.bo;

import com.yan.xpay.enums.Chain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColdAddressBo {
	@NotBlank(message = "coldAddress不能为空")
	private String coldAddress;
	@NotNull(message = "chain不能为空")
	private Chain chain;
	@NotNull(message = "code不能为空")
	private Integer code;
}
