package com.yan.login.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮件登录对象
 *
 * @author Lion Li
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWeb3LoginBody extends UserLoginBody {

    /**
     * 簽名數據
     */
    @NotBlank(message="{sign.null.error}")
    private String signature;
    /**
     * 原始數據
     */
    @NotBlank(message="{original.null.error}")
    private String message;
    /**
     * 簽名地址
     */
    @NotBlank(message="{sign.address.null}")
    private String address;

    /**
     * 父邀请码
     */
    private String inviteCode;

}
