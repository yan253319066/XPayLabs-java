package com.yan.xpay.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatePayoutOrderReq extends BaseCreateOrderReq{

	/**
	 * Receive Address
	 */
	@NotBlank(message = "The receiveAddress cannot be left blank.")
	private String receiveAddress;
}
