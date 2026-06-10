package com.yan.xpay.domain.bo;

import com.yan.xpay.enums.Chain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalBo {
	@NotNull(message = "chain不能为空")
	private Chain chain;
	@NotBlank(message = "symbol不能为空")
	private String symbol;
	@NotNull(message = "amount不能为空")
	private BigDecimal amount;
	@NotBlank(message = "address不能为空")
	private String address;
	@NotNull(message = "code不能为空")
	private Integer code;
}
