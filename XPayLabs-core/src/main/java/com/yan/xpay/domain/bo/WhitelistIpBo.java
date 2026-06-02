package com.yan.xpay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WhitelistIpBo {
	@NotNull
	private String[] ips;
	@NotNull
	private Integer code;
}
