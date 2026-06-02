package com.yan.login.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePwdForm {
	/**
	 * 旧密码
	 */
    private String oldPwd;
    /**
	 * 新密码
	 */
    @NotBlank(message="{newPwd.null.error}")
    private String newPwd;

}
