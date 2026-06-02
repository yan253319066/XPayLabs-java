package com.yan.login.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘記密碼(短信找回)form
 * @author PC1
 *
 */
@Data
public class ForgetPwdForm {
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
	 * password
	 */
    @NotBlank(message="{password.null.error}")
    private String password;

}
