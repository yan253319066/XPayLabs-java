package com.yan.xpay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnergyApikeyBo {
	@NotBlank(message = "apiKey不能为空")
	private String apiKey;
	@NotNull(message = "code不能为空")
	private Integer code;
}
