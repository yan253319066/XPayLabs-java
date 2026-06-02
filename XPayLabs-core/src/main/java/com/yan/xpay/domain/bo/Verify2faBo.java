package com.yan.xpay.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Verify2faBo {
	@NotNull(message = "code不能为空")
	private Integer code;
}
