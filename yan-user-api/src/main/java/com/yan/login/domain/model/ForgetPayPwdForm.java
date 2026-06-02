package com.yan.login.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘記支付密碼form
 * @author PC1
 *
 */
@Data
public class ForgetPayPwdForm {
	/**
	 * areacode
	 */
	@NotBlank(message="{areacode.null.error}")
	private String areacode;
	/**
	 * mobile
	 */
    @NotBlank(message="{mobile.null.error}")
    private String mobile;
    /**
	 * code
	 */
    @NotBlank(message="{code.null.error}")
    private String code;
    /**
	 * payPwd
	 */
    @NotBlank(message="{payPwd.null.error}")
    private String payPwd;

}
