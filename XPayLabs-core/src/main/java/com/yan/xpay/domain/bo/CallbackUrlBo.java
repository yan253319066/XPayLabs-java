package com.yan.xpay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CallbackUrlBo {
	@NotBlank(message = "callbackUrl不能为空")
	private String callbackUrl;
	@NotNull(message = "code不能为空")
	private Integer code;
}
